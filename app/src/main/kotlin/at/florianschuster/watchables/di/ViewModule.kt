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

package at.florianschuster.watchables.di

import at.florianschuster.android.koin.coordinator
import at.florianschuster.watchables.ui.detail.OptionsAdapter
import at.florianschuster.watchables.ui.detail.DetailMediaAdapter
import at.florianschuster.watchables.ui.detail.DetailReactor
import at.florianschuster.watchables.ui.login.LoginCoordinator
import at.florianschuster.watchables.ui.login.LoginReactor
import at.florianschuster.watchables.ui.search.SearchAdapter
import at.florianschuster.watchables.ui.search.SearchReactor
import at.florianschuster.watchables.ui.watchables.WatchablesAdapter
import at.florianschuster.watchables.ui.watchables.WatchablesCoordinator
import at.florianschuster.watchables.ui.watchables.WatchablesReactor
import com.tailoredapps.reaktor.koin.reactor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val viewModule = module {
    coordinator { LoginCoordinator(get())  }
    reactor { LoginReactor(get(), get(), get()) }

    coordinator { WatchablesCoordinator(get())  }
    reactor { WatchablesReactor(get(), get(), get(), get()) }
    factory { WatchablesAdapter(androidContext().resources) }

    reactor { SearchReactor(get(), get()) }
    factory { SearchAdapter() }

    reactor { (itemId: String) -> DetailReactor(itemId, get(), get(), get()) }
    factory { DetailMediaAdapter() }
    factory { OptionsAdapter() }
}