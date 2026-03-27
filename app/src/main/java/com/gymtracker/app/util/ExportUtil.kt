package com.gymtracker.app.util

import android.content.Context
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.gson.GsonBuilder
import com.gymtracker.app.data.model.ExportData
import com.gymtracker.app.data.repository.GymRepository
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fonction utilitaire pour exporter les données JSON automatiquement
 * @param context Contexte Android
 * @param repository Repository pour récupérer les données
 * @param onSuccess Callback en cas de succès (recoit le nom du fichier)
 * @param onError Callback en cas d'erreur (recoit le message d'erreur)
 */
suspend fun autoExportData(
    context: Context,
    repository: GymRepository,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val exportData = repository.exportAllData()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(exportData)

        val dateFormat = SimpleDateFormat("dd-MM-yy-HH'h'mm", Locale.getDefault())
        val fileName = "${dateFormat.format(Date())}.json"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                onSuccess(fileName)
            } ?: run {
                onError("Impossible de créer le fichier")
            }
        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { it.write(json.toByteArray()) }
            onSuccess(fileName)
        }
    } catch (e: Exception) {
        onError(e.message ?: "Erreur inconnue")
    }
}

