package com.example.vtsdaily3.feature_clinics.export

import com.opencsv.CSVWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

class ClinicAddressCsvExporter {

    fun export(
        outputStream: OutputStream,
        candidates: List<ClinicAddressCandidate>
    ) {
        OutputStreamWriter(outputStream).use { writer ->
            CSVWriter(writer).use { csv ->
                csv.writeNext(arrayOf("trip_type", "address", "drive_date"))

                candidates.forEach { candidate ->
                    csv.writeNext(
                        arrayOf(
                            candidate.tripType.name,
                            candidate.address,
                            candidate.driveDate
                        )
                    )
                }
            }
        }
    }
}