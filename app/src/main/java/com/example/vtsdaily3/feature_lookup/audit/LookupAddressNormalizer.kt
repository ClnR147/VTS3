package com.example.vtsdaily3.feature_lookup.audit

object LookupAddressNormalizer {

    fun normalize(address: String?): String {
        if (address.isNullOrBlank()) return ""

        return address
            .lowercase()
            .trim()
            .replace(Regex("[.,#\\-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}