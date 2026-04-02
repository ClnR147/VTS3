package com.example.vtsdaily3.feature_clinics.export

import android.content.ContentResolver
import android.net.Uri
import com.example.vtsdaily3.feature_lookup.data.LookupRow

class ClinicAddressExportWriter(
    private val contentResolver: ContentResolver,
    private val useCase: ExportClinicCandidatesUseCase = ExportClinicCandidatesUseCase()
) {
    fun exportToUri(
        uri: Uri,
        rows: List<LookupRow>,
        knownClinicAddresses: Set<String>,
        options: ClinicAddressExportOptions = ClinicAddressExportOptions()
    ): Int {
        val outputStream = contentResolver.openOutputStream(uri)
            ?: error("Unable to open output stream for export.")

        outputStream.use { stream ->
            return useCase.execute(
                rows = rows,
                knownClinicAddresses = knownClinicAddresses,
                outputStream = stream,
                options = options
            )
        }
    }
}