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

package at.florianschuster.watchables.ui

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import at.florianschuster.watchables.R
import at.florianschuster.watchables.service.FirebaseUserSessionService
import at.florianschuster.watchables.service.Session
import at.florianschuster.watchables.service.local.PrefRepo
import at.florianschuster.watchables.ui.base.views.BaseActivity
import at.florianschuster.watchables.util.Utils
import at.florianschuster.watchables.util.extensions.RxDialogAction
import at.florianschuster.watchables.util.extensions.RxTasks
import at.florianschuster.watchables.util.extensions.rxDialog
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import io.reactivex.rxkotlin.addTo
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDate
import timber.log.Timber


class MainActivity : BaseActivity(R.layout.activity_main) {
    private val navController by lazy { findNavController(R.id.navHost) }

    private val userSessionService: FirebaseUserSessionService by inject()
    private val prefRepo: PrefRepo by inject()

    private val noSessionNeededDestinations = arrayOf(R.id.splashscreen, R.id.login)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RxTasks.maybe { FirebaseDynamicLinks.getInstance().getDynamicLink(intent) }
                .subscribe({ Timber.d("Deeplink: ${it.link}") }, Timber::e) //todo
                .addTo(disposables)

        userSessionService.session
                .distinctUntilChanged()
                .filter { it is Session.None }
                .filter { navController.currentDestination?.id !in noSessionNeededDestinations }
                .subscribe {
                    val navOptions = navOptions { popUpTo(R.id.splashscreen) { inclusive = true } }
                    navController.navigate(R.id.login, null, navOptions)
                }
                .addTo(disposables)

        if (userSessionService.loggedIn && prefRepo.enjoyingAppDialogShownDate.isBefore(LocalDate.now().minusMonths(1))) {
            rxDialog {
                titleResource = R.string.enjoying_dialog_title
                messageResource = R.string.enjoying_dialog_message
                positiveButtonResource = R.string.enjoying_dialog_positive
                negativeButtonResource = R.string.enjoying_dialog_negative
                neutralButtonResource = R.string.enjoying_dialog_neutral
            }.doOnSuccess {
                prefRepo.enjoyingAppDialogShownDate = LocalDate.now()
                val intent = when (it) {
                    is RxDialogAction.Positive -> Utils.rateApp(getString(R.string.playstore_link, packageName))
                    is RxDialogAction.Negative -> Utils.mail(getString(R.string.dev_mail))
                    else -> null
                }
                intent?.let(::startActivity)
            }.subscribe().addTo(disposables)
        }
    }

    override fun onSupportNavigateUp() = navController.navigateUp()
}