package com.gyg.concierge.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val rateLimitInterceptor: RateLimitInterceptor,
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:5173",
                "http://localhost:3000",
                "https://gyg-ai-concierge.vercel.app",
            )
            // Vercel preview deployments get a generated subdomain per commit.
            .allowedOriginPatterns("https://gyg-ai-concierge-*.vercel.app")
            .allowedMethods("GET", "POST", "OPTIONS")
            .allowedHeaders("Content-Type")
            // The API is stateless and uses no cookies or auth headers. Leaving
            // credentials on while accepting a wildcard *.vercel.app pattern would
            // let any site on that domain make credentialed calls to this API.
            .allowCredentials(false)
            // Cache the preflight so a POST /chat is one round trip, not two —
            // which matters most while the backend is cold-starting.
            .maxAge(3600)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/concierge/**")
    }
}
