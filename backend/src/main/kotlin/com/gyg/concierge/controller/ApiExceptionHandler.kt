package com.gyg.concierge.controller

import com.gyg.concierge.model.ChatResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Turns validation failures into a shape the chat widget already knows how to
 * render, rather than Spring's default error body.
 */
@RestControllerAdvice
class ApiExceptionHandler {

    private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ChatResponse> {
        val reason = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: "The request could not be processed."

        log.info("Rejected invalid request: {}", reason)

        return ResponseEntity
            .badRequest()
            .body(ChatResponse(message = reason))
    }
}

// Deliberately no catch-all `Exception` handler: it would also swallow Spring's
// own 404/405 responses and turn them into 500s. Unexpected errors fall through
// to Spring's default handling, which is configured in application.properties
// not to leak messages or stack traces.
