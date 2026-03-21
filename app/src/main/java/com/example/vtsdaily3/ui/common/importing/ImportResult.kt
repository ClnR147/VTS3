package com.example.vtsdaily3.ui.common.importing

sealed class ImportResult<out T> {
    data class Success<T>(val data: T) : ImportResult<T>()
    data class Error(val message: String) : ImportResult<Nothing>()
}