package com.babelsoftware.loudly

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.babelsoftware.loudly.playback.PlayerConnection
import com.babelsoftware.loudly.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Referans projedeki çalışma mantığına göre uyarlanmış, stabil ve sorunsuz çalışan nihai kod.
 * Tasarım korunmuş, sadece kontrol ve güncelleme mantığı düzeltilmiştir.
 */
class MusicWidget : AppWidgetProvider() {

    // onUpdate, onEnabled, ve onDisabled fonksiyonları widget'ın yaşam döngüsünü yönetir.
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // İlk widget eklendiğinde ilerleme çubuğu güncelleyiciyi başlatmayı dene.
        manageProgressUpdater(context)
    }

    override fun onDisabled(context: Context) {
        // Son widget kaldırıldığında güncelleyiciyi kesin olarak durdur.
        stopProgressUpdater()
    }

    /**
     * Widget butonlarına basıldığında veya durum değişikliği bildirildiğinde bu fonksiyon tetiklenir.
     * Referans projedeki gibi, her eylemden sonra widget'ın tamamı güncellenir.
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val player = PlayerConnection.instance

        when (intent.action) {
            ACTION_PLAY_PAUSE -> player?.togglePlayPause()
            ACTION_PREV -> player?.seekToPrevious()
            ACTION_NEXT -> player?.seekToNext()
            // Müzik uygulamasından durum değişikliği geldiğinde de güncelle.
            ACTION_STATE_CHANGED -> { /* Sadece güncelleme tetiklenir */ }
        }

        // Bir eylem gerçekleştikten sonra, tüm widget'ları yeni duruma göre güncelle.
        updateAllWidgets(context)
        // Oynatma durumu değişmiş olabileceğinden, güncelleyiciyi yeniden yönet.
        manageProgressUpdater(context)
    }

    companion object {
        private const val ACTION_PLAY_PAUSE = "com.babelsoftware.loudly.ACTION_PLAY_PAUSE"
        private const val ACTION_PREV = "com.babelsoftware.loudly.ACTION_PREV"
        private const val ACTION_NEXT = "com.babelsoftware.loudly.ACTION_NEXT"
        const val ACTION_STATE_CHANGED = "com.babelsoftware.loudly.ACTION_STATE_CHANGED"

        private val handler = Handler(Looper.getMainLooper())
        private var progressUpdater: Runnable? = null

        /**
         * Ekrandaki tüm müzik widget'larını günceller.
         */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MusicWidget::class.java)
            appWidgetManager.getAppWidgetIds(componentName)?.forEach { appWidgetId ->
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        /**
         * Belirli bir widget'ı güncelleyen ana fonksiyon.
         */
        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_music)
            val player = PlayerConnection.instance?.player

            // Müzik çalmıyorsa veya bağlantı koptuysa, varsayılan görünümü ayarla.
            if (player == null || player.currentMediaItem == null) {
                views.setTextViewText(R.id.widget_track_title, "Müzik Çalmıyor")
                views.setTextViewText(R.id.widget_artist, "")
                views.setImageViewResource(R.id.widget_play_pause, R.drawable.play)
                views.setImageViewResource(R.id.widget_album_art, R.drawable.widget_album_art_background)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            // --- 1. Metin, Buton ve İlerleme gibi anında güncellenecek bilgileri ayarla ---
            views.setTextViewText(R.id.widget_track_title, player.mediaMetadata.title)
            views.setTextViewText(R.id.widget_artist, player.mediaMetadata.artist)

            val playPauseIcon = if (player.isPlaying) R.drawable.pause else R.drawable.play
            views.setImageViewResource(R.id.widget_play_pause, playPauseIcon)

            val progress = if (player.duration > 0) (player.currentPosition * 100 / player.duration).toInt() else 0
            views.setProgressBar(R.id.widget_progress, 100, progress, false)

            // --- 2. Kenarları yuvarlatma özelliğini etkinleştir ---
            views.setBoolean(R.id.widget_album_art, "setClipToOutline", true)

            // --- 3. Albüm kapağını yükle ---
            val imageUrl = player.mediaMetadata.artworkUri
            if (imageUrl != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val request = ImageRequest.Builder(context).data(imageUrl).build()
                    val bitmap = try { ImageLoader(context).execute(request).drawable?.toBitmap() } catch (e: Exception) { null }

                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            views.setImageViewBitmap(R.id.widget_album_art, bitmap)
                        } else {
                            views.setImageViewResource(R.id.widget_album_art, R.drawable.widget_album_art_background)
                        }
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            } else {
                views.setImageViewResource(R.id.widget_album_art, R.drawable.widget_album_art_background)
            }

            // --- 4. Tıklama olaylarını (PendingIntent) ayarla ---
            views.setOnClickPendingIntent(R.id.widget_play_pause, getBroadcastPendingIntent(context, ACTION_PLAY_PAUSE))
            views.setOnClickPendingIntent(R.id.widget_prev, getBroadcastPendingIntent(context, ACTION_PREV))
            views.setOnClickPendingIntent(R.id.widget_next, getBroadcastPendingIntent(context, ACTION_NEXT))

            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

            // --- 5. Widget'ı GÜNCELLE ---
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getBroadcastPendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, MusicWidget::class.java).apply { this.action = action }
            return PendingIntent.getBroadcast(
                context, action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        /**
         * Sadece müzik çalarken ilerleme çubuğunu güncelleyen mekanizmayı yönetir.
         * Bu, referans koddaki mantığın daha güvenli halidir.
         */
        private fun manageProgressUpdater(context: Context) {
            stopProgressUpdater() // Önce mevcut olanı durdur.
            if (PlayerConnection.instance?.player?.isPlaying == true) {
                progressUpdater = Runnable {
                    updateAllWidgets(context)
                    // Sadece hala çalıyorsa bir sonraki güncellemeyi planla.
                    if (PlayerConnection.instance?.player?.isPlaying == true) {
                        handler.postDelayed(progressUpdater!!, 1000)
                    }
                }.also {
                    handler.post(it) // Güncelleyiciyi başlat.
                }
            }
        }

        private fun stopProgressUpdater() {
            progressUpdater?.let { handler.removeCallbacks(it) }
            progressUpdater = null
        }
    }
}
