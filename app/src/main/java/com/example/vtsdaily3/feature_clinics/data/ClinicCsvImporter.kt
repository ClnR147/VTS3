package com.example.vtsdaily3.feature_clinics.data

import android.content.Context
import android.net.Uri
import com.opencsv.CSVReader
import java.io.InputStreamReader

object ClinicCsvImporter {

    fun importFromUri(
        context: Context,
        uri: Uri
    ): List<ClinicEntry> {
        val result = mutableListOf<ClinicEntry>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            CSVReader(InputStreamReader(inputStream)).use { reader ->
                val rows = reader.readAll()
                if (rows.isEmpty()) return emptyList()

                val dataRows = if (rows.first()[0].lowercase() == "name") {
                    rows.drop(1)
                } else {
                    rows
                }

                dataRows.forEach { row ->
                    val name = row.getOrNull(0)?.trim().orEmpty()
                    val address = row.getOrNull(1)?.trim().orEmpty()
                    val phone = row.getOrNull(2)?.trim().orEmpty()

                    if (name.isBlank() || address.isBlank()) return@forEach

                    result.add(
                        ClinicEntry(
                            name = name,
                            address = address,
                            phone = phone
                        )
                    )
                }
            }
        }

        return result
            .distinctBy { it.address.lowercase() }
    }
}