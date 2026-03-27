package com.gymtracker.app.util

import java.text.Normalizer
import java.util.Locale

fun String.normalizeForSearch(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return normalized
        .replace("\\p{M}+".toRegex(), "")
        .lowercase(Locale.getDefault())
}

fun matchesSearch(candidate: String, query: String): Boolean {
    if (query.isBlank()) return false
    return candidate.normalizeForSearch().contains(query.normalizeForSearch())
}

