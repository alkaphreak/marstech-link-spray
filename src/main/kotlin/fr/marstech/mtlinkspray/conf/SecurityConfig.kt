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

/**
 * HTTP Basic auth restricted to the "/admin" path tree (currently: the API keys admin page - MLS-203).
 * Every other route stays publicly accessible, matching the app's current behaviour before this
 * config was introduced. CSRF protection is disabled: the app has no session-based state to
 * protect against, and none of the existing forms (spray, paste, dashboard, ...) carry a CSRF
 * token, so enabling it would break them.
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
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/admin/**").authenticated()
                it.anyRequest().permitAll()
            }
            .httpBasic { }
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
}
