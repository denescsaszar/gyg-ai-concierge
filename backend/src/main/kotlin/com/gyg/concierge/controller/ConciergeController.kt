package com.gyg.concierge.controller

import com.gyg.concierge.model.ChatRequest
import com.gyg.concierge.model.ChatResponse
import com.gyg.concierge.service.ConciergeService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/concierge")
class ConciergeController(private val conciergeService: ConciergeService) {

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): ChatResponse {
        return conciergeService.chat(request)
    }
}
