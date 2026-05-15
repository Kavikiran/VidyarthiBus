package com.kavikiran.vidyarthibus.model

data class CrowdReport(
    val status: String = "EMPTY",
    val reportedBy: String = "",
    val reporterName: String = "",
    val reportedAt: Long = 0L,
    val expiresAt: Long = 0L
)