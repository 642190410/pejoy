/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.keep2iron.pejoy.filter

import android.content.Context

import io.github.keep2iron.pejoy.MimeType
import io.github.keep2iron.pejoy.internal.entity.IncapableCause
import io.github.keep2iron.pejoy.internal.entity.Item

abstract class Filter {

    /**
     * Against what mime types this filter applies.
     */
    protected abstract fun constraintTypes(): Set<MimeType>

    /**
     * Invoked for filtering each item.
     *
     * @return null if selectable, [IncapableCause] if not selectable.
     */
    abstract fun filter(context: Context, item: Item): IncapableCause

    /**
     * Whether an [Item] need filtering.
     */
    protected fun needFiltering(context: Context, item: Item): Boolean {
        for (type in constraintTypes()) {
            if (type.checkType(context.contentResolver, item.contentUri)) {
                return true
            }
        }
        return false
    }

    companion object {
        /**
         * Convenient constant for a minimum value.
         */
        val MIN = 0
        /**
         * Convenient constant for a maximum value.
         */
        val MAX = Integer.MAX_VALUE
        /**
         * Convenient constant for 1024.
         */
        val K = 1024
    }
}