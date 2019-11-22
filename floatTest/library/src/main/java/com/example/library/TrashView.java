package com.example.library;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * This is a view to erase the FloatingView.
 */
class TrashView extends FrameLayout implements ViewTreeObserver.OnPreDrawListener {

    /**
     * background height(dp)
     */
    private static final int BACKGROUND_HEIGHT = 164;

    /**
     * Horizontal area for capturing targets(dp)
     */
    private static final float TARGET_CAPTURE_HORIZONTAL_REGION = 30.0f;

    /**
     * Vertical space to capture targets(dp)
     */
    private static final float TARGET_CAPTURE_VERTICAL_REGION = 4.0f;

    /**
     * Animation time to enlarge or reduce deleted icons
     */
    private static final long TRASH_ICON_SCALE_DURATION_MILLIS = 200L;

    /**
     * an animation-free constant
     */
    static final int ANIMATION_NONE = 0;
    /**
     * Fixed number representing animation showing background and deletion icon
     * Includes tracking of FloatingView.
     */
    static final int ANIMATION_OPEN = 1;
    /**
     * The constant representing animation that erases background, deletion icon, etc.
     */
    static final int ANIMATION_CLOSE = 2;
    /**
     * The constant that indicates that the background and deletion icons should be deleted immediately.
     */
    static final int ANIMATION_FORCE_CLOSE = 3;

    /**
     * Animation State
     */
    @IntDef({ANIMATION_NONE, ANIMATION_OPEN, ANIMATION_CLOSE, ANIMATION_FORCE_CLOSE})
    @Retention(RetentionPolicy.SOURCE)
    @interface AnimationState {
    }

    /**
     * long-press time
     */
    private static final int LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();

    /**
     * Overlay Type
     */
    private static final int OVERLAY_TYPE;

    /**
     * WindowManager
     */
    private final WindowManager mWindowManager;

    /**
     * LayoutParams
     */
    private final WindowManager.LayoutParams mParams;

    /**
     * DisplayMetrics
     */
    private final DisplayMetrics mMetrics;

    /**
     * Root View (background, View with delete icon)
     */
    private final ViewGroup mRootView;

    /**
     * delete icon
     */
    private final FrameLayout mTrashIconRootView;

    /**
     * fixed delete icon
     */
    private final ImageView mFixedTrashIconView;

    /**
     * Delete icon that operates according to overlap
     */
    private final ImageView mActionTrashIconView;

    /**
     * ActionTrashIcon of Width
     */
    private int mActionTrashIconBaseWidth;

    /**
     * ActionTrashIcon of Height
     */
    private int mActionTrashIconBaseHeight;

    /**
     * ActionTrashIcon of maximum expansion rate
     */
    private float mActionTrashIconMaxScale;

    /**
     * 背景View
     */
    private final FrameLayout mBackgroundView;

    /**
     * Animation (enlarged) when you enter the deleted icon
     */
    private ObjectAnimator mEnterScaleAnimator;

    /**
     * Animation when you go out of the box of the deleted icon (reduced)
     */
    private ObjectAnimator mExitScaleAnimator;

    /**
     * animated handler
     */
    private final AnimationHandler mAnimationHandler;

    /**
     * TrashViewListener
     */
    private TrashViewListener mTrashViewListener;

    /**
     * Enabled/disabled flag for View (not displayed if disabled)
     */
    private boolean mIsEnabled;

    static {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            OVERLAY_TYPE = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        } else {
            OVERLAY_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
    }

