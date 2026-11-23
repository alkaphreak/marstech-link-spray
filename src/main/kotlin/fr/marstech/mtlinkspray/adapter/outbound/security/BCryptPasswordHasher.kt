package fr.marstech.mtlinkspray.adapter.outbound.security

import fr.marstech.mtlinkspray.domain.shared.PasswordHasher
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHasher : PasswordHasher {

    private val encoder = BCryptPasswordEncoder()

    override fun hash(raw: String): String = encoder.encode(raw)!!

    override fun matches(raw: String, hash: String): Boolean = encoder.matches(raw, hash)
}
