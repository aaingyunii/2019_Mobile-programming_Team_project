package com.example.lib;

/**
 * Listener that handles events in the TrashView.。
 * INFO: Due to the specification followed by the delete icon, the end of the OPEN animation is not notified.
 */
interface TrashViewListener {

    /**
     * Require ActionTrashIcon updates.
     */
    void onUpdateActionTrashIcon();

    /**
     * You're notified when you start an animation.。
     *
     * @param animationCode animation code
     */
    void onTrashAnimationStarted(int animationCode);

    /**
     *You will be notified when the animation is finished.
     *
     * @param animationCode animation code
     */
    void onTrashAnimationEnd(int animationCode);


}
