package com.example.vtsdaily3.feature_clinics.export

import com.example.vtsdaily3.feature_lookup.data.LookupRow
import java.io.OutputStream

class ExportClinicCandidatesUseCase(
    private val extractor: ClinicAddressCandidateExtractor = ClinicAddressCandidateExtractor(),
    private val csvExporter: ClinicAddressCsvExporter = ClinicAddressCsvExporter()
) {
    fun execute(
        rows: List<LookupRow>,
        knownClinicAddresses: Set<String>,
        outputStream: OutputStream,
        options: ClinicAddressExportOptions = ClinicAddressExportOptions()
    ): Int {
        val candidates = extractor.extract(
            rows = rows,
            knownClinicAddresses = knownClinicAddresses,
            options = options
        )
        csvExporter.export(outputStream, candidates)
        return candidates.size
    }
}