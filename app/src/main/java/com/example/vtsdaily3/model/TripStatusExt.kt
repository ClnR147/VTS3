package com.example.vtsdaily3.model
fun TripStatus.isOtherBucket(): Boolean =
    this == TripStatus.REMOVED ||
            this == TripStatus.NOSHOW ||
            this == TripStatus.CANCELLED

fun TripStatus.otherLabel(): String = when (this) {
    TripStatus.CANCELLED -> "CANCEL"
    TripStatus.NOSHOW    -> "NO SHOW"
    TripStatus.REMOVED   -> "REMOVED"
    else                 -> ""
}
