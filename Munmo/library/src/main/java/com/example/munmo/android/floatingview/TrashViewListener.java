/**
 * Copyright 2015 RECRUIT LIFESTYLE CO., LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.munmo.android.floatingview;

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
