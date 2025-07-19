package com.babelsoftware.loudly

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.babelsoftware.loudly.playback.PlayerConnection

class MusicWidgetQuick : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == MusicWidget.ACTION_STATE_CHANGED) {
            updateAllWidgets(context)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MusicWidgetQuick::class.java)
            appWidgetManager.getAppWidgetIds(componentName)?.forEach { appWidgetId ->
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_music_quick)
            val playerConnection = PlayerConnection.instance
            val player = playerConnection?.player

            if (player != null && player.currentMediaItem != null) {
                val mediaMetadata = player.mediaMetadata
                views.setTextViewText(R.id.widget_title, mediaMetadata.title ?: "Bilinmeyen Şarkı")
                views.setTextViewText(R.id.widget_artist, mediaMetadata.artist ?: "Bilinmeyen Sanatçı")
                val playPauseIcon = if (player.isPlaying) R.drawable.pause else R.drawable.play
                views.setImageViewResource(R.id.widget_play_pause_button, playPauseIcon)
                views.setViewVisibility(R.id.widget_artist, View.VISIBLE)
            } else {
                views.setTextViewText(R.id.widget_title, "Müzik Çalmıyor")
                views.setViewVisibility(R.id.widget_artist, View.GONE)
                views.setImageViewResource(R.id.widget_play_pause_button, R.drawable.play)
            }

            val playPauseIntent = Intent(context, MusicWidget::class.java).apply {
                action = MusicWidget.ACTION_PLAY_PAUSE
            }
            val playPausePendingIntent = PendingIntent.getBroadcast(
                context,
                MusicWidget.ACTION_PLAY_PAUSE.hashCode(),
                playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_play_pause_button, playPausePendingIntent)

            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
