package io.github.keep2iron.pejoy.utilities

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri

class CaptureMediaScanner constructor(
    val context: Context,
    val path: String,
    val onScanerComplete: (() -> Unit)? = null
) : MediaScannerConnection.MediaScannerConnectionClient {

    private var scanner: MediaScannerConnection? = null

    init {
        scanner = MediaScannerConnection(context, this)
        scanner!!.connect()
    }

    override fun onMediaScannerConnected() {
        scanner?.scanFile(path, null)
    }

    override fun onScanCompleted(path: String, uri: Uri) {
        scanner?.disconnect()
        scanner = null
        onScanerComplete?.invoke()
    }
}