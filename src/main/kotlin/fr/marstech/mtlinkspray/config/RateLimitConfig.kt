package fr.marstech.mtlinkspray.config
import fr.marstech.mtlinkspray.adapter.outbound.ratelimit.AnonymousQuota
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Per-IP rate limit on the public routes that write (links, pastes, dashboards) or send mail
 * (abuse reports), API and form alike (MLS-161, ADR 0006). A shortened spray pays one token per
 * link in SprayService instead. A 429 goes through GlobalRestExceptionHandler with Retry-After.
 */
@Configuration
class RateLimitConfig(
    @Value("\${mt.link-spray.rate-limit.writes.quota:200}") private val writesQuota: Int,
    @Value("\${mt.link-spray.rate-limit.writes.window-ms:600000}") private val writesWindowMillis: Long,
    @Value("\${mt.link-spray.rate-limit.abuse.quota:5}") abuseQuota: Int,
    @Value("\${mt.link-spray.rate-limit.abuse.window-ms:3600000}") abuseWindowMillis: Long,
) : WebMvcConfigurer {

    private val abuse = AnonymousQuota("Abuse report", abuseQuota, abuseWindowMillis, "retry later")

    /** One budget per IP shared by every write route, so switching routes does not reset it. */
    @Bean
    fun writeQuota() = AnonymousQuota("Write", writesQuota, writesWindowMillis, "retry later")

    override fun addInterceptors(registry: InterceptorRegistry) {
        val writes = writeQuota()
        registry.addInterceptor(RateLimitInterceptor(writes, setOf("POST", "PUT"))).addPathPatterns(
            "/api/url-shortener", "/api/paste", "/api/dashboard", "/api/dashboard/*",
            "/shortener", "/paste", "/dashboard",
        )
        // Spring MVC sends HEAD to the GET handler, which writes too
        registry.addInterceptor(RateLimitInterceptor(writes, setOf("GET", "HEAD")))
            .addPathPatterns("/api/url-shortener/shorten")
        registry.addInterceptor(RateLimitInterceptor(abuse, setOf("POST"))).addPathPatterns("/abuse")
    }
}

class RateLimitInterceptor(private val quota: AnonymousQuota, private val methods: Set<String>) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.method in methods) quota.count(NetworkUtils.getIpAddress(request) ?: "unknown")
        return true
    }
}
