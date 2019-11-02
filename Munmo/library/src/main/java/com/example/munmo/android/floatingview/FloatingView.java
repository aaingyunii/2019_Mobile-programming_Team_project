/**
 * Copyright 2015 RECRUIT LIFESTYLE CO., LTD.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.munmo.android.floatingview;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.animation.FloatValueHolder;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * Classes that represent floating views。
 * http://stackoverflow.com/questions/18503050/how-to-create-draggabble-system-alert-in-android
 * FIXME: Nexus5 + YouTube app comes out in front of the navigation bar */
class FloatingView extends FrameLayout implements ViewTreeObserver.OnPreDrawListener {

    /**
     Pressing Scaling Rate
     */
    private static final float SCALE_PRESSED = 0.9f;

    /**
     * normal expansion rate
     */
    private static final float SCALE_NORMAL = 1.0f;

    /**
     * end-of-screen animation time
     */
    private static final long MOVE_TO_EDGE_DURATION = 450L;

    /**
     * edge-motion animation coefficient
     */
    private static final float MOVE_TO_EDGE_OVERSHOOT_TENSION = 1.25f;

    /**
     * Damping ratio constant for spring animation (X coordinate)
     */
    private static final float ANIMATION_SPRING_X_DAMPING_RATIO = 0.7f;

    /**
     * Stiffness constant for spring animation (X coordinate)
     */
    private static final float ANIMATION_SPRING_X_STIFFNESS = 350f;

    /**
     * Friction constant for fling animation (X coordinate)
     */
    private static final float ANIMATION_FLING_X_FRICTION = 1.7f;

    /**
     * Friction constant for fling animation (Y coordinate)
     */
    private static final float ANIMATION_FLING_Y_FRICTION = 1.7f;

    /**
     * Current velocity units
     */
    private static final int CURRENT_VELOCITY_UNITS = 1000;

    /**
     * normal state
     */
    static final int STATE_NORMAL = 0;

    /**
     * overlapping state
     */
    static final int STATE_INTERSECTING = 1;

    /**
     * termination state
     */
    static final int STATE_FINISHING = 2;

    /**
     * AnimationState
     */
    @IntDef({STATE_NORMAL, STATE_INTERSECTING, STATE_FINISHING})
    @Retention(RetentionPolicy.SOURCE)
    @interface AnimationState {
    }

    /**
     * Time to make a long press decision
     * (one and a half times more than usual considering moving operation)
     */
    private static final int LONG_PRESS_TIMEOUT = (int) (1.5f * ViewConfiguration.getLongPressTimeout());

    /**
     * Constant for scaling down X coordinate velocity
     */
    private static final float MAX_X_VELOCITY_SCALE_DOWN_VALUE = 9;

    /**
     * Constant for scaling down Y coordinate velocity
     */
    private static final float MAX_Y_VELOCITY_SCALE_DOWN_VALUE = 8;

    /**
     * Constant for calculating the threshold to move when throwing
     */
    private static final float THROW_THRESHOLD_SCALE_DOWN_VALUE = 9;

    /**
     * Default X-coordinate value
     */
    static final int DEFAULT_X = Integer.MIN_VALUE;

    /**
     * Default Y-coordinate value
     */
    static final int DEFAULT_Y = Integer.MIN_VALUE;

    /**
     * Default width size
     */
    static final int DEFAULT_WIDTH = ViewGroup.LayoutParams.WRAP_CONTENT;

    /**
     * Default height size
     */
    static final int DEFAULT_HEIGHT = ViewGroup.LayoutParams.WRAP_CONTENT;

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
     * VelocityTracker
     */
    private VelocityTracker mVelocityTracker;

    /**
     * {@link ViewConfiguration}
     */
    private ViewConfiguration mViewConfiguration;

    /**
     * Minimum threshold required for movement(px)
     */
    private float mMoveThreshold;

    /**
     * Maximum fling velocity
     */
    private float mMaximumFlingVelocity;

    /**
     * Maximum x coordinate velocity
     */
    private float mMaximumXVelocity;

    /**
     * Maximum x coordinate velocity
     */
    private float mMaximumYVelocity;

    /**
     * Threshold to move when throwing
     */
    private float mThrowMoveThreshold;

    /**
     * DisplayMetrics
     */
    private final DisplayMetrics mMetrics;

    /**
     * Time to check if the press process has passed
     */
    private long mTouchDownTime;

    /**
     * Screen press X coordinates (for determining the amount of movement)
     */
    private float mScreenTouchDownX;
    /**
     * Screen press Y coordinates (for determining the amount of movement)
     */
    private float mScreenTouchDownY;
    /**
     * a flag that has started moving
     */
    private boolean mIsMoveAccept;

    /**
     * screen touch X coordinates
     */
    private float mScreenTouchX;
    /**
     * screen touch Y coordinates
     */
    private float mScreenTouchY;
    /**
     * Local Touch X-coordinate
     */
    private float mLocalTouchX;
    /**
     * Local Touch Y-coordinate
     */
    private float mLocalTouchY;
    /**
     * initial X-coordinate
     */
    private int mInitX;
    /**
     * initial Y-coordinate
     */
    private int mInitY;

    /**
     * Initial animation running flag
     */
    private boolean mIsInitialAnimationRunning;

    /**
     * Flag to animate on initial display
     */
    private boolean mAnimateInitialMove;

    /**
     * status bar's height
     */
    private final int mBaseStatusBarHeight;

    /**
     * status bar's height(landscape)
     */
    private final int mBaseStatusBarRotatedHeight;

    /**
     * Current status bar's height
     */
    private int mStatusBarHeight;

    /**
     * Navigation bar's height(portrait)
     */
    private final int mBaseNavigationBarHeight;

    /**
     * Navigation bar's height
     * Placed bottom on the screen(tablet)
     * Or placed vertically on the screen(phone)
     */
    private final int mBaseNavigationBarRotatedHeight;

    /**
     * Current Navigation bar's vertical size
     */
    private int mNavigationBarVerticalOffset;

    /**
     * Current Navigation bar's horizontal size
     */
    private int mNavigationBarHorizontalOffset;

    /**
     * Offset of touch X coordinate
     */
    private int mTouchXOffset;

    /**
     * Offset of touch Y coordinate
     */
    private int mTouchYOffset;

    /**
     * Animations to the left and right edges
     */
    private ValueAnimator mMoveEdgeAnimator;

    /**
     * Interpolator
     */
    private final TimeInterpolator mMoveEdgeInterpolator;

