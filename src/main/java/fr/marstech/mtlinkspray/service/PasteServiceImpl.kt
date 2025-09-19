package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.entity.Paste
import fr.marstech.mtlinkspray.repository.PasteRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class PasteServiceImpl(private val pasteRepository: PasteRepository) : PasteService {

    private val passwordEncoder = BCryptPasswordEncoder()

    override fun createPaste(request: PasteRequest): String = pasteRepository.save(
        Paste(
            id = UUID.randomUUID().toString(),
            title = request.title,
            content = request.content,
            language = request.language,
            passwordHash = request.password?.let { hashPassword(it) },
            expiresAt = calculateExpiration(request.expiration),
            isPrivate = request.isPrivate,
            isPasswordProtected = !request.password.isNullOrBlank(),
            author = request.author,
        )
    ).id

    override fun getPaste(id: String, password: String?): Paste {
        val paste = pasteRepository.findById(id).orElseThrow { NoSuchElementException("Paste not found") }
        if (paste.isPasswordProtected && !checkPassword(password!!, paste.passwordHash!!)) {
            throw IllegalAccessException("Invalid password")
        }
        return paste
    }

    override fun deletePaste(id: String) {
        pasteRepository.deleteById(id)
    }

    private fun calculateExpiration(expiration: String): LocalDateTime = LocalDateTime.now().plus(
        when (expiration) {
            "10m" -> java.time.Duration.ofMinutes(10)
            "1h" -> java.time.Duration.ofHours(1)
            "1d" -> java.time.Duration.ofDays(1)
            "1w" -> java.time.Period.ofWeeks(1)
            "1m" -> java.time.Period.ofMonths(1)
            "never" -> java.time.Period.ofYears(100) // Effectively never
            else -> throw IllegalArgumentException("Invalid expiration value")
        }
    )

    fun hashPassword(password: String): String = passwordEncoder.encode(password)

    private fun checkPassword(password: String, passwordHash: String): Boolean =
        passwordEncoder.matches(password, passwordHash)
}