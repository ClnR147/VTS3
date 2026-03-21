package com.example.vtsdaily3.ui.common.importing

import android.content.Context
import android.content.Intent
import android.net.Uri

object VtsFileImporter {

    fun <T> importFromUri(
        context: Context,
        uri: Uri,
        parser: (String) -> T
    ): ImportResult<T> {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Some providers do not support persistable permissions.
        }

        val text = try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                reader.readText()
            }
        } catch (e: Exception) {
            return ImportResult.Error(
                e.message ?: "Unable to read selected file."
            )
        }

        if (text.isNullOrBlank()) {
            return ImportResult.Error("Selected file was empty.")
        }

        return try {
            ImportResult.Success(parser(text))
        } catch (e: Exception) {
            ImportResult.Error(
                e.message ?: "Import failed while parsing file."
            )
        }
    }
}