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

package at.florianschuster.watchables.ui.more

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import at.florianschuster.reaktor.ReactorView
import at.florianschuster.reaktor.android.ViewModelReactor
import at.florianschuster.reaktor.android.bind
import at.florianschuster.reaktor.android.koin.reactor
import at.florianschuster.reaktor.changesFrom
import at.florianschuster.reaktor.emptyMutation
import at.florianschuster.watchables.BuildConfig
import at.florianschuster.watchables.R
import at.florianschuster.watchables.all.Option
import at.florianschuster.watchables.all.OptionsAdapter
import at.florianschuster.watchables.all.util.extensions.openAppInPlayStore
import at.florianschuster.watchables.all.util.extensions.openChromeTab
import at.florianschuster.watchables.all.util.extensions.showLibraries
import at.florianschuster.watchables.all.worker.DeleteWatchablesWorker
import at.florianschuster.watchables.all.worker.UpdateWatchablesWorker
import at.florianschuster.watchables.service.AnalyticsService
import at.florianschuster.watchables.service.Session
import at.florianschuster.watchables.service.SessionService
import at.florianschuster.watchables.service.ShareService
import at.florianschuster.watchables.service.local.PrefRepo
import at.florianschuster.watchables.ui.base.BaseFragment
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.jakewharton.rxbinding3.view.clicks
import com.tailoredapps.androidutil.optional.asOptional
import com.tailoredapps.androidutil.optional.filterSome
import com.tailoredapps.androidutil.optional.ofType
import com.tailoredapps.androidutil.ui.IntentUtil
import com.tailoredapps.androidutil.ui.extensions.RxDialogAction
import com.tailoredapps.androidutil.ui.extensions.rxDialog
import com.tailoredapps.androidutil.ui.extensions.toast
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.ofType
import kotlin.random.Random
import kotlinx.android.synthetic.main.fragment_more.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class MoreFragment : BaseFragment(R.layout.fragment_more), ReactorView<MoreReactor> {
    override val reactor: MoreReactor by reactor()
    private val adapter: OptionsAdapter by inject()
    private val shareService: ShareService by inject { parametersOf(activity) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(rvMoreOptions) {
            adapter = this@MoreFragment.adapter
            layoutManager = GridLayoutManager(requireContext(), 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int = if (position < 2) 1 else 2
                }
            }
        }

        tvVersion.text = getString(R.string.more_app_version, "${BuildConfig.VERSION_NAME}-b${BuildConfig.VERSION_CODE}")
        bind(reactor)
    }

    override fun bind(reactor: MoreReactor) {
        adapter.update()

        reactor.state.changesFrom { it.userName.asOptional }
            .filterSome()
            .map {
                val array = resources.getStringArray(R.array.more_hello_there)
                "${array[Random.nextInt(array.size)]}$it"
            }
            .bind(to = tvHello::setText)
            .addTo(disposables)

        reactor.state.changesFrom { it.userId.asOptional }
            .filterSome()
            .map { getString(R.string.more_user_id, it) }
            .bind(to = tvUserId::setText)
            .addTo(disposables)

        tvUserId.clicks()
            .map { reactor.currentState.userId.asOptional }
            .filterSome()
            .bind {
                val label = getString(R.string.more_copied_user_id_label)
                requireContext().copyTextToClipBoard(label, it)
                toast(label)
            }
            .addTo(disposables)
    }

    private fun OptionsAdapter.update() {
        listOfNotNull(
            rateOption,
            shareOption,
            ratingsOption,
            updateWatchablesOption,
            Option.Divider,
            privacyOption,
            licensesOption,
            devInfoOption,
            reportBugOption,
            analyticsOption,
            Option.Divider,
            logoutOption
        ).also(::submitList)
    }

    private fun Context.copyTextToClipBoard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    private val rateOption: Option
        get() = Option.SquareAction(R.string.settings_rate_app, R.drawable.ic_rate_review) {
            openAppInPlayStore()
        }

    private val shareOption: Option
        get() = Option.SquareAction(R.string.more_share_app, R.drawable.ic_share) {
            shareService.shareApp().subscribe().addTo(disposables)
        }

    private val updateWatchablesOption: Option
        get() = Option.Value(R.string.more_update_watchables, R.drawable.ic_update, getString(R.string.more_update_watchables_subtitle)) {
            reactor.action.accept(MoreReactor.Action.UpdateWatchables)
        }

    private val ratingsOption: Option
        get() = Option.Toggle(R.string.more_ratings_toggle, R.drawable.ic_star, reactor.currentState.watchableRatingsEnabled) {
            reactor.action.accept(MoreReactor.Action.SetWatchableRatings(it))
        }

    private val devInfoOption: Option
        get() = Option.Action(R.string.more_developer_info, R.drawable.ic_code) {
            openChromeTab(getString(R.string.developer_url))
        }

    private val reportBugOption: Option
        get() = Option.Action(R.string.more_report_bug, R.drawable.ic_bug_report) {
            startActivity(IntentUtil.mail(getString(R.string.dev_mail)))
        }

    private val privacyOption: Option
        get() = Option.Action(R.string.more_privacy_policy, R.drawable.ic_phonelink_lock) {
            openChromeTab(getString(R.string.privacy_policy_url))
        }

    private val licensesOption: Option
        get() = Option.Action(R.string.more_licenses, R.drawable.ic_search) {
            showLibraries()
        }

    private val analyticsOption: Option
        get() = Option.Toggle(R.string.more_analytics, R.drawable.ic_show_chart, reactor.currentState.analyticsEnabled) {
            reactor.action.accept(MoreReactor.Action.SetAnalytics(it))
        }

    private val logoutOption: Option
        get() = Option.Action(R.string.more_logout, R.drawable.ic_open_in_browser) {
            rxDialog(R.style.DialogTheme) {
                titleResource = R.string.dialog_logout_title
                messageResource = R.string.dialog_logout_message
                positiveButtonResource = R.string.dialog_ok
                negativeButtonResource = R.string.dialog_cancel
            }.ofType<RxDialogAction.Positive>()
                .map { MoreReactor.Action.Logout }
                .bind(to = reactor.action)
                .addTo(disposables)
        }
}

