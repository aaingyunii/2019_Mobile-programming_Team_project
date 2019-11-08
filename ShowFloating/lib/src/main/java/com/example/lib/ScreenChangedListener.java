package com.example.lib;

import android.graphics.Rect;

/**
 * It's a listener that deals with screen changes.
 */
interface ScreenChangedListener {
    /**
     * It's called when the screen changes.
     *
     * @param windowRect System window rect
     * @param visibility System UI Mode
     */
    void onScreenChanged(Rect windowRect, int visibility);
}
