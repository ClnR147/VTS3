package com.example.vtsdaily3.feature_clinics.data

import android.content.Context
import android.net.Uri
import com.opencsv.CSVWriter
import java.io.OutputStreamWriter

object ClinicCsvExporter {

    fun exportToUri(
        context: Context,
        uri: Uri,
        clinics: List<ClinicEntry>
    ) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            CSVWriter(OutputStreamWriter(outputStream)).use { writer ->
                writer.writeNext(arrayOf("name", "address", "phone"))

                clinics.forEach { clinic ->
                    writer.writeNext(
                        arrayOf(
                            clinic.name.trim(),
                            clinic.address.trim(),
                            clinic.phone.trim()
                        )
                    )
                }
            }
        }
    }
}