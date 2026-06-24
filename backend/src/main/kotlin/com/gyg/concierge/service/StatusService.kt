package com.gyg.concierge.service

import com.gyg.concierge.model.StatusResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class StatusService(
    @Value("\${spring.application.name}") private val serviceName: String,
    @Value("\${app.version}") private val applicationVersion: String
) {

    fun getStatus(): StatusResponse = StatusResponse(
        serviceName = serviceName,
        status = "ok",
        applicationVersion = applicationVersion,
        activeAiModel = ConciergeService.ACTIVE_AI_MODEL.asString(),
        timestamp = Instant.now()
    )
}
