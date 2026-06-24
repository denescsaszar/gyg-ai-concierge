package com.gyg.concierge.controller

import com.gyg.concierge.model.StatusResponse
import com.gyg.concierge.service.StatusService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class StatusController(private val statusService: StatusService) {

    @GetMapping("/status")
    fun status(): StatusResponse = statusService.getStatus()
}
