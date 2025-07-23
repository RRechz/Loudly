package com.babelsoftware.loudly.ui.player // veya uygun bir paket

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import kotlinx.coroutines.*

/**
 * Oynatıcı hatalarını yönetmek, yeniden deneme ve kalite düşürme gibi
 * otomatik kurtarma stratejilerini uygulamak için bir yardımcı sınıf.
 */
class PlayerErrorManager(
    private val player: Player,
    private val onStateChange: (State) -> Unit
) {
    // Kurtarma durumlarını UI'a bildirmek için enum sınıfı
    enum class State {
        IDLE,       // Boşta, hata yok
        RECOVERING, // Otomatik kurtarma deneniyor
        FAILED      // Kurtarma başarısız oldu, kalıcı hata
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var consecutiveErrors = 0
    private var backoffMillis = 1000L

    companion object {
        private const val MAX_CONSECUTIVE_ERRORS = 3 // Maksimum ardışık hata sayısı
        private const val MAX_BACKOFF_MILLIS = 8000L // Maksimum yeniden deneme bekleme süresi
    }

    /**
     * Oynatıcıdan bir hata geldiğinde bu fonksiyon çağrılır.
     */
    fun handlePlayerError(error: PlaybackException) {
        consecutiveErrors++
        if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
            onStateChange(State.FAILED)
            return
        }

        onStateChange(State.RECOVERING)

        scope.launch {
            delay(backoffMillis)

            // Hâlâ bir medya öğesi varsa ve oynatıcı durdurulmadıysa yeniden deneme yap
            if (player.currentMediaItem != null && player.playbackState != Player.STATE_IDLE) {
                player.prepare()
                player.play()
            }

            // Bir sonraki deneme için bekleme süresini artır
            backoffMillis = (backoffMillis * 2).coerceAtMost(MAX_BACKOFF_MILLIS)
        }
    }

    /**
     * Oynatma başarıyla başladığında hata sayacını sıfırlar.
     */
    fun notifyPlaybackStarted() {
        if (consecutiveErrors == 0) return // Zaten hata yoksa işlem yapma
        consecutiveErrors = 0
        backoffMillis = 1000L
        onStateChange(State.IDLE)
    }

    /**
     * Kaynakları temizler.
     */
    fun release() {
        scope.cancel()
    }
}