    /**
     * Rect representing travel limit
     */
    private final Rect mMoveLimitRect;

    /**
     * Rect representing the limit of the display position (screen edge)     */
    private final Rect mPositionLimitRect;

    /**
     * Draggable flag
     */
    private boolean mIsDraggable;

    /**
     * coefficient representing the shape of
     */
    private float mShape;

    /**
     * HANDLER FOR ANIMATION OF FLOATINGView
     */
    private final FloatingAnimationHandler mAnimationHandler;

     /**
      * Handler to determine long press
      */
    private final LongPressHandler mLongPressHandler;

    /**
      * Margin over the screen edge
      */
        private int mOverMargin;

        /**
      * OnTouchListener
      */
        private OnTouchListener mOnTouchListener;

    /**
      * Long press
      */
         private boolean mIsLongPressed;

     /**
     * Direction of movement
      */
         private int mMoveDirection;

    /**
       * Use dynamic physics-based animations or not
      */
        private boolean mUsePhysics;

     /**
      * If true, it's a tablet.If false, it's a phone
      */
         private final boolean mIsTablet;

    /**
              * Surface.ROTATION_XXX
      */
        private int mRotation;

        /**
     * Cutout safe inset rect (Same as FloatingViewManager's mSafeInsetRect)
     * */
    private final Rect mSafeInsetRect;

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
     * @param context {@link android.content.Context}
     */
    FloatingView(final Context context) {
        super(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        mMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        mParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.type = OVERLAY_TYPE;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        mParams.format = PixelFormat.TRANSLUCENT;
        // Set the lower left coordinate to 0
        mParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        mAnimationHandler = new FloatingAnimationHandler(this);
        mLongPressHandler = new LongPressHandler(this);
        mMoveEdgeInterpolator = new OvershootInterpolator(MOVE_TO_EDGE_OVERSHOOT_TENSION);
        mMoveDirection = FloatingViewManager.MOVE_DIRECTION_DEFAULT;
        mUsePhysics = false;
        final Resources resources = context.getResources();
        mIsTablet = (resources.getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        mRotation = mWindowManager.getDefaultDisplay().getRotation();

        mMoveLimitRect = new Rect();
        mPositionLimitRect = new Rect();
        mSafeInsetRect = new Rect();

        // Get status bar height
        mBaseStatusBarHeight = getSystemUiDimensionPixelSize(resources, "status_bar_height");
        // Check landscape resource id
        final int statusBarLandscapeResId = resources.getIdentifier("status_bar_height_landscape", "dimen", "android");
        if (statusBarLandscapeResId > 0) {
            mBaseStatusBarRotatedHeight = getSystemUiDimensionPixelSize(resources, "status_bar_height_landscape");
        } else {
            mBaseStatusBarRotatedHeight = mBaseStatusBarHeight;
        }

        // Init physics-based animation properties
        updateViewConfiguration();

        // Detect NavigationBar
        if (hasSoftNavigationBar()) {
            mBaseNavigationBarHeight = getSystemUiDimensionPixelSize(resources, "navigation_bar_height");
            final String resName = mIsTablet ? "navigation_bar_height_landscape" : "navigation_bar_width";
            mBaseNavigationBarRotatedHeight = getSystemUiDimensionPixelSize(resources, resName);
        } else {
            mBaseNavigationBarHeight = 0;
            mBaseNavigationBarRotatedHeight = 0;
        }

        // For the first drawing process
        getViewTreeObserver().addOnPreDrawListener(this);
    }

    /**
     * Check if there is a software navigation bar(including the navigation bar in the screen).
     *
     * @return True if there is a software navigation bar
     */
    private boolean hasSoftNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getRealMetrics(realDisplayMetrics);
            return realDisplayMetrics.heightPixels > mMetrics.heightPixels || realDisplayMetrics.widthPixels > mMetrics.widthPixels;
        }

        // old device check flow
        // Navigation bar exists (config_showNavigationBar is true, or both the menu key and the back key are not exists)
        final Context context = getContext();
        final Resources resources = context.getResources();
        final boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        final boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        final int showNavigationBarResId = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        final boolean hasNavigationBarConfig = showNavigationBarResId != 0 && resources.getBoolean(showNavigationBarResId);
        return hasNavigationBarConfig || (!hasMenuKey && !hasBackKey);
    }

    /**
     * Get the System ui dimension(pixel)
     *
     * @param resources {@link Resources}
     * @param resName   dimension resource name
     * @return pixel size
     */
    private static int getSystemUiDimensionPixelSize(Resources resources, String resName) {
        int pixelSize = 0;
        final int resId = resources.getIdentifier(resName, "dimen", "android");
        if (resId > 0) {
            pixelSize = resources.getDimensionPixelSize(resId);
        }
        return pixelSize;
    }


