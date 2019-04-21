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

package at.florianschuster.watchables

import androidx.appcompat.app.AppCompatActivity
import at.florianschuster.watchables.all.OptionsAdapter
import at.florianschuster.watchables.service.ActivityShareService
import at.florianschuster.watchables.service.AnalyticsService
import at.florianschuster.watchables.service.AndroidNotificationService
import at.florianschuster.watchables.service.FirebaseAnalyticsService
import at.florianschuster.watchables.service.FirebaseSessionService
import at.florianschuster.watchables.service.FirebaseWatchablesDataSource
import at.florianschuster.watchables.service.NotificationService
import at.florianschuster.watchables.service.SessionService
import at.florianschuster.watchables.service.ShareService
import at.florianschuster.watchables.service.WatchablesDataSource
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.squareup.leakcanary.LeakCanary
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { LeakCanary.install(androidApplication()) }
    single { FirebaseSessionService(androidContext()) as SessionService<FirebaseUser, AuthCredential> }
    single { FirebaseAnalyticsService(androidContext(), get()) as AnalyticsService }
    single { AndroidNotificationService(androidContext()) as NotificationService }
    single { FirebaseWatchablesDataSource(get()) as WatchablesDataSource }

    factory { (activity: AppCompatActivity) -> ActivityShareService(activity) as ShareService }
    factory { OptionsAdapter() }
}