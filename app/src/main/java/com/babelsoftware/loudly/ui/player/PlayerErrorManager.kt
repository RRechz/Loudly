package com.babelsoftware.loudly.ui.player

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.babelsoftware.loudly.extensions.forceResume
import kotlinx.coroutines.*

/**
 * A smart, self-validating, and persistent helper class that handles errors.
 */
class PlayerErrorManager(
    private val player: Player,
    private val onStateChange: (State) -> Unit
) {
    enum class State { IDLE, RECOVERING, FAILED }
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var recoveryJob: Job? = null

    fun handlePlayerError(error: PlaybackException) {
        if (recoveryJob?.isActive == true) return

        when (error.errorCode) {
            PlaybackException.ERROR_CODE_DRM_PROVISIONING_FAILED,
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
            PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED -> {
                onStateChange(State.FAILED)
            }

            else -> {
                onStateChange(State.RECOVERING)
                recoveryJob = scope.launch {
                    var needsLongerDelay = false
                    while (isActive && !player.isPlaying && player.currentMediaItem != null) {
                        if (needsLongerDelay) {
                            delay(5000)
                        }

                        player.prepare()

                        var attempt = 0
                        while (isActive && attempt < 5 && !player.isPlaying) {
                            player.forceResume()
                            delay(1000)
                            attempt++
                        }
                        needsLongerDelay = !player.isPlaying
                    }
                    onStateChange(State.IDLE)
                }
            }
        }
    }

    /**
     * Called when playback has started successfully.
     */
    fun notifyPlaybackStarted() {
        if (recoveryJob?.isActive == true) {
            recoveryJob?.cancel()
        }
        onStateChange(State.IDLE)
    }
    fun release() {
        scope.cancel()
    }
}