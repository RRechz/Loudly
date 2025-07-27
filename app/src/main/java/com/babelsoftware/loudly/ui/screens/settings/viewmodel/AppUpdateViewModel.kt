package com.babelsoftware.loudly.ui.screens.settings.viewmodel

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.loudly.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class UpdateState {
    object Idle : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class ReadyToInstall(val apkUri: Uri) : UpdateState()
    data class Failed(val error: String) : UpdateState()
}

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val app: Application
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState = _updateState.asStateFlow()

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 600000L // Timeout for the entire request (default 10 minutes)
            connectTimeoutMillis = 60000L  // Timeout for connecting to the server (default 1 minute)
            socketTimeoutMillis = 60000L   // Wait time between data packets (default 1 minute)
        }
    }

    fun downloadAndInstallApk(downloadUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _updateState.value = UpdateState.Downloading(0)
                val file = File(app.cacheDir, "update.apk")

                client.prepareGet(downloadUrl).execute { httpResponse ->
                    val fileSize = httpResponse.contentLength() ?: 0L
                    val fileOutputStream = file.outputStream()

                    val byteChannel = httpResponse.bodyAsChannel()
                    var totalBytesRead = 0L

                    val buffer = ByteArray(1024)
                    var bytesRead: Int

                    while (byteChannel.readAvailable(buffer).also { bytesRead = it } != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        if (fileSize > 0) {
                            val progress = ((totalBytesRead * 100) / fileSize).toInt()
                            _updateState.value = UpdateState.Downloading(progress)
                        }
                    }

                    fileOutputStream.flush()
                    fileOutputStream.close()
                }

                val authority = "${BuildConfig.APPLICATION_ID}.provider"
                val apkUri = FileProvider.getUriForFile(app, authority, file)
                _updateState.value = UpdateState.ReadyToInstall(apkUri)

            } catch (e: Exception) {
                e.printStackTrace()
                _updateState.value = UpdateState.Failed("Güncelleme indirilirken bir hata oluştu: ${e.message}")
            }
        }
    }

    fun resetState() {
        _updateState.value = UpdateState.Idle
    }
}