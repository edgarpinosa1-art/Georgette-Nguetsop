package com.example.data.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TxtExporter {

    fun copyToClipboard(context: Context, label: String, content: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
    }

    suspend fun writeTextToUri(context: Context, uri: Uri, content: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.bufferedWriter().use { writer ->
                        writer.write(content)
                    }
                } ?: error("Unable to open output stream for destination file.")
            }
        }
}
