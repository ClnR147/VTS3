package com.example.vtsdaily3.lookup

data class LookupUiState(
    val rows: List<LookupRow> = emptyList(),
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

fun selectPassenger(
    state: LookupUiState,
    passengerName: String
): LookupUiState {
    val detail = buildLookupPassengerDetail(state.rows, passengerName)
    return state.copy(
        selectedPassenger = passengerName,
        selectedDetail = detail
    )
}

fun clearSelectedPassenger(state: LookupUiState): LookupUiState {
    return state.copy(
        selectedPassenger = null,
        selectedDetail = null
    )
}
