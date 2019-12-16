package com.example.library;


/**
 * FloatingView Listner
 */
public interface FloatingViewListener {

    /**
     * Called when exiting FloatingView。
     */
    void onFinishFloatingView();

    /**
     * Callback when touch action finished.
     *
     * @param isFinishing Whether FloatingView is being deleted or not.
     * @param x           x coordinate
     * @param y           y coordinate
     */
    void onTouchFinished(boolean isFinishing, int x, int y);

}
