package com.example.vtsdaily3.feature_lookup.ui.state

import com.example.vtsdaily3.feature_lookup.domain.LookupPassengerDetail
import com.example.vtsdaily3.feature_lookup.data.LookupRow
import com.example.vtsdaily3.feature_lookup.domain.LookupSummary
import com.example.vtsdaily3.feature_lookup.domain.buildLookupSummaries
data class LookupUiState(
    var rows: List<LookupRow> = emptyList(),
    val summaries: List<LookupSummary> = emptyList(),
    val selectedPassenger: String? = null,
    val selectedDetail: LookupPassengerDetail? = null,
    val errorMessage: String? = null
)

fun buildLookupUiState(rows: List<LookupRow>): LookupUiState {
    val summaries = buildLookupSummaries(rows)

    return LookupUiState(
        rows = rows,
        summaries = summaries,
        errorMessage = if (rows.isEmpty()) "No lookup rows found" else null
    )
}




