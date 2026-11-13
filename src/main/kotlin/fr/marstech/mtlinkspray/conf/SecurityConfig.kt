package fr.marstech.mtlinkspray.conf

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.NegatedRequestMatcher

/**
 * HTTP Basic auth restricted to the "/admin" path tree (currently: the API keys admin page - MLS-203).
 * Every other route stays publicly accessible, matching the app's current behaviour before this
 * config was introduced. CSRF protection applies to the admin path tree only: a browser that has
 * cached the Basic credentials could otherwise be driven cross-site to create or toggle API keys.
 * The admin forms use th:action, so Spring Security injects the token automatically. The public
 * forms (spray, paste, dashboard, ...) are unauthenticated and carry no token, so they stay exempt.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Value("\${mt.link-spray.admin.username}") private val adminUsername: String,
    @Value("\${mt.link-spray.admin.password}") private val adminPassword: String,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.ignoringRequestMatchers(NegatedRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher("/admin/**"))) }
            .authorizeHttpRequests {
                it.requestMatchers("/admin/**").authenticated()
                it.anyRequest().permitAll()
            }
            .httpBasic { }
            .headers { it.contentSecurityPolicy { csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY).reportOnly() } }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val admin = User.withUsername(adminUsername)
            .password(passwordEncoder.encode(adminPassword))
            .roles("ADMIN")
            .build()
        return InMemoryUserDetailsManager(admin)
    }

    companion object {
        /**
         * Sent as Content-Security-Policy-Report-Only until every view is checked in a browser (MLS-269):
         * violations only show in the console. Images allow any https origin for dashboard avatars.
         */
        const val CONTENT_SECURITY_POLICY = "default-src 'self'; script-src 'self' https://cdn.jsdelivr.net; " +
            "style-src 'self' https://cdn.jsdelivr.net; font-src 'self' https://cdn.jsdelivr.net; " +
            "img-src 'self' data: https:; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; " +
            "form-action 'self'; object-src 'none'"
    }
}