class MoreReactor(
    private val sessionService: SessionService<FirebaseUser, AuthCredential>,
    private val analyticsService: AnalyticsService,
    private val prefRepo: PrefRepo
) : ViewModelReactor<MoreReactor.Action, MoreReactor.Mutation, MoreReactor.State>(
    initialState = State(
        analyticsEnabled = analyticsService.analyticsEnabled,
        watchableRatingsEnabled = prefRepo.watchableRatingsEnabled
    )
) {
    sealed class Action {
        object Logout : Action()
        data class SetAnalytics(val enabled: Boolean) : Action()
        data class SetWatchableRatings(val enabled: Boolean) : Action()
        object UpdateWatchables : Action()
    }

    sealed class Mutation {
        data class SetUser(val id: String?, val name: String?) : Mutation()
        data class SetAnalyticsEnabled(val enabled: Boolean) : Mutation()
        data class SetWatchableRatingsEnabled(val enabled: Boolean) : Mutation()
    }

    data class State(
        val analyticsEnabled: Boolean,
        val watchableRatingsEnabled: Boolean,
        val userName: String? = null,
        val userId: String? = null
    )

    override fun transformMutation(mutation: Observable<Mutation>): Observable<out Mutation> =
        Observable.merge(mutation, userNameMutation)

    override fun mutate(action: Action): Observable<out Mutation> = when (action) {
        is Action.Logout -> {
            sessionService.logout()
                .doOnComplete { UpdateWatchablesWorker.stop() }
                .doOnComplete { DeleteWatchablesWorker.cancel() }
                .toObservable()
        }
        is Action.SetAnalytics -> {
            Single.just(action.enabled)
                .doOnSuccess { analyticsService.analyticsEnabled = it }
                .map { Mutation.SetAnalyticsEnabled(it) }
                .toObservable()
        }
        is Action.SetWatchableRatings -> {
            Single.just(action.enabled)
                .doOnSuccess { prefRepo.watchableRatingsEnabled = it }
                .map { Mutation.SetWatchableRatingsEnabled(it) }
                .toObservable()
        }
        is Action.UpdateWatchables -> {
            emptyMutation { UpdateWatchablesWorker.once() }
        }
    }

    override fun reduce(previousState: State, mutation: Mutation): State = when (mutation) {
        is Mutation.SetAnalyticsEnabled -> previousState.copy(analyticsEnabled = mutation.enabled)
        is Mutation.SetUser -> previousState.copy(userId = mutation.id, userName = mutation.name)
        is Mutation.SetWatchableRatingsEnabled -> previousState.copy(watchableRatingsEnabled = mutation.enabled)
    }

    private val userNameMutation: Observable<out Mutation>
        get() = sessionService.session
            .ofType<Session.Exists<FirebaseUser>>()
            .map { Mutation.SetUser(it.user.uid, it.user.displayName) }
            .toObservable()
}
