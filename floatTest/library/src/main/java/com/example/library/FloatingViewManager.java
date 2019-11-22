package com.example.library;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.DisplayCutout;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * FloatingViewManager.
 *TODO: Look for the cause because the operation is quick.
 *TODO: Second multi-display support for follow-up movement
 */
public class FloatingViewManager implements ScreenChangedListener, View.OnTouchListener, TrashViewListener {

    /**
     * Always visible mode
     */
    public static final int DISPLAY_MODE_SHOW_ALWAYS = 1;

    /**
     * Always hidden mode
     */
    public static final int DISPLAY_MODE_HIDE_ALWAYS = 2;

    /**
     * Mode to hide at full screen
     */
    public static final int DISPLAY_MODE_HIDE_FULLSCREEN = 3;

    /**
     * display mode
     */
    @IntDef({DISPLAY_MODE_SHOW_ALWAYS, DISPLAY_MODE_HIDE_ALWAYS, DISPLAY_MODE_HIDE_FULLSCREEN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DisplayMode {
    }

    /**
     * Moves left and right
     */
    public static final int MOVE_DIRECTION_DEFAULT = 0;
    /**
     * Always move to the left
     */
    public static final int MOVE_DIRECTION_LEFT = 1;
    /**
     * 常に右に移動
     */
    public static final int MOVE_DIRECTION_RIGHT = 2;

    /**
     * immigrate
     */
    public static final int MOVE_DIRECTION_NONE = 3;

    /**
     * It moves in a direction that's close to the side.
     */
    public static final int MOVE_DIRECTION_NEAREST = 4;

    /**
     * Goes in the direction in which it is thrown
     */
    public static final int MOVE_DIRECTION_THROWN = 5;

    /**
     * Moving direction
     */
    @IntDef({MOVE_DIRECTION_DEFAULT, MOVE_DIRECTION_LEFT, MOVE_DIRECTION_RIGHT,
            MOVE_DIRECTION_NEAREST, MOVE_DIRECTION_NONE, MOVE_DIRECTION_THROWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MoveDirection {
    }

    /**
     * {@link Context}
     */
    private final Context mContext;

    /**
     * {@link Resources}
     */
    private final Resources mResources;

    /**
     * WindowManager
     */
    private final WindowManager mWindowManager;

    /**
     * {@link DisplayMetrics}
     */
    private final DisplayMetrics mDisplayMetrics;

    /**
     * Working FloatingView
     */
    private FloatingView mTargetFloatingView;

    /**
     * This is a full-screen view.
     */
    private final FullscreenObserverView mFullscreenObserverView;

    /**
     * This is the View to delete the FloatingView.。
     */
    private final TrashView mTrashView;

    /**
     * FloatingViewListener
     */
    private final FloatingViewListener mFloatingViewListener;

    /**
     * FloatingView Remarkable rectangle
     */
    private final Rect mFloatingViewRect;

    /**
     * TrashView hit detection rectangle
     */
    private final Rect mTrashViewRect;

    /**
     * Flag that allows touch movement
     * Flag that prevents touch processing from being accepted when the screen is rotated
     */
    private boolean mIsMoveAccept;

    /**
     * Current Display Mode
     */
    @DisplayMode
    private int mDisplayMode;

    /**
     * Cutout safe inset rect
     */
    private final Rect mSafeInsetRect;

    /**
     * List of FloatingViews pasted into Window
     *TODO: Will demonstrate meaning in multiple views of the second FloatingView
     */
    private final ArrayList<FloatingView> mFloatingViewList;

    /**
     * constructor
     *
     * @param context  Context
     * @param listener FloatingViewListener
     */
    public FloatingViewManager(Context context, FloatingViewListener listener) {
        mContext = context;
        mResources = context.getResources();
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplayMetrics = new DisplayMetrics();
        mFloatingViewListener = listener;
        mFloatingViewRect = new Rect();
        mTrashViewRect = new Rect();
        mIsMoveAccept = false;
        mDisplayMode = DISPLAY_MODE_HIDE_FULLSCREEN;
        mSafeInsetRect = new Rect();

        // Building Views with FloatingViews
        mFloatingViewList = new ArrayList<>();
        mFullscreenObserverView = new FullscreenObserverView(context, this);
        mTrashView = new TrashView(context);
    }

    /**
     * Check if it overlaps with the Deleted View。
     *
     * @return True if the deleted View overlaps
     */
    private boolean isIntersectWithTrash() {
        // Invalid case, no overlap is determined.
        if (!mTrashView.isTrashEnabled()) {
            return false;
        }
        // INFO:TrashView and FloatingView must be the same Gravity
        mTrashView.getWindowDrawingRect(mTrashViewRect);
        mTargetFloatingView.getWindowDrawingRect(mFloatingViewRect);
        return Rect.intersects(mTrashViewRect, mFloatingViewRect);
    }

    /**
     * Hide View if the Screen Is Full。
     */
    @Override
    public void onScreenChanged(Rect windowRect, int visibility) {
        // detect status bar
        final boolean isFitSystemWindowTop = windowRect.top == 0;
        boolean isHideStatusBar;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && visibility != FullscreenObserverView.NO_LAST_VISIBILITY) {
            // Support for screen rotation when setSystemUiVisibility is used
            isHideStatusBar = isFitSystemWindowTop || (visibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) == View.SYSTEM_UI_FLAG_LOW_PROFILE;
        } else {
            isHideStatusBar = isFitSystemWindowTop;
        }

        // detect navigation bar
        final boolean isHideNavigationBar;
        if (visibility == FullscreenObserverView.NO_LAST_VISIBILITY) {
            // At the first it can not get the correct value, so do special processing
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mWindowManager.getDefaultDisplay().getRealMetrics(mDisplayMetrics);
                isHideNavigationBar = windowRect.width() - mDisplayMetrics.widthPixels == 0 && windowRect.bottom - mDisplayMetrics.heightPixels == 0;
            } else {
                mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
                isHideNavigationBar = windowRect.width() - mDisplayMetrics.widthPixels > 0 || windowRect.height() - mDisplayMetrics.heightPixels > 0;
            }
        } else {
            isHideNavigationBar = (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        final boolean isPortrait = mResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        // update FloatingView layout
        mTargetFloatingView.onUpdateSystemLayout(isHideStatusBar, isHideNavigationBar, isPortrait, windowRect);

        // Do nothing if not in full screen hidden mode
        if (mDisplayMode != DISPLAY_MODE_HIDE_FULLSCREEN) {
            return;
        }

        mIsMoveAccept = false;
        final int state = mTargetFloatingView.getState();
        // Hide all non-overlapping
        if (state == FloatingView.STATE_NORMAL) {
            final int size = mFloatingViewList.size();
            for (int i = 0; i < size; i++) {
                final FloatingView floatingView = mFloatingViewList.get(i);
                floatingView.setVisibility(isFitSystemWindowTop ? View.GONE : View.VISIBLE);
            }
            mTrashView.dismiss();
        }
        // Delete overlapping
        else if (state == FloatingView.STATE_INTERSECTING) {
            mTargetFloatingView.setFinishing();
            mTrashView.dismiss();
        }
    }

    /**
     * Update ActionTrashIcon
     */
    @Override
    public void onUpdateActionTrashIcon() {
        mTrashView.updateActionTrashIcon(mTargetFloatingView.getMeasuredWidth(), mTargetFloatingView.getMeasuredHeight(), mTargetFloatingView.getShape());
    }

    /**
     * Lock the FloatingView Touch
     */
    @Override
    public void onTrashAnimationStarted(@TrashView.AnimationState int animationCode) {
        // Do not touch all FloatingViews when closed or forced.
        if (animationCode == TrashView.ANIMATION_CLOSE || animationCode == TrashView.ANIMATION_FORCE_CLOSE) {
            final int size = mFloatingViewList.size();
            for (int i = 0; i < size; i++) {
                final FloatingView floatingView = mFloatingViewList.get(i);
                floatingView.setDraggable(false);
            }
        }
    }

    /**
     * Unlock the FloatingView Touch Lock
     */
    @Override
    public void onTrashAnimationEnd(@TrashView.AnimationState int animationCode) {

        final int state = mTargetFloatingView.getState();
        // Delete Views When Exit
        if (state == FloatingView.STATE_FINISHING) {
            removeViewToWindow(mTargetFloatingView);
        }

        // Restore all FloatingView Touch States
        final int size = mFloatingViewList.size();
        for (int i = 0; i < size; i++) {
            final FloatingView floatingView = mFloatingViewList.get(i);
            floatingView.setDraggable(true);
        }

    }

    /**
     * Process the display/hide of the delete button.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int action = event.getAction();

        // Do nothing if you don't have permission to move while you're not in a pressed state.
        // (Responds to the phenomenon that an ACTION_MOVE arrives shortly after rotation and the FloatingView disappears)

        if (action != MotionEvent.ACTION_DOWN && !mIsMoveAccept) {
            return false;
        }

        final int state = mTargetFloatingView.getState();
        mTargetFloatingView = (FloatingView) v;

        // depression
        if (action == MotionEvent.ACTION_DOWN) {
            // No action
            mIsMoveAccept = true;
        }
        // migration
        else if (action == MotionEvent.ACTION_MOVE) {
            // current state
            final boolean isIntersecting = isIntersectWithTrash();
            // state of affairs
            final boolean isIntersect = state == FloatingView.STATE_INTERSECTING;
            // If so, FloatingView follows TrashView
            if (isIntersecting) {
                mTargetFloatingView.setIntersecting((int) mTrashView.getTrashIconCenterX(), (int) mTrashView.getTrashIconCenterY());
            }
            // in the case of beginning to overlap
            if (isIntersecting && !isIntersect) {
                mTargetFloatingView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                mTrashView.setScaleTrashIcon(true);
            }
            // at the end of the overlap
            else if (!isIntersecting && isIntersect) {
                mTargetFloatingView.setNormal();
                mTrashView.setScaleTrashIcon(false);
            }

        }
        // push up, cancel
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            // overlapping case
            if (state == FloatingView.STATE_INTERSECTING) {
            // Delete FloatingView and remove expansion
                mTargetFloatingView.setFinishing();
                mTrashView.setScaleTrashIcon(false);
            }
            mIsMoveAccept = false;

            // Touch finish callback
            if (mFloatingViewListener != null) {
                final boolean isFinishing = mTargetFloatingView.getState() == FloatingView.STATE_FINISHING;
                final WindowManager.LayoutParams params = mTargetFloatingView.getWindowLayoutParams();
                mFloatingViewListener.onTouchFinished(isFinishing, params.x, params.y);
            }
        }

        // Notify the event to the TrashView
        // Pass finger position in normal state
        // Pass the TrashView location if it overlaps
        if (state == FloatingView.STATE_INTERSECTING) {
            mTrashView.onTouchFloatingView(event, mFloatingViewRect.left, mFloatingViewRect.top);
        } else {
            final WindowManager.LayoutParams params = mTargetFloatingView.getWindowLayoutParams();
            mTrashView.onTouchFloatingView(event, params.x, params.y);
        }

        return false;
    }

    /**
     * Set the fixed delete icon image
     *
     * @param resId drawable ID
     */
    public void setFixedTrashIconImage(@DrawableRes int resId) {
        mTrashView.setFixedTrashIconImage(resId);
    }

    /**
     * Set the image of the delete icon you want to act on
     *
     * @param resId drawable ID
     */
    public void setActionTrashIconImage(@DrawableRes int resId) {
        mTrashView.setActionTrashIconImage(resId);
    }


    /**
     * Change the Display Mode
     *
     * @param displayMode {@link #DISPLAY_MODE_SHOW_ALWAYS} or {@link #DISPLAY_MODE_HIDE_ALWAYS} or {@link #DISPLAY_MODE_HIDE_FULLSCREEN}
     */
    public void setDisplayMode(@DisplayMode int displayMode) {
        mDisplayMode = displayMode;
        // For modes that always show/hide at full screen
        if (mDisplayMode == DISPLAY_MODE_SHOW_ALWAYS || mDisplayMode == DISPLAY_MODE_HIDE_FULLSCREEN) {
            for (FloatingView floatingView : mFloatingViewList) {
                floatingView.setVisibility(View.VISIBLE);
            }
        }
        // Always Hidden Mode
        else if (mDisplayMode == DISPLAY_MODE_HIDE_ALWAYS) {
            for (FloatingView floatingView : mFloatingViewList) {
                floatingView.setVisibility(View.GONE);
            }
            mTrashView.dismiss();
        }
    }



    /**
     * Set the DisplayCutout's safe area
     * Note:You must set the Cutout obtained on portrait orientation.
     *
     * @param safeInsetRect DisplayCutout#getSafeInsetXXX
     */
    public void setSafeInsetRect(Rect safeInsetRect) {
        if (safeInsetRect == null) {
            mSafeInsetRect.setEmpty();
        } else {
            mSafeInsetRect.set(safeInsetRect);
        }

        final int size = mFloatingViewList.size();
        if (size == 0) {
            return;
        }

        // update floating view
        for (int i = 0; i < size; i++) {
            final FloatingView floatingView = mFloatingViewList.get(i);
            floatingView.setSafeInsetRect(mSafeInsetRect);
        }
        // dirty hack
        mFullscreenObserverView.onGlobalLayout();
    }

    /**
     * Paste View into Window。
     *
     * @param view    Views to Float
     * @param options Options
     */
    public void addViewToWindow(View view, Options options) {
        final boolean isFirstAttach = mFloatingViewList.isEmpty();
        // FloatingView
        final FloatingView floatingView = new FloatingView(mContext);
        floatingView.setInitCoords(options.floatingViewX, options.floatingViewY);
        floatingView.setOnTouchListener(this);
        floatingView.setOverMargin(options.overMargin);
        floatingView.setMoveDirection(options.moveDirection);
        floatingView.usePhysics(options.usePhysics);
        floatingView.setAnimateInitialMove(options.animateInitialMove);
        floatingView.setSafeInsetRect(mSafeInsetRect);

        // set FloatingView size
        final FrameLayout.LayoutParams targetParams = new FrameLayout.LayoutParams(options.floatingViewWidth, options.floatingViewHeight);
        view.setLayoutParams(targetParams);
        floatingView.addView(view);

        // in hidden mode
        if (mDisplayMode == DISPLAY_MODE_HIDE_ALWAYS) {
            floatingView.setVisibility(View.GONE);
        }
        mFloatingViewList.add(floatingView);
        // TrashView
        mTrashView.setTrashViewListener(this);

        // Paste View
        mWindowManager.addView(floatingView, floatingView.getWindowLayoutParams());
        // Paste Full Screen Monitor View and Delete View only when first pasting
        if (isFirstAttach) {
            mWindowManager.addView(mFullscreenObserverView, mFullscreenObserverView.getWindowLayoutParams());
            mTargetFloatingView = floatingView;
        } else {
            removeViewImmediate(mTrashView);
        }
        // I want you to come to the top, so I paste it every time.
        mWindowManager.addView(mTrashView, mTrashView.getWindowLayoutParams());
    }

    /**
     * Remove View from Window.
     *
     * @param floatingView FloatingView
     */
    private void removeViewToWindow(FloatingView floatingView) {
        final int matchIndex = mFloatingViewList.indexOf(floatingView);
        // Remove from view and list if found
        if (matchIndex != -1) {
            removeViewImmediate(floatingView);
            mFloatingViewList.remove(matchIndex);
        }

        // Check the remaining Views
        if (mFloatingViewList.isEmpty()) {
            // Notification of termination
            if (mFloatingViewListener != null) {
                mFloatingViewListener.onFinishFloatingView();
            }
        }
    }

    /**
     * Remove All Views from Window
     */
    public void removeAllViewToWindow() {
        removeViewImmediate(mFullscreenObserverView);
        removeViewImmediate(mTrashView);
        // Float View View the deletion of the
        final int size = mFloatingViewList.size();
        for (int i = 0; i < size; i++) {
            final FloatingView floatingView = mFloatingViewList.get(i);
            removeViewImmediate(floatingView);
        }
        mFloatingViewList.clear();
    }

    /**
     * Safely remove the View (issue #89)
     *
     * @param view {@link View}
     */
    private void removeViewImmediate(View view) {
        // fix #100(crashes on Android 8)
        try {
            mWindowManager.removeViewImmediate(view);
        } catch (IllegalArgumentException e) {
            //do nothing
        }
    }

    /**
     * Find the safe area of DisplayCutout.
     *
     * @param activity {@link Activity} (Portrait and `windowLayoutInDisplayCutoutMode` != never)
     * @return Safe cutout insets.
     */
    public static Rect findCutoutSafeArea(@androidx.annotation.NonNull Activity activity) {
        final Rect safeInsetRect = new Rect();
        // TODO:Rewrite with android-x
        // TODO:Consider alternatives
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return safeInsetRect;
        }

        // Fix: getDisplayCutout() on a null object reference (issue #110)
        final WindowInsets windowInsets = activity.getWindow().getDecorView().getRootWindowInsets();
        if (windowInsets == null) {
            return safeInsetRect;
        }

        // set safeInsetRect
        final DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        if (displayCutout != null) {
            safeInsetRect.set(displayCutout.getSafeInsetLeft(), displayCutout.getSafeInsetTop(), displayCutout.getSafeInsetRight(), displayCutout.getSafeInsetBottom());
        }

        return safeInsetRect;
    }

    /**
     * Classes that represent options for pasting FloatingView.
     */
    public static class Options {

        /**
         * out-of-screen projection margin(px)
         */
        public int overMargin;

        /**
         * X-coordinate of FloatingView from the bottom left corner of the screen
         */
        public int floatingViewX;

        /**
         * Y-coordinate of FloatingView from the bottom left corner of the screen
         */
        public int floatingViewY;

        /**
         * Width of FloatingView(px)
         */
        public int floatingViewWidth;

        /**
         * Height of FloatingView(px)
         */
        public int floatingViewHeight;

        /**
         * FloatingView Attracts
         * If you specify coordinates, they will automatically be MOVE_DIRECTATION_NONE.
         */
        @MoveDirection
        public int moveDirection;

        /**
         * Use of physics-based animations or (default) ValueAnimation
         */
        public boolean usePhysics;

        /**
         * Flags to animate at initial display
         */
        public boolean animateInitialMove;

        /**
         * Set optional default values
         */
        public Options() {
            overMargin = 0;
            floatingViewX = FloatingView.DEFAULT_X;
            floatingViewY = FloatingView.DEFAULT_Y;
            floatingViewWidth = FloatingView.DEFAULT_WIDTH;
            floatingViewHeight = FloatingView.DEFAULT_HEIGHT;
            moveDirection = MOVE_DIRECTION_DEFAULT;
            usePhysics = true;
            animateInitialMove = true;
        }

    }

}