    /**
     * Determine the display position。
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        refreshLimitRect();
    }

    /**
     * Adjust the layout when rotating the screen。
     */
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateViewConfiguration();
        refreshLimitRect();
    }

    /**
     * Set the coordinates for the first drawing。
     */
    @Override
    public boolean onPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);
        // Enter the default value if the initial value is set in the X coordinate (margin is not considered)
        if (mInitX == DEFAULT_X) {
            mInitX = 0;
        }
        // Enter the default value if an initial value is set for the Y coordinate
        if (mInitY == DEFAULT_Y) {
            mInitY = mMetrics.heightPixels - mStatusBarHeight - getMeasuredHeight();
        }

        // Set initial position
        mParams.x = mInitX;
        mParams.y = mInitY;

        // If not moving to the screen edge, move to specified coordinates
        if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_NONE) {
            moveTo(mInitX, mInitY, mInitX, mInitY, false);
        } else {
            mIsInitialAnimationRunning = true;
            // Move from initial position to screen edge
            moveToEdge(mInitX, mInitY, mAnimateInitialMove);
        }
        mIsDraggable = true;
        updateViewLayout();
        return true;
    }

    /**
     * Called when the layout of the system has changed.
     *
     * @param isHideStatusBar     If true, the status bar is hidden
     * @param isHideNavigationBar If true, the navigation bar is hidden
     * @param isPortrait          If true, the device orientation is portrait
     * @param windowRect          {@link Rect} of system window
     */

    void onUpdateSystemLayout(boolean isHideStatusBar, boolean isHideNavigationBar, boolean isPortrait, Rect windowRect) {
        // status bar
        updateStatusBarHeight(isHideStatusBar, isPortrait);
        // touch X offset(support Cutout)
        updateTouchXOffset(isHideNavigationBar, windowRect.left);
        // touch Y offset(support Cutout)
        mTouchYOffset = isPortrait ? mSafeInsetRect.top : 0;
        // navigation bar
        updateNavigationBarOffset(isHideNavigationBar, isPortrait, windowRect);
        refreshLimitRect();
    }

    /**
     * Update height of StatusBar.
     *
     * @param isHideStatusBar If true, the status bar is hidden
     * @param isPortrait      If true, the device orientation is portrait
     */
    private void updateStatusBarHeight(boolean isHideStatusBar, boolean isPortrait) {
        if (isHideStatusBar) {
            // 1.(No Cutout)No StatusBar(=0)
            // 2.(Has Cutout)StatusBar is not included in mMetrics.heightPixels (=0)
            mStatusBarHeight = 0;
            return;
        }

        // Has Cutout
        final boolean hasTopCutout = mSafeInsetRect.top != 0;
        if (hasTopCutout) {
            if (isPortrait) {
                mStatusBarHeight = 0;
            } else {
                mStatusBarHeight = mBaseStatusBarRotatedHeight;
            }
            return;
        }

        // No cutout
        if (isPortrait) {
            mStatusBarHeight = mBaseStatusBarHeight;
        } else {
            mStatusBarHeight = mBaseStatusBarRotatedHeight;
        }
    }

    /**
     * Update of touch X coordinate
     *
     * @param isHideNavigationBar If true, the navigation bar is hidden
     * @param windowLeftOffset    Left side offset of device display
     */
    private void updateTouchXOffset(boolean isHideNavigationBar, int windowLeftOffset) {
        final boolean hasBottomCutout = mSafeInsetRect.bottom != 0;
        if (hasBottomCutout) {
            mTouchXOffset = windowLeftOffset;
            return;
        }

        // No cutout
        // touch X offset(navigation bar is displayed and it is on the left side of the device)
        mTouchXOffset = !isHideNavigationBar && windowLeftOffset > 0 ? mBaseNavigationBarRotatedHeight : 0;
    }

    /**
     * Update offset of NavigationBar.
     *
     * @param isHideNavigationBar If true, the navigation bar is hidden
     * @param isPortrait          If true, the device orientation is portrait
     * @param windowRect          {@link Rect} of system window
     */
    private void updateNavigationBarOffset(boolean isHideNavigationBar, boolean isPortrait, Rect windowRect) {
        int currentNavigationBarHeight = 0;
        int currentNavigationBarWidth = 0;
        int navigationBarVerticalDiff = 0;
        final boolean hasSoftNavigationBar = hasSoftNavigationBar();

        // auto hide navigation bar(Galaxy S8, S9 and so on.)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getRealMetrics(realDisplayMetrics);
            currentNavigationBarHeight = realDisplayMetrics.heightPixels - windowRect.bottom;
            currentNavigationBarWidth = realDisplayMetrics.widthPixels - mMetrics.widthPixels;
            navigationBarVerticalDiff = mBaseNavigationBarHeight - currentNavigationBarHeight;
        }

        if (!isHideNavigationBar) {
            // auto hide navigation bar
            // Estimate based on conflicts with other devices
            // 1. The navigation bar (mBaseNavigationBarHeight = = 0) built into
            // the device does not vary in height depending on the state of the system.
            // 2. The navigation bar built into the device.(!hasSoftNavigationBar)
            // It is contradictory because deliberately zeroes the base.
            if (navigationBarVerticalDiff != 0 && mBaseNavigationBarHeight == 0 ||
                    !hasSoftNavigationBar && mBaseNavigationBarHeight != 0) {
                if (hasSoftNavigationBar) {
                    // 1.auto hide mode -> show mode
                    // 2.show mode -> auto hide mode -> home
                    mNavigationBarVerticalOffset = 0;
                } else {
                    // show mode -> home
                    mNavigationBarVerticalOffset = -currentNavigationBarHeight;
                }
            } else {
                // normal device
                mNavigationBarVerticalOffset = 0;
            }

            mNavigationBarHorizontalOffset = 0;
            return;
        }

        // If the portrait, is displayed at the bottom of the screen
        if (isPortrait) {
            // auto hide navigation bar
            if (!hasSoftNavigationBar && mBaseNavigationBarHeight != 0) {
                mNavigationBarVerticalOffset = 0;
            } else {
                mNavigationBarVerticalOffset = mBaseNavigationBarHeight;
            }
            mNavigationBarHorizontalOffset = 0;
            return;
        }

        // If it is a Tablet, it will appear at the bottom of the screen.
        // If it is Phone, it will appear on the side of the screen
        if (mIsTablet) {
            mNavigationBarVerticalOffset = mBaseNavigationBarRotatedHeight;
            mNavigationBarHorizontalOffset = 0;
        } else {
            mNavigationBarVerticalOffset = 0;
            // auto hide navigation bar
            // guess from inconsistencies with other devices
            // 1.navigation bar built into the device(!hasSoftNavigationBar)is contradictory because deliberately zeroes the base.
            if (!hasSoftNavigationBar && mBaseNavigationBarRotatedHeight != 0) {
                mNavigationBarHorizontalOffset = 0;
            } else if (hasSoftNavigationBar && mBaseNavigationBarRotatedHeight == 0) {
                // 2.Soft Navigation Bar, Conflicting due to Base Configuration
                mNavigationBarHorizontalOffset = currentNavigationBarWidth;
            } else {
                mNavigationBarHorizontalOffset = mBaseNavigationBarRotatedHeight;
            }
        }
    }

    /**
     * Update {@link ViewConfiguration}
     */
    private void updateViewConfiguration() {
        mViewConfiguration = ViewConfiguration.get(getContext());
        mMoveThreshold = mViewConfiguration.getScaledTouchSlop();
        mMaximumFlingVelocity = mViewConfiguration.getScaledMaximumFlingVelocity();
        mMaximumXVelocity = mMaximumFlingVelocity / MAX_X_VELOCITY_SCALE_DOWN_VALUE;
        mMaximumYVelocity = mMaximumFlingVelocity / MAX_Y_VELOCITY_SCALE_DOWN_VALUE;
        mThrowMoveThreshold = mMaximumFlingVelocity / THROW_THRESHOLD_SCALE_DOWN_VALUE;
    }

    /**
     * Update the PositionLimitRect and MoveLimitRect according to the screen size change.
     */
    private void refreshLimitRect() {
        cancelAnimation();

        // Save previous screen coordinates
        final int oldPositionLimitWidth = mPositionLimitRect.width();
        final int oldPositionLimitHeight = mPositionLimitRect.height();

        // Switch to new coordinate information
        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final int newScreenWidth = mMetrics.widthPixels;
        final int newScreenHeight = mMetrics.heightPixels;

        // Configuring the Range of Movements
        mMoveLimitRect.set(-width, -height * 2, newScreenWidth + width + mNavigationBarHorizontalOffset, newScreenHeight + height + mNavigationBarVerticalOffset);
        mPositionLimitRect.set(-mOverMargin, 0, newScreenWidth - width + mOverMargin + mNavigationBarHorizontalOffset, newScreenHeight - mStatusBarHeight - height + mNavigationBarVerticalOffset);

        // Initial animation stop when the device rotates
        final int newRotation = mWindowManager.getDefaultDisplay().getRotation();
        if (mAnimateInitialMove && mRotation != newRotation) {
            mIsInitialAnimationRunning = false;
        }

        // When animation is running and the device is not rotating
        if (mIsInitialAnimationRunning && mRotation == newRotation) {
            moveToEdge(mParams.x, mParams.y, true);
        } else {
            // If there is a screen change during the operation, move to the appropriate position
            if (mIsMoveAccept) {
                moveToEdge(mParams.x, mParams.y, false);
            } else {
                final int newX = (int) (mParams.x * mPositionLimitRect.width() / (float) oldPositionLimitWidth + 0.5f);
                final int goalPositionX = Math.min(Math.max(mPositionLimitRect.left, newX), mPositionLimitRect.right);
                final int newY = (int) (mParams.y * mPositionLimitRect.height() / (float) oldPositionLimitHeight + 0.5f);
                final int goalPositionY = Math.min(Math.max(mPositionLimitRect.top, newY), mPositionLimitRect.bottom);
                moveTo(mParams.x, mParams.y, goalPositionX, goalPositionY, false);
            }
        }
        mRotation = newRotation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDetachedFromWindow() {
        if (mMoveEdgeAnimator != null) {
            mMoveEdgeAnimator.removeAllUpdateListeners();
        }
        super.onDetachedFromWindow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        // If View is not displayed, do nothing
        if (getVisibility() != View.VISIBLE) {
            return true;
        }

        // Do nothing if you can't touch it
        if (!mIsDraggable) {
            return true;
        }

        // Block while initial display animation is running
        if (mIsInitialAnimationRunning) {
            return true;
        }

        // Current Location Cache
        mScreenTouchX = event.getRawX();
        mScreenTouchY = event.getRawY();
        final int action = event.getAction();
        boolean isWaitForMoveToEdge = false;
        // depression
        if (action == MotionEvent.ACTION_DOWN) {
            // Cancel Animation
            cancelAnimation();
            mScreenTouchDownX = mScreenTouchX;
            mScreenTouchDownY = mScreenTouchY;
            mLocalTouchX = event.getX();
            mLocalTouchY = event.getY();
            mIsMoveAccept = false;
            setScale(SCALE_PRESSED);

            if (mVelocityTracker == null) {
                // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                mVelocityTracker = VelocityTracker.obtain();
            } else {
                // Reset the velocity tracker back to its initial state.
                mVelocityTracker.clear();
            }

            // Start Touch Tracking Animation
            mAnimationHandler.updateTouchPosition(getXByTouch(), getYByTouch());
            mAnimationHandler.removeMessages(FloatingAnimationHandler.ANIMATION_IN_TOUCH);
            mAnimationHandler.sendAnimationMessage(FloatingAnimationHandler.ANIMATION_IN_TOUCH);
            // Starting Long-Press Determination
            mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED);
            mLongPressHandler.sendEmptyMessageDelayed(LongPressHandler.LONG_PRESSED, LONG_PRESS_TIMEOUT);
            // RETENTION OF TIME FOR DETERMINING PASSING OF PUSH-DOWN PROCESSING
            // To prevent MOVE or the like from being processed when the flag of mIsDragable or
            // getVisibility() is changed after depression.
            mTouchDownTime = event.getDownTime();
            // compute offset and restore
            addMovement(event);
            mIsInitialAnimationRunning = false;
        }
        // migration
        else if (action == MotionEvent.ACTION_MOVE) {
            // Cancel Long-Press in the case of decision of movement
            if (mIsMoveAccept) {
                mIsLongPressed = false;
                mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED);
            }
            // Do not process if pressing process is not performed
            if (mTouchDownTime != event.getDownTime()) {
                return true;
            }
            // When the movement is not accepted and both the X and Y axes are smaller than the threshold value
            if (!mIsMoveAccept && Math.abs(mScreenTouchX - mScreenTouchDownX) < mMoveThreshold &&
                    Math.abs(mScreenTouchY - mScreenTouchDownY) < mMoveThreshold) {
                return true;
            }
            mIsMoveAccept = true;
            mAnimationHandler.updateTouchPosition(getXByTouch(), getYByTouch());
            // compute offset and restore
            addMovement(event);
        }
        // Push-up, cancel
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            // compute velocity tracker
            if (mVelocityTracker != null) {
                mVelocityTracker.computeCurrentVelocity(CURRENT_VELOCITY_UNITS);
            }

            // Holds the long press state temporarily for judgment
            final boolean tmpIsLongPressed = mIsLongPressed;
            // Release long press
            mIsLongPressed = false;
            mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED);
            // Do not process if pressing process is not performed
            if (mTouchDownTime != event.getDownTime()) {
                return true;
            }
            // Delete animation
            mAnimationHandler.removeMessages(FloatingAnimationHandler.ANIMATION_IN_TOUCH);
            // Restore magnification
            setScale(SCALE_NORMAL);

            // destroy VelocityTracker (#103)
            if (!mIsMoveAccept && mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }

            // When ACTION_UP is done (when not pressed or moved)
            if (action == MotionEvent.ACTION_UP && !tmpIsLongPressed && !mIsMoveAccept) {
                final int size = getChildCount();
                for (int i = 0; i < size; i++) {
                    getChildAt(i).performClick();
                }
            } else {
                // Make a move after checking whether it is finished or not
                isWaitForMoveToEdge = true;
            }
        }

        // Notify touch listener
        if (mOnTouchListener != null) {
            mOnTouchListener.onTouch(this, event);
        }

        // Lazy execution of moveToEdge
        if (isWaitForMoveToEdge && mAnimationHandler.getState() != STATE_FINISHING) {
            // include device rotation
            moveToEdge(true);
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
        }

        return true;
    }

    /**
     * Call addMovement and restore MotionEvent coordinate
     *
     * @param event {@link MotionEvent}
     */
    private void addMovement(@NonNull MotionEvent event) {
        final float deltaX = event.getRawX() - event.getX();
        final float deltaY = event.getRawY() - event.getY();
        event.offsetLocation(deltaX, deltaY);
        mVelocityTracker.addMovement(event);
        event.offsetLocation(-deltaX, -deltaY);
    }

    /**
     * Processing when long-presse.
     */
    private void onLongClick() {
        mIsLongPressed = true;
        // long-press treatment
        final int size = getChildCount();
        for (int i = 0; i < size; i++) {
            getChildAt(i).performLongClick();
        }
    }

    /**
     * Represents the process for erasing from the screen.
     */
    @Override
    public void setVisibility(int visibility) {
        // When the screen is displayed
        if (visibility != View.VISIBLE) {
            // When erasing from the screen, cancel the long press and move to the edge of the screen.
            cancelLongPress();
            setScale(SCALE_NORMAL);
            if (mIsMoveAccept) {
                moveToEdge(false);
            }
            mAnimationHandler.removeMessages(FloatingAnimationHandler.ANIMATION_IN_TOUCH);
            mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED);
        }
        super.setVisibility(visibility);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    /**
     * Move to the left and right edges
     *
     * @param withAnimation true to animate, false otherwise
     * */

    private void moveToEdge(boolean withAnimation) {
        final int currentX = getXByTouch();
        final int currentY = getYByTouch();
        moveToEdge(currentX, currentY, withAnimation);
    }

    /**
     * Specify the start point and move to the left and right edges。
     *
     * @param startX The initial value of the X coordinate
     * @param startY Y coordinate initial value
     * @param withAnimation true to animate, false otherwise
     */
    private void moveToEdge(int startX, int startY, boolean withAnimation) {
        // Move to specified coordinates

        final int goalPositionX = getGoalPositionX(startX, startY);
        final int goalPositionY = getGoalPositionY(startX, startY);
        moveTo(startX, startY, goalPositionX, goalPositionY, withAnimation);
    }

    /**
     * Move to the specified coordinates. <br/>
       * If it exceeds the coordinates of the screen edge, it will automatically move to the screen edge.
       *
       * @param currentX Current X coordinate (used for animation start)
       * @param currentY Current Y coordinate (used for animation start)
       * @param goalPositionX X coordinate to move to
       * @param goalPositionY Y coordinate to move to
       * @param withAnimation true to animate, false otherwise
     */
    private void moveTo(int currentX, int currentY, int goalPositionX, int goalPositionY, boolean withAnimation) {
        // Adjust so that it does not protrude from the edge of the screen
        goalPositionX = Math.min(Math.max(mPositionLimitRect.left, goalPositionX), mPositionLimitRect.right);
        goalPositionY = Math.min(Math.max(mPositionLimitRect.top, goalPositionY), mPositionLimitRect.bottom);

        // When performing animation
        if (withAnimation) {
            // Use physics animation
            final boolean usePhysicsAnimation = mUsePhysics && mVelocityTracker != null && mMoveDirection != FloatingViewManager.MOVE_DIRECTION_NEAREST;
            if (usePhysicsAnimation) {
                startPhysicsAnimation(goalPositionX, currentY);
            } else {
                startObjectAnimation(currentX, currentY, goalPositionX, goalPositionY);
            }
        } else {
            // Update only when position changes
            if (mParams.x != goalPositionX || mParams.y != goalPositionY) {
                mParams.x = goalPositionX;
                mParams.y = goalPositionY;
                updateViewLayout();
            }
        }
        // Initialize touch coordinates
        mLocalTouchX = 0;
        mLocalTouchY = 0;
        mScreenTouchDownX = 0;
        mScreenTouchDownY = 0;
        mIsMoveAccept = false;
    }

    /**
     * Start Physics-based animation
     *
     * @param goalPositionX goal position X coordinate
     * @param currentY      current Y coordinate
     */
    private void startPhysicsAnimation(int goalPositionX, int currentY) {
        // start X coordinate animation
        final boolean containsLimitRectWidth = mParams.x < mPositionLimitRect.right && mParams.x > mPositionLimitRect.left;
        // If MOVE_DIRECTION_NONE, play fling animation
        if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_NONE && containsLimitRectWidth) {
            final float velocityX = Math.min(Math.max(mVelocityTracker.getXVelocity(), -mMaximumXVelocity), mMaximumXVelocity);
            startFlingAnimationX(velocityX);
        } else {
            startSpringAnimationX(goalPositionX);
        }

        // start Y coordinate animation
        final boolean containsLimitRectHeight = mParams.y < mPositionLimitRect.bottom && mParams.y > mPositionLimitRect.top;
        final float velocityY = -Math.min(Math.max(mVelocityTracker.getYVelocity(), -mMaximumYVelocity), mMaximumYVelocity);
        if (containsLimitRectHeight) {
            startFlingAnimationY(velocityY);
        } else {
            startSpringAnimationY(currentY, velocityY);
        }
    }

    /**
     * Start object animation
     *
     * @param currentX      current X coordinate
     * @param currentY      current Y coordinate
     * @param goalPositionX goal position X coordinate
     * @param goalPositionY goal position Y coordinate
     */
    private void startObjectAnimation(int currentX, int currentY, int goalPositionX, int goalPositionY) {
        if (goalPositionX == currentX) {
            //to move only y coordinate
            mMoveEdgeAnimator = ValueAnimator.ofInt(currentY, goalPositionY);
            mMoveEdgeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mParams.y = (Integer) animation.getAnimatedValue();
                    updateViewLayout();
                    updateInitAnimation(animation);
                }
            });
        } else {
            // To move only x coordinate (to left or right)
            mParams.y = goalPositionY;
            mMoveEdgeAnimator = ValueAnimator.ofInt(currentX, goalPositionX);
            mMoveEdgeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mParams.x = (Integer) animation.getAnimatedValue();
                    updateViewLayout();
                    updateInitAnimation(animation);
                }
            });
        }
        // X axis animation settings
        mMoveEdgeAnimator.setDuration(MOVE_TO_EDGE_DURATION);
        mMoveEdgeAnimator.setInterpolator(mMoveEdgeInterpolator);
        mMoveEdgeAnimator.start();
    }

    /**
     * Start spring animation(X coordinate)
     *
     * @param goalPositionX goal position X coordinate
     */
    private void startSpringAnimationX(int goalPositionX) {
        // springX
        final SpringForce springX = new SpringForce(goalPositionX);
        springX.setDampingRatio(ANIMATION_SPRING_X_DAMPING_RATIO);
        springX.setStiffness(ANIMATION_SPRING_X_STIFFNESS);
        // springAnimation
        final SpringAnimation springAnimationX = new SpringAnimation(new FloatValueHolder());
        springAnimationX.setStartVelocity(mVelocityTracker.getXVelocity());
        springAnimationX.setStartValue(mParams.x);
        springAnimationX.setSpring(springX);
        springAnimationX.setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS);
        springAnimationX.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                final int x = Math.round(value);
                // Not moving, or the touch operation is continuing
                if (mParams.x == x || mVelocityTracker != null) {
                    return;
                }
                // update x coordinate
                mParams.x = x;
                updateViewLayout();
            }
        });
        springAnimationX.start();
    }

    /**
     * Start spring animation(Y coordinate)
     *
     * @param currentY  current Y coordinate
     * @param velocityY velocity Y coordinate
     */
    private void startSpringAnimationY(int currentY, float velocityY) {
        // Create SpringForce
        final SpringForce springY = new SpringForce(currentY < mMetrics.heightPixels / 2 ? mPositionLimitRect.top : mPositionLimitRect.bottom);
        springY.setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
        springY.setStiffness(SpringForce.STIFFNESS_LOW);

        // Create SpringAnimation
        final SpringAnimation springAnimationY = new SpringAnimation(new FloatValueHolder());
        springAnimationY.setStartVelocity(velocityY);
        springAnimationY.setStartValue(mParams.y);
        springAnimationY.setSpring(springY);
        springAnimationY.setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS);
        springAnimationY.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                final int y = Math.round(value);
                // Not moving, or the touch operation is continuing
                if (mParams.y == y || mVelocityTracker != null) {
                    return;
                }
                // update y coordinate
                mParams.y = y;
                updateViewLayout();
            }
        });
        springAnimationY.start();
    }

    /**
     * Start fling animation(X coordinate)
     *
     * @param velocityX velocity X coordinate
     */
    private void startFlingAnimationX(float velocityX) {
        final FlingAnimation flingAnimationX = new FlingAnimation(new FloatValueHolder());
        flingAnimationX.setStartVelocity(velocityX);
        flingAnimationX.setMaxValue(mPositionLimitRect.right);
        flingAnimationX.setMinValue(mPositionLimitRect.left);
        flingAnimationX.setStartValue(mParams.x);
        flingAnimationX.setFriction(ANIMATION_FLING_X_FRICTION);
        flingAnimationX.setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS);
        flingAnimationX.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                final int x = Math.round(value);
                // Not moving, or the touch operation is continuing
                if (mParams.x == x || mVelocityTracker != null) {
                    return;
                }
                // update y coordinate
                mParams.x = x;
                updateViewLayout();
            }
        });
        flingAnimationX.start();
    }

    /**
     * Start fling animation(Y coordinate)
     *
     * @param velocityY velocity Y coordinate
     */
    private void startFlingAnimationY(float velocityY) {
        final FlingAnimation flingAnimationY = new FlingAnimation(new FloatValueHolder());
        flingAnimationY.setStartVelocity(velocityY);
        flingAnimationY.setMaxValue(mPositionLimitRect.bottom);
        flingAnimationY.setMinValue(mPositionLimitRect.top);
        flingAnimationY.setStartValue(mParams.y);
        flingAnimationY.setFriction(ANIMATION_FLING_Y_FRICTION);
        flingAnimationY.setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS);
        flingAnimationY.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                final int y = Math.round(value);
                // Not moving, or the touch operation is continuing
                if (mParams.y == y || mVelocityTracker != null) {
                    return;
                }
                // update y coordinate
                mParams.y = y;
                updateViewLayout();
            }
        });
        flingAnimationY.start();
    }

    /**
     * Check if it is attached to the Window and call WindowManager.updateLayout()
     */
    private void updateViewLayout() {
        if (!ViewCompat.isAttachedToWindow(this)) {
            return;
        }
        mWindowManager.updateViewLayout(this, mParams);
    }

    /**
     * Update animation initialization flag
     *
     * @param animation {@link ValueAnimator}
     */
    private void updateInitAnimation(ValueAnimator animation) {
        if (mAnimateInitialMove && animation.getDuration() <= animation.getCurrentPlayTime()) {
            mIsInitialAnimationRunning = false;
        }
    }

    /**
     * Get the final point of movement (X coordinate)
     *
     * @param startX Initial value of X coordinate
     * @param startY Initial value of Y coordinate
     * @return End point of X coordinate
     */
    private int getGoalPositionX(int startX, int startY) {
        int goalPositionX = startX;

        // Move to left or right edges
        if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_DEFAULT) {
            final boolean isMoveRightEdge = startX > (mMetrics.widthPixels - getWidth()) / 2;
            goalPositionX = isMoveRightEdge ? mPositionLimitRect.right : mPositionLimitRect.left;
        }
        // Move to left edges
        else if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_LEFT) {
            goalPositionX = mPositionLimitRect.left;
        }
        // Move to right edges
        else if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_RIGHT) {
            goalPositionX = mPositionLimitRect.right;
        }
        // Move to top/bottom/left/right edges
        else if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_NEAREST) {
            final int distLeftRight = Math.min(startX, mPositionLimitRect.width() - startX);
            final int distTopBottom = Math.min(startY, mPositionLimitRect.height() - startY);
            if (distLeftRight < distTopBottom) {
                final boolean isMoveRightEdge = startX > (mMetrics.widthPixels - getWidth()) / 2;
                goalPositionX = isMoveRightEdge ? mPositionLimitRect.right : mPositionLimitRect.left;
            }
        }
        // Move in the direction in which it is thrown
        else if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_THROWN) {
            if (mVelocityTracker != null && mVelocityTracker.getXVelocity() > mThrowMoveThreshold) {
                goalPositionX = mPositionLimitRect.right;
            } else if (mVelocityTracker != null && mVelocityTracker.getXVelocity() < -mThrowMoveThreshold) {
                goalPositionX = mPositionLimitRect.left;
            } else {
                final boolean isMoveRightEdge = startX > (mMetrics.widthPixels - getWidth()) / 2;
                goalPositionX = isMoveRightEdge ? mPositionLimitRect.right : mPositionLimitRect.left;
            }
        }

        return goalPositionX;
    }

    /**
     * Get the final point of movement (Y coordinate)
     *
     * @param startX Initial value of X coordinate
     * @param startY Initial value of Y coordinate
     * @return End point of Y coordinate
     */
    private int getGoalPositionY(int startX, int startY) {
        int goalPositionY = startY;

        // Move to top/bottom/left/right edges
        if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_NEAREST) {
            final int distLeftRight = Math.min(startX, mPositionLimitRect.width() - startX);
            final int distTopBottom = Math.min(startY, mPositionLimitRect.height() - startY);
            if (distLeftRight >= distTopBottom) {
                final boolean isMoveTopEdge = startY < (mMetrics.heightPixels - getHeight()) / 2;
                goalPositionY = isMoveTopEdge ? mPositionLimitRect.top : mPositionLimitRect.bottom;
            }
        }

        return goalPositionY;
    }

    /**
     * Cancel animation.
     */
    private void cancelAnimation() {
        if (mMoveEdgeAnimator != null && mMoveEdgeAnimator.isStarted()) {
            mMoveEdgeAnimator.cancel();
            mMoveEdgeAnimator = null;
        }
    }

    /**
     * Enlarge / reduce.
     *
     * @param newScale Scale to set
     */
    private void setScale(float newScale) {
        // INFO:Correction to cope with the phenomenon that the enlargement rate does not change unless scale is set for child

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View targetView = getChildAt(i);
                targetView.setScaleX(newScale);
                targetView.setScaleY(newScale);
            }
        } else {
            setScaleX(newScale);
            setScaleY(newScale);
        }
    }

    /**
     * Draggable flag
     *
     * @param isDraggable true to enable dragging
     */
    void setDraggable(boolean isDraggable) {
        mIsDraggable = isDraggable;
    }

    /**
     * Constant representing the shape of View
     *
     * @param shape SHAPE_CIRCLE or SHAPE_RECTANGLE
     */
    void setShape(float shape) {
        mShape = shape;
    }

    /**
     * Get the shape of the view。
     *
     * @return SHAPE_CIRCLE or SHAPE_RECTANGLE
     */
    float getShape() {
        return mShape;
    }

    /**
     * This is the margin over the screen edge.。
     *
     * @param margin
     */
    void setOverMargin(int margin) {
        mOverMargin = margin;
    }

    /**
     * Set the movement direction.
     *
     * @param moveDirection Direction of movement
     */
    void setMoveDirection(int moveDirection) {
        mMoveDirection = moveDirection;
    }

    /**
     * Use dynamic physics-based animations or not
     * Warning: Can not be used before API 16
     *
     * @param usePhysics Setting this to false will revert to using a ValueAnimator (default is true)
     */
    void usePhysics(boolean usePhysics) {
        mUsePhysics = usePhysics && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * Set initial coordinates。
     *
     * @param x FloatingView initial X coordinate
     * @param y FloatingView initial Y coordinate
     */
    void setInitCoords(int x, int y) {
        mInitX = x;
        mInitY = y;
    }

    /**
     * Set a flag to animate on initial display。
     *
     * @param animateInitialMove True to animate on initial display
     */
    void setAnimateInitialMove(boolean animateInitialMove) {
        mAnimateInitialMove = animateInitialMove;
    }

    /**
     * Get the drawing area on the Window.
     *
     * @param outRect Rect to make changes
     */
    void getWindowDrawingRect(Rect outRect) {
        final int currentX = getXByTouch();
        final int currentY = getYByTouch();
        outRect.set(currentX, currentY, currentX + getWidth(), currentY + getHeight());
    }

    /**
     * WindowManager.Get LayoutParams
     */
    WindowManager.LayoutParams getWindowLayoutParams() {
        return mParams;
    }

    /**
     * FloatingView X coordinate calculated from touch coordinates     
     * @return FloatingView X coordinate
     */

    private int getXByTouch() {
        return (int) (mScreenTouchX - mLocalTouchX - mTouchXOffset);
    }

    /**
     * Y coordinate of FloatingView calculated from touch coordinates
     *
     * @return FloatingView Y coordinate
     */
    private int getYByTouch() {
        return (int) (mMetrics.heightPixels + mNavigationBarVerticalOffset - (mScreenTouchY - mLocalTouchY + getHeight() - mTouchYOffset));
    }

    /**
     * Change to normal state.
     */
    void setNormal() {
        mAnimationHandler.setState(STATE_NORMAL);
        mAnimationHandler.updateTouchPosition(getXByTouch(), getYByTouch());
    }

    /**
     * Change to the overlapped state.
     *
     * @param centerX Target center coordinate X
     * @param centerY Target center coordinate Y
     */
    void setIntersecting(int centerX, int centerY) {
        mAnimationHandler.setState(STATE_INTERSECTING);
        mAnimationHandler.updateTargetPosition(centerX, centerY);
    }

    /**
     * Change to finished state
     */
    void setFinishing() {
        mAnimationHandler.setState(STATE_FINISHING);
        mIsMoveAccept = false;
        setVisibility(View.GONE);
    }

    int getState() {
        return mAnimationHandler.getState();
    }

    /**
     * Set the cutout's safe inset area
     *
     * @param safeInsetRect {@link FloatingViewManager#setSafeInsetRect(Rect)}
     */
    void setSafeInsetRect(Rect safeInsetRect) {
        mSafeInsetRect.set(safeInsetRect);
    }

    /**
     * Handler that controls the animation
     */
    static class FloatingAnimationHandler extends Handler {

        /**
         * Milliseconds to refresh the animation
         */
        private static final long ANIMATION_REFRESH_TIME_MILLIS = 10L;

        /**
         * FloatingView adsorption / desorption time
         */
        private static final long CAPTURE_DURATION_MILLIS = 300L;

        /**
         * A constant representing the state without animation
         */
        private static final int ANIMATION_NONE = 0;

        /**
         * Constant animation that occurs when touching
         */
        private static final int ANIMATION_IN_TOUCH = 1;

        /**
         * Constant indicating the start of animation
         */
        private static final int TYPE_FIRST = 1;
        /**
         * Constant representing animation update
         */
        private static final int TYPE_UPDATE = 2;

        /**
         * The time when the animation started
         */
        private long mStartTime;

        /**
         * TransitionX at the start of the animation
         */
        private float mStartX;

        /**
         * TransitionY at the start of the animation
         */
        private float mStartY;

        /**
         * Running animation code
         */
        private int mStartedCode;

        /**
         * Animation state flag
         */
        private int mState;

        /**
         * Current state
         */
        private boolean mIsChangeState;

        /**
         * X coordinate to follow
         */
        private float mTouchPositionX;

        /**
         * Y coordinate to follow
         */
        private float mTouchPositionY;

        /**
         * X coordinate to follow
         */
        private float mTargetPositionX;

        /**
         * Y coordinate to follow
         */
        private float mTargetPositionY;

        /**
         * FloatingView
         */
        private final WeakReference<FloatingView> mFloatingView;

        /**
         * constructor
         */
        FloatingAnimationHandler(FloatingView floatingView) {
            mFloatingView = new WeakReference<>(floatingView);
            mStartedCode = ANIMATION_NONE;
            mState = STATE_NORMAL;
        }

        /**
         * Perform animation processing。
         */
        @Override
        public void handleMessage(Message msg) {
            final FloatingView floatingView = mFloatingView.get();
            if (floatingView == null) {
                removeMessages(ANIMATION_IN_TOUCH);
                return;
            }

            final int animationCode = msg.what;
            final int animationType = msg.arg1;
            final WindowManager.LayoutParams params = floatingView.mParams;

            // Initialization when state change or animation starts
            if (mIsChangeState || animationType == TYPE_FIRST) {
                // Use animation time only when changing state
                mStartTime = mIsChangeState ? SystemClock.uptimeMillis() : 0;
                mStartX = params.x;
                mStartY = params.y;
                mStartedCode = animationCode;
                mIsChangeState = false;
            }
            // elapsed time
            final float elapsedTime = SystemClock.uptimeMillis() - mStartTime;
            final float trackingTargetTimeRate = Math.min(elapsedTime / CAPTURE_DURATION_MILLIS, 1.0f);

            // Animation when there is no overlap
            if (mState == FloatingView.STATE_NORMAL) {
                final float basePosition = calcAnimationPosition(trackingTargetTimeRate);
                // Allow over-screen over
                final Rect moveLimitRect = floatingView.mMoveLimitRect;
                // Final destination
                final float targetPositionX = Math.min(Math.max(moveLimitRect.left, (int) mTouchPositionX), moveLimitRect.right);
                final float targetPositionY = Math.min(Math.max(moveLimitRect.top, (int) mTouchPositionY), moveLimitRect.bottom);
                params.x = (int) (mStartX + (targetPositionX - mStartX) * basePosition);
                params.y = (int) (mStartY + (targetPositionY - mStartY) * basePosition);
                floatingView.updateViewLayout();
                sendMessageAtTime(newMessage(animationCode, TYPE_UPDATE), SystemClock.uptimeMillis() + ANIMATION_REFRESH_TIME_MILLIS);
            }
            // Animation when overlapping
            else if (mState == FloatingView.STATE_INTERSECTING) {
                final float basePosition = calcAnimationPosition(trackingTargetTimeRate);
                // Final destination
                final float targetPositionX = mTargetPositionX - floatingView.getWidth() / 2;
                final float targetPositionY = mTargetPositionY - floatingView.getHeight() / 2;
                // Moving from your current location
                params.x = (int) (mStartX + (targetPositionX - mStartX) * basePosition);
                params.y = (int) (mStartY + (targetPositionY - mStartY) * basePosition);
                floatingView.updateViewLayout();
                sendMessageAtTime(newMessage(animationCode, TYPE_UPDATE), SystemClock.uptimeMillis() + ANIMATION_REFRESH_TIME_MILLIS);
            }

        }

        /**
         * Calculate the position obtained from the animation time
         *
         * @param timeRate Time ratio
         * @return Base coefficient (0.0 to 1.0 + α)
         */

        private static float calcAnimationPosition(float timeRate) {
            final float position;
            // y=0.55sin(8.0564x-π/2)+0.55
            if (timeRate <= 0.4) {
                position = (float) (0.55 * Math.sin(8.0564 * timeRate - Math.PI / 2) + 0.55);
            }
            // y=4(0.417x-0.341)^2-4(0.417-0.341)^2+1
            else {
                position = (float) (4 * Math.pow(0.417 * timeRate - 0.341, 2) - 4 * Math.pow(0.417 - 0.341, 2) + 1);
            }
            return position;
        }

        /**
         * Send animated message
         *
         * @param animation   ANIMATION_IN_TOUCH
         * @param delayMillis Message sending time
         */

        void sendAnimationMessageDelayed(int animation, long delayMillis) {
            sendMessageAtTime(newMessage(animation, TYPE_FIRST), SystemClock.uptimeMillis() + delayMillis);
        }

        /**
         * Send animated message
         *
         * @param animation ANIMATION_IN_TOUCH
         */
        void sendAnimationMessage(int animation) {
            sendMessage(newMessage(animation, TYPE_FIRST));
        }

        /**
         * Generate a message to send.
         *
         * @param animation ANIMATION_IN_TOUCH
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
         * Updates the position of touch coordinates.
         *
         * @param positionX Touch X coordinate
         * @param positionY Touch Y coordinate
         */
        void updateTouchPosition(float positionX, float positionY) {
            mTouchPositionX = positionX;
            mTouchPositionY = positionY;
        }

        /**
         * Update the position of the tracking target.
         *
         * @param centerX X coordinate to follow
         * @param centerY Y coordinate to follow
         */
        void updateTargetPosition(float centerX, float centerY) {
            mTargetPositionX = centerX;
            mTargetPositionY = centerY;
        }

        /**
         * Sets the animation state.
         *
         * @param newState STATE_NORMAL or STATE_INTERSECTING or STATE_FINISHING
         */
        void setState(@AnimationState int newState) {
            // Change state change flag only when the state is different
            if (mState != newState) {
                mIsChangeState = true;
            }
            mState = newState;
        }

        /**
         * Returns the current state.
         *
         * @return STATE_NORMAL or STATE_INTERSECTING or STATE_FINISHING
         */
        int getState() {
            return mState;
        }
    }

    /**
     * Handler that controls long press processing. <br/>
     * Since all touch processing is implemented with dispatchTouchEvent, long press is also implemented independently.
     */
    static class LongPressHandler extends Handler {

        /**
         * TrashView
         */
        private final WeakReference <FloatingView> mFloatingView;

        /**
           * Constant indicating no animation
           */
    private static final int LONG_PRESSED = 0;

        /**
         * Constructor
         * @param view FloatingVIew
         */
        LongPressHandler(FloatingView view) {
            mFloatingView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            FloatingView view = mFloatingView.get();
            if (view == null) {
                removeMessages(LONG_PRESSED);
                return;
            }
            view.onLongClick();
        }
     }
 }

