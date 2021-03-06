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

package at.florianschuster.watchables.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import at.florianschuster.watchables.MainDirections
import at.florianschuster.watchables.R
import at.florianschuster.watchables.all.util.GlideApp
import at.florianschuster.watchables.model.Movie
import at.florianschuster.watchables.model.Season
import at.florianschuster.watchables.model.Watchable
import at.florianschuster.watchables.model.thumbnailPoster
import at.florianschuster.watchables.ui.main.MainActivity
import com.google.firebase.messaging.RemoteMessage
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import timber.log.Timber

interface NotificationService {
    fun addWatchableError(id: Int, name: String?)
    fun push(remote: RemoteMessage.Notification)
    fun pushMovieUpdate(watchable: Watchable, movie: Movie)
    fun pushShowUpdate(watchable: Watchable, showName: String, season: Season)
}

class AndroidNotificationService(
    private val context: Context
) : NotificationService {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) generateChannels()
    }

    private val contentPendingIntent: PendingIntent
        get() {
            val contentIntent = Intent(context, MainActivity::class.java)
            return PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

    // add watchable

    override fun addWatchableError(id: Int, name: String?) {
        val notification = NotificationCompat.Builder(context, ADD_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification)
            setContentIntent(contentPendingIntent)
            setContentTitle(context.getString(R.string.notification_add_error_notification_title))
            val displayName = name ?: id.toString()
            setContentText(context.getString(R.string.notification_add_error_notification_text, displayName))
            setAutoCancel(true)
            setShowWhen(true)
        }.build()

        NotificationManagerCompat.from(context).notify(ADD_ERROR_NOTIFICATION_ID + id, notification)
    }

    // push

    override fun push(remote: RemoteMessage.Notification) {
        val pushNotification = NotificationCompat.Builder(context, MESSAGE_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification)
            setContentTitle(remote.title)
            setContentText(remote.body)
            setContentIntent(contentPendingIntent)
            setAutoCancel(true)
            setShowWhen(true)
            setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
        }.build()

        NotificationManagerCompat.from(context).notify(MESSAGE_NOTIFICATION_ID, pushNotification)
    }

    // update

    override fun pushMovieUpdate(watchable: Watchable, movie: Movie) {
        val shortName = if (movie.name.length <= 25) movie.name else "${movie.name.substring(0, 26)}..."
        pushUpdate(movie.id, shortName, watchable)
    }

    override fun pushShowUpdate(watchable: Watchable, showName: String, season: Season) {
        val shortName = if (showName.length <= 25) showName else "${showName.substring(0, 26)}..."
        pushUpdate(season.id, context.getString(R.string.notification_update_title_show_name, shortName, season.index), watchable)
    }

    private fun pushUpdate(notificationId: Int, name: String, watchable: Watchable) {
        val pendingIntent = NavDeepLinkBuilder(context).apply {
            setGraph(R.navigation.main)
            setDestination(R.id.detail)
            setArguments(MainDirections.toDetail(watchable.id, watchable.type).arguments)
        }.createPendingIntent()

        val pushNotificationBuilder = NotificationCompat
            .Builder(context, UPDATE_CHANNEL_ID).apply {
                setSmallIcon(R.drawable.ic_notification)
                setContentTitle(context.getString(R.string.notification_update_title, name))
                setContentText(context.getString(R.string.notification_update_text))
                setContentIntent(pendingIntent)
                setShowWhen(true)
                setAutoCancel(true)
                setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                setGroup(UPDATE_NOTIFICATION_GROUP)
            }

        val image = watchable.thumbnailPoster

        if (image != null) {
            showNotificationWithImage(notificationId, pushNotificationBuilder, image)
        } else {
            NotificationManagerCompat.from(context).notify(notificationId, pushNotificationBuilder.build())
        }
    }

    private fun showNotificationWithImage(notificationId: Int, pushNotificationBuilder: NotificationCompat.Builder, imageUrl: String) {
        val futureTarget = GlideApp.with(context).asBitmap().load(imageUrl).submit()

        fun showNotification(bitmap: Bitmap?) {
            bitmap?.let(pushNotificationBuilder::setLargeIcon)
            NotificationManagerCompat.from(context).notify(notificationId, pushNotificationBuilder.build())
            GlideApp.with(context).clear(futureTarget)
        }

        @Suppress("UNUSED_VARIABLE")
        val ignore = Single.fromFuture(futureTarget)
            .timeout(10, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess(::showNotification)
            .doOnError(Timber::e)
            .doOnError { showNotification(null) }
            .subscribe()
    }

    // channels

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateChannels() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        NotificationChannel(
            ADD_CHANNEL_ID,
            context.getString(R.string.notification_add_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_add_channel_description)
            enableVibration(false)
            enableLights(false)
        }.also(notificationManager::createNotificationChannel)

        NotificationChannel(
            MESSAGE_CHANNEL_ID,
            context.getString(R.string.notification_message_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_message_channel_description)
            enableVibration(true)
            enableLights(true)
        }.also(notificationManager::createNotificationChannel)

        NotificationChannel(
            UPDATE_CHANNEL_ID,
            context.getString(R.string.notification_update_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_update_channel_description)
            enableVibration(true)
            enableLights(true)
        }.also(notificationManager::createNotificationChannel)
    }

    companion object {
        private const val ADD_CHANNEL_ID = "WATCHABLES.add"
        private const val ADD_ERROR_NOTIFICATION_ID = 420

        private const val MESSAGE_CHANNEL_ID = "WATCHABLES.message"
        private const val MESSAGE_NOTIFICATION_ID = 42

        private const val UPDATE_CHANNEL_ID = "WATCHABLES.update"
        private const val UPDATE_NOTIFICATION_GROUP = "updates"
    }
}
