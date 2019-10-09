package com.example.munmo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

/**
 *View that monitors full screen.
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
     * Last display status (onSystemUiVisibilityChange may not come)
     * ※ If you do not come: ImmersiveMode → touch the status bar → the status bar disappears
     */
    private int mLastUiVisibility;

    /**
     * Window Rect
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
     * コンストラクタ
     */
    FullscreenObserverView(Context context, ScreenChangedListener listener) {
        super(context);

        // リスナーのセット
        mScreenChangedListener = listener;

        // 幅1,高さ最大の透明なViewを用意して、レイアウトの変化を検知する
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
        //Remove layout change notification
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
        // Get View (full screen) size
        if (mScreenChangedListener != null) {
            getWindowVisibleDisplayFrame(mWindowRect);
            mScreenChangedListener.onScreenChanged(mWindowRect, mLastUiVisibility);
        }
    }

    /**
     Used in an application that performs processing on the navigation bar (when an onGlobalLayout event does not occur).
     * (Nexus5 camera app etc.)
     */
    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        mLastUiVisibility = visibility;
        // ナビゲーションバーの変化を受けて表示・非表示切替
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