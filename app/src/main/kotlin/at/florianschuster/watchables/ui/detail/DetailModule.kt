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

package at.florianschuster.watchables.ui.detail

import at.florianschuster.koordinator.android.koin.coordinator
import at.florianschuster.reaktor.android.koin.reactor
import at.florianschuster.watchables.model.Watchable
import org.koin.dsl.module

internal val detailModule = module {
    coordinator { DetailCoordinator() }
    reactor { (itemId: String, type: Watchable.Type) ->
        DetailReactor(itemId, type, get(), get(), get(), get(), get(), get())
    }
    factory { DetailHeaderAdapter() }
}
