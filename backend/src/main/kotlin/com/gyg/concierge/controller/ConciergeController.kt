package com.gyg.concierge.controller

import com.gyg.concierge.model.ChatRequest
import com.gyg.concierge.model.ChatResponse
import com.gyg.concierge.service.ConciergeService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/concierge")
class ConciergeController(private val conciergeService: ConciergeService) {

    @PostMapping("/chat")
    fun chat(@Valid @RequestBody request: ChatRequest): ChatResponse {
        return conciergeService.chat(request)
    }
}
