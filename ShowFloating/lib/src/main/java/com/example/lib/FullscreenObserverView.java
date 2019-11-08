package com.example.lib;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

/**
 * This is a full-screen view.。
 * http://stackoverflow.com/questions/18551135/receiving-hidden-status-bar-entering-a-full-screen-activity-event-on-a-service/19201933#19201933
 */
class FullscreenObserverView extends View implements ViewTreeObserver.OnGlobalLayoutListener, View.OnSystemUiVisibilityChangeListener {

    /**
     * Constant that mLastUiVisibility does not exist.
     */
    static final int NO_LAST_VISIBILITY = -1;

    /**
     * Overlay Type
     */
    private static final int OVERLAY_TYPE;

    /**
     * WindowManager.LayoutParams
     */
    private final WindowManager.LayoutParams mParams;

    /**
     * ScreenListener
     */
    private final ScreenChangedListener mScreenChangedListener;

    /**
     * Last view state (keep as it may not arrive onSystemUiVisibilityChange)
     * If you do not come: ImmediateMode→Touch the status bar→Status bar disappears
     */
    private int mLastUiVisibility;

    /**
     * Window of Rect
     */
    private final Rect mWindowRect;

    static {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            OVERLAY_TYPE = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        } else {
            OVERLAY_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
    }

    /**
     * constructor
     */
    FullscreenObserverView(Context context, ScreenChangedListener listener) {
        super(context);

        // Listener set
        mScreenChangedListener = listener;

        // Width1, Maximum Transparency View to detect layout changes
        mParams = new WindowManager.LayoutParams();
        mParams.width = 1;
        mParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mParams.type = OVERLAY_TYPE;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mParams.format = PixelFormat.TRANSLUCENT;

        mWindowRect = new Rect();
        mLastUiVisibility = NO_LAST_VISIBILITY;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        setOnSystemUiVisibilityChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDetachedFromWindow() {
        // Delete Layout Change Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            //noinspection deprecation
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
        setOnSystemUiVisibilityChangeListener(null);
        super.onDetachedFromWindow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGlobalLayout() {
        // Get View (Full Screen) Size
        if (mScreenChangedListener != null) {
            getWindowVisibleDisplayFrame(mWindowRect);
            mScreenChangedListener.onScreenChanged(mWindowRect, mLastUiVisibility);
        }
    }

    /**
     * I am using it in an application that handles the navigation bar (if onGlobalLayout events do not occur).
     * (e.g., Nexus5 camera apps)
     */
    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        mLastUiVisibility = visibility;
        // In response to changes in navigation bar, display/non-display switching
        if (mScreenChangedListener != null) {
            getWindowVisibleDisplayFrame(mWindowRect);
            mScreenChangedListener.onScreenChanged(mWindowRect, visibility);
        }
    }

    /**
     * WindowManager.LayoutParams
     *
     * @return WindowManager.LayoutParams
     */
    WindowManager.LayoutParams getWindowLayoutParams() {
        return mParams;
    }
}