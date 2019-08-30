/*
 * Copyright 2019 Florian Schuster. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.florianschuster.watchables.all.util.extensions

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable

fun <T : Any> Observable<List<T>>.rxDiff(
    differ: (List<T>, List<T>) -> DiffUtil.Callback
): Flowable<Pair<List<T>, DiffUtil.DiffResult?>> {
    return toFlowable(BackpressureStrategy.BUFFER)
        .scan(Pair(emptyList(), null), { (oldItems, _), nextItems ->
            val callback = differ(oldItems, nextItems)
            val result = DiffUtil.calculateDiff(callback, true)

            nextItems to result
        })
}