    /**
     * constructor
     *
     * @param context Context
     */
    TrashView(Context context) {
        super(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        mAnimationHandler = new AnimationHandler(this);
        mIsEnabled = true;

        mParams = new WindowManager.LayoutParams();
        mParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mParams.type = OVERLAY_TYPE;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mParams.format = PixelFormat.TRANSLUCENT;

        // INFO:Window of setting origin only to the lower left
        mParams.gravity = Gravity.LEFT | Gravity.BOTTOM;

        // Configuring Various Views
        // Views that are pasted directly into the TrashView
        // (Without this View, the layout of the deleted View and the background View is somehow broken.)
        mRootView = new FrameLayout(context);
        mRootView.setClipChildren(false);
        // Remove Icon Root View
        mTrashIconRootView = new FrameLayout(context);
        mTrashIconRootView.setClipChildren(false);
        mFixedTrashIconView = new ImageView(context);
        mActionTrashIconView = new ImageView(context);
        // Background View
        mBackgroundView = new FrameLayout(context);
        mBackgroundView.setAlpha(0.0f);
        final GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0x00000000, 0x50000000});
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            mBackgroundView.setBackgroundDrawable(gradientDrawable);
        } else {
            mBackgroundView.setBackground(gradientDrawable);
        }

        // Paste Background View
        final LayoutParams backgroundParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (BACKGROUND_HEIGHT * mMetrics.density));
        backgroundParams.gravity = Gravity.BOTTOM;
        mRootView.addView(mBackgroundView, backgroundParams);
        // Paste Action Icons
        final LayoutParams actionTrashIconParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionTrashIconParams.gravity = Gravity.CENTER;
        mTrashIconRootView.addView(mActionTrashIconView, actionTrashIconParams);
        // Attaching Fixed Icons
        final LayoutParams fixedTrashIconParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fixedTrashIconParams.gravity = Gravity.CENTER;
        mTrashIconRootView.addView(mFixedTrashIconView, fixedTrashIconParams);
        // Paste Delete Icons
        final LayoutParams trashIconParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        trashIconParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        mRootView.addView(mTrashIconRootView, trashIconParams);

        // Paste into TrashView
        addView(mRootView);

        // first-time drawing processing
        getViewTreeObserver().addOnPreDrawListener(this);
    }

    /**
     * Determine the Display Location.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateViewLayout();
    }

    /**
     * Adjust the layout when the screen rotates.
     */
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateViewLayout();
    }

    /**
     * Configure the coordinates settings for the first time drawing.<br/>
     * There are events where the delete icon is displayed only for a moment at the first time.
     */
    @Override
    public boolean onPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);
        mTrashIconRootView.setTranslationY(mTrashIconRootView.getMeasuredHeight());
        return true;
    }

    /**
     * initialize ActionTrashIcon
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mTrashViewListener.onUpdateActionTrashIcon();
    }

    /**
     * Determine your location from the screen size.
     */
    private void updateViewLayout() {
        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        mParams.x = (mMetrics.widthPixels - getWidth()) / 2;
        mParams.y = 0;

        // Update view and layout
        mTrashViewListener.onUpdateActionTrashIcon();
        mAnimationHandler.onUpdateViewLayout();

        mWindowManager.updateViewLayout(this, mParams);
    }

    /**
     * Hide the TrashView.
     */
    void dismiss() {
        // animation stop
        mAnimationHandler.removeMessages(ANIMATION_OPEN);
        mAnimationHandler.removeMessages(ANIMATION_CLOSE);
        mAnimationHandler.sendAnimationMessage(ANIMATION_FORCE_CLOSE);
        // Stop Extended Animation
        setScaleTrashIconImmediately(false);
    }

    /**
     * Retrieves the drawing area on the Window.
     * Represents the rectangle of the hit decision.
     *
     * @param outRect Rect to make changes
     */
    void getWindowDrawingRect(Rect outRect) {
        // Gravity is reversed, so the rectangle hit is reversed upside down (top/bottom)
        // Set the top (downward on screen) decision more
        final ImageView iconView = hasActionTrashIcon() ? mActionTrashIconView : mFixedTrashIconView;
        final float iconPaddingLeft = iconView.getPaddingLeft();
        final float iconPaddingTop = iconView.getPaddingTop();
        final float iconWidth = iconView.getWidth() - iconPaddingLeft - iconView.getPaddingRight();
        final float iconHeight = iconView.getHeight() - iconPaddingTop - iconView.getPaddingBottom();
        final float x = mTrashIconRootView.getX() + iconPaddingLeft;
        final float y = mRootView.getHeight() - mTrashIconRootView.getY() - iconPaddingTop - iconHeight;
        final int left = (int) (x - TARGET_CAPTURE_HORIZONTAL_REGION * mMetrics.density);
        final int top = -mRootView.getHeight();
        final int right = (int) (x + iconWidth + TARGET_CAPTURE_HORIZONTAL_REGION * mMetrics.density);
        final int bottom = (int) (y + iconHeight + TARGET_CAPTURE_VERTICAL_REGION * mMetrics.density);
        outRect.set(left, top, right, bottom);
    }

    /**
     * Update the settings for the delete icon you want to act on.
     *
     * @param width  Width of Views Affected
     * @param height View Heights Affected
     * @param shape  View geometry
     */
    void updateActionTrashIcon(float width, float height, float shape) {
        // Do nothing if the delete icon you want to take action is not set
        if (!hasActionTrashIcon()) {
            return;
        }
        // Configuring the Scaling Rate
        mAnimationHandler.mTargetWidth = width;
        mAnimationHandler.mTargetHeight = height;
        final float newWidthScale = width / mActionTrashIconBaseWidth * shape;
        final float newHeightScale = height / mActionTrashIconBaseHeight * shape;
        mActionTrashIconMaxScale = Math.max(newWidthScale, newHeightScale);
        // ENTER ANIMATION CREATION
        mEnterScaleAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionTrashIconView, PropertyValuesHolder.ofFloat(ImageView.SCALE_X, mActionTrashIconMaxScale), PropertyValuesHolder.ofFloat(ImageView.SCALE_Y, mActionTrashIconMaxScale));
        mEnterScaleAnimator.setInterpolator(new OvershootInterpolator());
        mEnterScaleAnimator.setDuration(TRASH_ICON_SCALE_DURATION_MILLIS);
        // Exit Animation
        mExitScaleAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionTrashIconView, PropertyValuesHolder.ofFloat(ImageView.SCALE_X, 1.0f), PropertyValuesHolder.ofFloat(ImageView.SCALE_Y, 1.0f));
        mExitScaleAnimator.setInterpolator(new OvershootInterpolator());
        mExitScaleAnimator.setDuration(TRASH_ICON_SCALE_DURATION_MILLIS);
    }

    /**
     * Obtain the center X-coordinate of the deleted icon.
     *
     * @return center X-coordinate of deleted icon
     */
    float getTrashIconCenterX() {
        final ImageView iconView = hasActionTrashIcon() ? mActionTrashIconView : mFixedTrashIconView;
        final float iconViewPaddingLeft = iconView.getPaddingLeft();
        final float iconWidth = iconView.getWidth() - iconViewPaddingLeft - iconView.getPaddingRight();
        final float x = mTrashIconRootView.getX() + iconViewPaddingLeft;
        return x + iconWidth / 2;
    }

    /**
     * Obtain the center Y-coordinate of the deleted icon.
     *
     * @return center Y-coordinate of deleted icon
     */
    float getTrashIconCenterY() {
        final ImageView iconView = hasActionTrashIcon() ? mActionTrashIconView : mFixedTrashIconView;
        final float iconViewHeight = iconView.getHeight();
        final float iconViewPaddingBottom = iconView.getPaddingBottom();
        final float iconHeight = iconViewHeight - iconView.getPaddingTop() - iconViewPaddingBottom;
        final float y = mRootView.getHeight() - mTrashIconRootView.getY() - iconViewHeight + iconViewPaddingBottom;
        return y + iconHeight / 2;
    }


    /**
     * Check if there is a delete icon to act on.
     *
     * @return True if there is a delete icon to act on
     */
    private boolean hasActionTrashIcon() {
        return mActionTrashIconBaseWidth != 0 && mActionTrashIconBaseHeight != 0;
    }

    /**
     * Set the fixed delete icon image.<br/>
     * This image does not change in size when the floating displays overlap.
     *
     * @param resId drawable ID
     */
    void setFixedTrashIconImage(int resId) {
        mFixedTrashIconView.setImageResource(resId);
    }

    /**
     * Sets the image of the delete icon to be acted on.<br/>
     * This image changes in size when the floating displays overlap.
     *
     * @param resId drawable ID
     */
    void setActionTrashIconImage(int resId) {
        mActionTrashIconView.setImageResource(resId);
        final Drawable drawable = mActionTrashIconView.getDrawable();
        if (drawable != null) {
            mActionTrashIconBaseWidth = drawable.getIntrinsicWidth();
            mActionTrashIconBaseHeight = drawable.getIntrinsicHeight();
        }
    }

    /**
     * Set the static delete icon.<br/>
     * This image does not change in size when the floating displays overlap.
     *
     * @param drawable Drawable
     */
    void setFixedTrashIconImage(Drawable drawable) {
        mFixedTrashIconView.setImageDrawable(drawable);
    }

    /**
     * Set the action delete icon.<br/>
     * This image changes in size when the floating displays overlap.
     *
     * @param drawable Drawable
     */
    void setActionTrashIconImage(Drawable drawable) {
        mActionTrashIconView.setImageDrawable(drawable);
        if (drawable != null) {
            mActionTrashIconBaseWidth = drawable.getIntrinsicWidth();
            mActionTrashIconBaseHeight = drawable.getIntrinsicHeight();
        }
    }

    /**
     * Change the size of the delete icon immediately.
     *
     * @param isEnter True if you enter the region; false otherwise.
     */
    private void setScaleTrashIconImmediately(boolean isEnter) {
        cancelScaleTrashAnimation();

        mActionTrashIconView.setScaleX(isEnter ? mActionTrashIconMaxScale : 1.0f);
        mActionTrashIconView.setScaleY(isEnter ? mActionTrashIconMaxScale : 1.0f);
    }

    /**
     * Change the size of the delete icon.
     *
     * @param isEnter True if you enter the region; false otherwise.
     */
    void setScaleTrashIcon(boolean isEnter) {
        // Do nothing unless the action icon is set
        if (!hasActionTrashIcon()) {
            return;
        }

        // Cancel Animation
        cancelScaleTrashAnimation();

        // when in the realm of
        if (isEnter) {
            mEnterScaleAnimator.start();
        } else {
            mExitScaleAnimator.start();
        }
    }

    /**
     * Enable or disable TrashView.
     *
     * @param enabled Enabled for true (display), disabled for false (hidden)
     */
    void setTrashEnabled(boolean enabled) {
        // Do nothing if the configurations are the same
        if (mIsEnabled == enabled) {
            return;
        }

        // Close to hide
        mIsEnabled = enabled;
        if (!mIsEnabled) {
            dismiss();
        }
    }

    /**
     * Retrieves the view state of the TrashView.
     *
     * @return Display if true
     */
    boolean isTrashEnabled() {
        return mIsEnabled;
    }

    /**
     * Cancel deleted icon expansion/reduction animation
     */
    private void cancelScaleTrashAnimation() {
        // in-frame animation
        if (mEnterScaleAnimator != null && mEnterScaleAnimator.isStarted()) {
            mEnterScaleAnimator.cancel();
        }

        // extra-frame animation
        if (mExitScaleAnimator != null && mExitScaleAnimator.isStarted()) {
            mExitScaleAnimator.cancel();
        }
    }

    /**
     * Configure the TrashViewListener
     *
     * @param listener TrashViewListener
     */
    void setTrashViewListener(TrashViewListener listener) {
        mTrashViewListener = listener;
    }

    /**
     * WindowManager.LayoutParams
     *
     * @return WindowManager.LayoutParams
     */
    WindowManager.LayoutParams getWindowLayoutParams() {
        return mParams;
    }

    /**
     * Perform actions related to FloatingView.
     *
     * @param event MotionEvent
     * @param x     FloatingView of X coordinate
     * @param y     FloatingView of Y coordinate
     */
    void onTouchFloatingView(MotionEvent event, float x, float y) {
        final int action = event.getAction();
        // depression
        if (action == MotionEvent.ACTION_DOWN) {
            mAnimationHandler.updateTargetPosition(x, y);
            // Long push waiting
            mAnimationHandler.removeMessages(ANIMATION_CLOSE);
            mAnimationHandler.sendAnimationMessageDelayed(ANIMATION_OPEN, LONG_PRESS_TIMEOUT);
        }
        // migration
        else if (action == MotionEvent.ACTION_MOVE) {
            mAnimationHandler.updateTargetPosition(x, y);
            // Run only if open animation has not yet started
            if (!mAnimationHandler.isAnimationStarted(ANIMATION_OPEN)) {
                // Delete long-press messages
                mAnimationHandler.removeMessages(ANIMATION_OPEN);
                // open
                mAnimationHandler.sendAnimationMessage(ANIMATION_OPEN);
            }
        }
        // push up, cancel
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            // Delete long-press messages
            mAnimationHandler.removeMessages(ANIMATION_OPEN);
            mAnimationHandler.sendAnimationMessage(ANIMATION_CLOSE);
        }
    }

    /**
     * A handler that controls animation.
     */
    static class AnimationHandler extends Handler {

        /**
         * Milliseconds to refresh the animation
         */
        private static final long ANIMATION_REFRESH_TIME_MILLIS = 10L;

        /**
         * background animation time
         */
        private static final long BACKGROUND_DURATION_MILLIS = 200L;

        /**
         * Start delay time of pop animation for deleted icons
         */
        private static final long TRASH_OPEN_START_DELAY_MILLIS = 200L;

        /**
         * Delete Icon Open Animation Time
         */
        private static final long TRASH_OPEN_DURATION_MILLIS = 400L;

        /**
         * Closed animation time for deleted icons
         */
        private static final long TRASH_CLOSE_DURATION_MILLIS = 200L;

        /**
         * Overshoot Animation Factor
         */
        private static final float OVERSHOOT_TENSION = 1.0f;

        /**
         * Remove icon movement limit X-axis offset(dp)
         */
        private static final int TRASH_MOVE_LIMIT_OFFSET_X = 22;

        /**
         * Remove Icon Move Limit Y-axis Offset(dp)
         */
        private static final int TRASH_MOVE_LIMIT_TOP_OFFSET = -4;

        /**
         * an animation constant
         */
        private static final int TYPE_FIRST = 1;
        /**
         * constant representing animation updates
         */
        private static final int TYPE_UPDATE = 2;

        /**
         * alpha maximum
         */
        private static final float MAX_ALPHA = 1.0f;

        /**
         * alpha minimum
         */
        private static final float MIN_ALPHA = 0.0f;

        /**
         * the time at which an animation was started
         */
        private long mStartTime;

        /**
         * alpha value at the time of animation
         */
        private float mStartAlpha;

        /**
         * TransitionY at the time of animation
         */
        private float mStartTransitionY;

        /**
         * Code of running animation
         */
        private int mStartedCode;

        /**
         * X-coordinate to follow
         */
        private float mTargetPositionX;

        /**
         * Y-coordinate to follow
         */
        private float mTargetPositionY;

        /**
         * width of a follow-up object
         */
        private float mTargetWidth;

        /**
         * height of a follow-up target
         */
        private float mTargetHeight;

        /**
         * Remove Icon Move Limit Location
         */
        private final Rect mTrashIconLimitPosition;

        /**
         * Y-axis range
         */
        private float mMoveStickyYRange;

        /**
         * OvershootInterpolator
         */
        private final OvershootInterpolator mOvershootInterpolator;


        /**
         * TrashView
         */
        private final WeakReference<TrashView> mTrashView;

        /**
         * constructor
         */
        AnimationHandler(TrashView trashView) {
            mTrashView = new WeakReference<>(trashView);
            mStartedCode = ANIMATION_NONE;
            mTrashIconLimitPosition = new Rect();
            mOvershootInterpolator = new OvershootInterpolator(OVERSHOOT_TENSION);
        }

        /**
         * We process the animation.
         */
        @Override
        public void handleMessage(Message msg) {
            final TrashView trashView = mTrashView.get();
            if (trashView == null) {
                removeMessages(ANIMATION_OPEN);
                removeMessages(ANIMATION_CLOSE);
                removeMessages(ANIMATION_FORCE_CLOSE);
                return;
            }

            // do not animate when not valid
            if (!trashView.isTrashEnabled()) {
                return;
            }

            final int animationCode = msg.what;
            final int animationType = msg.arg1;
            final FrameLayout backgroundView = trashView.mBackgroundView;
            final FrameLayout trashIconRootView = trashView.mTrashIconRootView;
            final TrashViewListener listener = trashView.mTrashViewListener;
            final float screenWidth = trashView.mMetrics.widthPixels;
            final float trashViewX = trashView.mParams.x;

            // Initialize on Animation
            if (animationType == TYPE_FIRST) {
                mStartTime = SystemClock.uptimeMillis();
                mStartAlpha = backgroundView.getAlpha();
                mStartTransitionY = trashIconRootView.getTranslationY();
                mStartedCode = animationCode;
                if (listener != null) {
                    listener.onTrashAnimationStarted(mStartedCode);
                }
            }
            // elapsed time
            final float elapsedTime = SystemClock.uptimeMillis() - mStartTime;

            // display animation
            if (animationCode == ANIMATION_OPEN) {
                final float currentAlpha = backgroundView.getAlpha();
                // If the maximum alpha value is not reached
                if (currentAlpha < MAX_ALPHA) {
                    final float alphaTimeRate = Math.min(elapsedTime / BACKGROUND_DURATION_MILLIS, 1.0f);
                    final float alpha = Math.min(mStartAlpha + alphaTimeRate, MAX_ALPHA);
                    backgroundView.setAlpha(alpha);
                }

                // If it exceeds DelayTime, it starts animation.
                if (elapsedTime >= TRASH_OPEN_START_DELAY_MILLIS) {
                    final float screenHeight = trashView.mMetrics.heightPixels;
                    // Calculate 0% and 100% respectively when all the icons stick out from side to side.
                    final float positionX = trashViewX + (mTargetPositionX + mTargetWidth) / (screenWidth + mTargetWidth) * mTrashIconLimitPosition.width() + mTrashIconLimitPosition.left;
                    // Delete icon Y-coordinate animation and follow-up (upward negative)
                    // targetPositionYRate is 0% when the Y-coordinate of the target is completely out of the screen,
                    // and 100% after half the screen.
                    // stickyPositionY moves the bottom edge of the migration limit to the top edge at the origin, mMoveStickyRange follows
                    // positionY calculations move over time
                    final float targetPositionYRate = Math.min(2 * (mTargetPositionY + mTargetHeight) / (screenHeight + mTargetHeight), 1.0f);
                    final float stickyPositionY = mMoveStickyYRange * targetPositionYRate + mTrashIconLimitPosition.height() - mMoveStickyYRange;
                    final float translationYTimeRate = Math.min((elapsedTime - TRASH_OPEN_START_DELAY_MILLIS) / TRASH_OPEN_DURATION_MILLIS, 1.0f);
                    final float positionY = mTrashIconLimitPosition.bottom - stickyPositionY * mOvershootInterpolator.getInterpolation(translationYTimeRate);
                    trashIconRootView.setTranslationX(positionX);
                    trashIconRootView.setTranslationY(positionY);
                    // clear drag view garbage
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        clearClippedChildren(trashView.mRootView);
                        clearClippedChildren(trashView.mTrashIconRootView);
                    }
                }

                sendMessageAtTime(newMessage(animationCode, TYPE_UPDATE), SystemClock.uptimeMillis() + ANIMATION_REFRESH_TIME_MILLIS);
            }
            // hidden animation
            else if (animationCode == ANIMATION_CLOSE) {
                // calculation of alpha values
                final float alphaElapseTimeRate = Math.min(elapsedTime / BACKGROUND_DURATION_MILLIS, 1.0f);
                final float alpha = Math.max(mStartAlpha - alphaElapseTimeRate, MIN_ALPHA);
                backgroundView.setAlpha(alpha);

                // Y-coordinate animation of deleted icons
                final float translationYTimeRate = Math.min(elapsedTime / TRASH_CLOSE_DURATION_MILLIS, 1.0f);
                // If the animation hasn't reached the end
                if (alphaElapseTimeRate < 1.0f || translationYTimeRate < 1.0f) {
                    final float position = mStartTransitionY + mTrashIconLimitPosition.height() * translationYTimeRate;
                    trashIconRootView.setTranslationY(position);
                    sendMessageAtTime(newMessage(animationCode, TYPE_UPDATE), SystemClock.uptimeMillis() + ANIMATION_REFRESH_TIME_MILLIS);
                } else {
                    // Force position adjustment
                    trashIconRootView.setTranslationY(mTrashIconLimitPosition.bottom);
                    mStartedCode = ANIMATION_NONE;
                    if (listener != null) {
                        listener.onTrashAnimationEnd(ANIMATION_CLOSE);
                    }
                }
            }
            // Immediate hiding
            else if (animationCode == ANIMATION_FORCE_CLOSE) {
                backgroundView.setAlpha(0.0f);
                trashIconRootView.setTranslationY(mTrashIconLimitPosition.bottom);
                mStartedCode = ANIMATION_NONE;
                if (listener != null) {
                    listener.onTrashAnimationEnd(ANIMATION_FORCE_CLOSE);
                }
            }
        }

        /**
         * Clear the animation garbage of the target view.
         */
        private static void clearClippedChildren(ViewGroup viewGroup) {
            viewGroup.setClipChildren(true);
            viewGroup.invalidate();
            viewGroup.setClipChildren(false);
        }

        /**
         * Send an animation message.
         *
         * @param animation   ANIMATION_OPEN,ANIMATION_CLOSE,ANIMATION_FORCE_CLOSE
         * @param delayMillis Message transmission time
         */
        void sendAnimationMessageDelayed(int animation, long delayMillis) {
            sendMessageAtTime(newMessage(animation, TYPE_FIRST), SystemClock.uptimeMillis() + delayMillis);
        }

        /**
         * Send an animation message.
         *
         * @param animation ANIMATION_OPEN,ANIMATION_CLOSE,ANIMATION_FORCE_CLOSE
         */
        void sendAnimationMessage(int animation) {
            sendMessage(newMessage(animation, TYPE_FIRST));
        }

        /**
         * 送信するメッセージを生成します。
         *
         * @param animation ANIMATION_OPEN,ANIMATION_CLOSE,ANIMATION_FORCE_CLOSE
         * @param type      TYPE_FIRST,TYPE_UPDATE
         * @return Message
         */
        private static Message newMessage(int animation, int type) {
            final Message message = Message.obtain();
            message.what = animation;
            message.arg1 = type;
            return message;
        }

        /**
         * Check if the animation is starting.
         *
         * @param animationCode animation code
         * @return True if the animation was started otherwise false
         */
        boolean isAnimationStarted(int animationCode) {
            return mStartedCode == animationCode;
        }

        /**
         * Update the location information to follow.
         *
         * @param x X-coordinate to follow
         * @param y Y-coordinate to follow
         */
        void updateTargetPosition(float x, float y) {
            mTargetPositionX = x;
            mTargetPositionY = y;
        }

        /**
         * Called when View display state changes.
         */
        void onUpdateViewLayout() {
            final TrashView trashView = mTrashView.get();
            if (trashView == null) {
                return;
            }
            // Migration Limit Settings for the Delete Icon (TrashIconRootView)
            // (calculated based on the Gravity reference position)
            // lower left origin（Lower edge of screen (including padding): 0, Up: Negative, Down: Plus）
            // The upper limit of the Y axis is where the delete icon comes to the center of the background,
            // and the lower limit is where the TrashIconRootView is all hidden.
            final float density = trashView.mMetrics.density;
            final float backgroundHeight = trashView.mBackgroundView.getMeasuredHeight();
            final float offsetX = TRASH_MOVE_LIMIT_OFFSET_X * density;
            final int trashIconHeight = trashView.mTrashIconRootView.getMeasuredHeight();
            final int left = (int) -offsetX;
            final int top = (int) ((trashIconHeight - backgroundHeight) / 2 - TRASH_MOVE_LIMIT_TOP_OFFSET * density);
            final int right = (int) offsetX;
            final int bottom = trashIconHeight;
            mTrashIconLimitPosition.set(left, top, right, bottom);

            // Set the Y-axis tracking range based on the size of the background.
            mMoveStickyYRange = backgroundHeight * 0.20f;
        }
    }
}
