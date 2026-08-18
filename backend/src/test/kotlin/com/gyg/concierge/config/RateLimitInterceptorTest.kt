package com.gyg.concierge.config

import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RateLimitInterceptorTest {

    private val start = Instant.parse("2026-01-01T00:00:00Z")

    /** Lets a test move time forward without sleeping. */
    private class MovableClock(private var now: Instant) : Clock() {
        override fun instant(): Instant = now
        override fun getZone(): ZoneOffset = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId): Clock = this
        fun advance(duration: Duration) {
            now = now.plus(duration)
        }
    }

    private fun properties(limit: Int = 3, windowSeconds: Long = 60) =
        ConciergeProperties(rateLimit = ConciergeProperties.RateLimit(limit, windowSeconds))

    private fun requestFrom(ip: String) = MockHttpServletRequest().apply {
        addHeader("X-Forwarded-For", ip)
    }

    private fun call(interceptor: RateLimitInterceptor, ip: String): MockHttpServletResponse {
        val response = MockHttpServletResponse()
        interceptor.preHandle(requestFrom(ip), response, Any())
        return response
    }

    @Test
    fun `allows requests up to the limit`() {
        val interceptor = RateLimitInterceptor(properties(), MovableClock(start))

        repeat(3) {
            assertTrue(interceptor.preHandle(requestFrom("1.2.3.4"), MockHttpServletResponse(), Any()))
        }
    }

    @Test
    fun `blocks the request that exceeds the limit`() {
        val interceptor = RateLimitInterceptor(properties(), MovableClock(start))
        repeat(3) { call(interceptor, "1.2.3.4") }

        val response = MockHttpServletResponse()
        val allowed = interceptor.preHandle(requestFrom("1.2.3.4"), response, Any())

        assertFalse(allowed)
        assertEquals(429, response.status)
        assertTrue(response.contentAsString.contains("recommendedActivityIds"))
    }

    @Test
    fun `tells the caller when to retry`() {
        val interceptor = RateLimitInterceptor(properties(), MovableClock(start))
        repeat(3) { call(interceptor, "1.2.3.4") }

        val response = call(interceptor, "1.2.3.4")

        assertEquals("60", response.getHeader("Retry-After"))
        assertEquals("0", response.getHeader("X-RateLimit-Remaining"))
    }

    @Test
    fun `limits are per client, not global`() {
        val interceptor = RateLimitInterceptor(properties(), MovableClock(start))
        repeat(3) { call(interceptor, "1.2.3.4") }

        assertTrue(interceptor.preHandle(requestFrom("5.6.7.8"), MockHttpServletResponse(), Any()))
    }

    @Test
    fun `the window resets`() {
        val clock = MovableClock(start)
        val interceptor = RateLimitInterceptor(properties(), clock)
        repeat(4) { call(interceptor, "1.2.3.4") }

        clock.advance(Duration.ofSeconds(61))

        assertTrue(interceptor.preHandle(requestFrom("1.2.3.4"), MockHttpServletResponse(), Any()))
    }

    @Test
    fun `uses the first hop of X-Forwarded-For, not the proxy address`() {
        val interceptor = RateLimitInterceptor(properties(limit = 1), MovableClock(start))

        val first = MockHttpServletRequest().apply { addHeader("X-Forwarded-For", "9.9.9.9, 10.0.0.1") }
        val second = MockHttpServletRequest().apply { addHeader("X-Forwarded-For", "9.9.9.9, 10.0.0.2") }

        assertTrue(interceptor.preHandle(first, MockHttpServletResponse(), Any()))
        assertFalse(interceptor.preHandle(second, MockHttpServletResponse(), Any()))
    }

    @Test
    fun `falls back to the socket address when there is no proxy header`() {
        val interceptor = RateLimitInterceptor(properties(limit = 1), MovableClock(start))
        val request = MockHttpServletRequest().apply { remoteAddr = "127.0.0.1" }

        assertTrue(interceptor.preHandle(request, MockHttpServletResponse(), Any()))
        assertFalse(interceptor.preHandle(request, MockHttpServletResponse(), Any()))
    }
}
