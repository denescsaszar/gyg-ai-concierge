package com.gyg.concierge.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Fixed-window rate limiter, keyed by client IP.
 *
 * Deliberately in-memory and dependency-free: the service runs as a single free
 * instance, so there is nothing to share state with. If this ever scales past
 * one instance, swap the map for Redis — the interface stays the same.
 */
@Component
class RateLimitInterceptor(
    private val properties: ConciergeProperties,
    private val clock: Clock = Clock.systemUTC(),
) : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(RateLimitInterceptor::class.java)
    private val counters = ConcurrentHashMap<String, Window>()

    private data class Window(val startedAtSeconds: Long, val count: AtomicInteger)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val limit = properties.rateLimit
        val now = clock.instant().epochSecond
        val key = clientKey(request)

        evictStaleWindows(now, limit.windowSeconds)

        val window = counters.compute(key) { _, existing ->
            if (existing == null || now - existing.startedAtSeconds >= limit.windowSeconds) {
                Window(now, AtomicInteger(1))
            } else {
                existing.count.incrementAndGet()
                existing
            }
        }!!

        val used = window.count.get()
        val resetInSeconds = (window.startedAtSeconds + limit.windowSeconds - now).coerceAtLeast(0)

        response.setHeader("X-RateLimit-Limit", limit.requestsPerWindow.toString())
        response.setHeader("X-RateLimit-Remaining", (limit.requestsPerWindow - used).coerceAtLeast(0).toString())

        if (used > limit.requestsPerWindow) {
            log.warn("Rate limit exceeded for {} ({} requests in window)", key, used)
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.setHeader("Retry-After", resetInSeconds.toString())
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write(
                """{"message":"You're sending messages faster than the concierge can answer. """ +
                    """Please wait $resetInSeconds seconds and try again.","recommendedActivityIds":[]}""",
            )
            return false
        }

        return true
    }

    /** Keeps the map from growing without bound on a long-lived instance. */
    private fun evictStaleWindows(now: Long, windowSeconds: Long) {
        if (counters.size < EVICTION_THRESHOLD) return
        counters.entries.removeIf { now - it.value.startedAtSeconds >= windowSeconds * 2 }
    }

    /**
     * Render terminates TLS upstream, so the socket address is always the proxy.
     * The first hop in X-Forwarded-For is the real client.
     */
    private fun clientKey(request: HttpServletRequest): String =
        request.getHeader("X-Forwarded-For")
            ?.substringBefore(',')
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: request.remoteAddr
            ?: "unknown"

    private companion object {
        const val EVICTION_THRESHOLD = 256
    }
}
