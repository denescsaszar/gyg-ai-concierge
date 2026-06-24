package com.gyg.concierge.model

import java.time.Instant

data class StatusResponse(
    val serviceName: String,
    val status: String,
    val applicationVersion: String,
    val activeAiModel: String,
    val timestamp: Instant
)
