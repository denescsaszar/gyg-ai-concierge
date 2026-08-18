package com.gyg.concierge.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

/**
 * Kept in its own configuration class rather than on WebConfig: WebConfig injects
 * RateLimitInterceptor, which injects the Clock, so declaring the bean there
 * would close a dependency cycle.
 */
@Configuration
class ClockConfig {

    /** Injectable so time-dependent logic can be tested without sleeping. */
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
