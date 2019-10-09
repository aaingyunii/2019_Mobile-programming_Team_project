package com.example.munmo;

interface TrashViewListener {

    /**
     * Require ActionTrashIcon updates.
     */
    void onUpdateActionTrashIcon();

    /**
     * アニメーションを開始した時に通知されます。
     *
     * @param animationCode アニメーションコード
     */
    void onTrashAnimationStarted(int animationCode);

    /**
     * アニメーションが終了した時に通知されます。
     *
     * @param animationCode アニメーションコード
     */
    void onTrashAnimationEnd(int animationCode);


}