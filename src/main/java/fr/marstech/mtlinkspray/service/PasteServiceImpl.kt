package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.PasteEntity
import fr.marstech.mtlinkspray.repository.PasteRepository
import fr.marstech.mtlinkspray.utils.NetworkUtils
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period
import java.util.*

@Service
class PasteServiceImpl(private val pasteRepository: PasteRepository) : PasteService {

    private val passwordEncoder = BCryptPasswordEncoder()

    override fun createPaste(request: PasteRequest, httpServletRequest: HttpServletRequest): String =
        pasteRepository.save(
            PasteEntity(
                id = UUID.randomUUID().toString(),
                title = request.title,
                content = request.content,
                language = request.language,
                passwordHash = request.password?.let { hashPassword(it) },
                expiresAt = calculateExpiration(request.expiration),
                isPrivate = request.isPrivate,
                isPasswordProtected = !request.password.isNullOrBlank(),
                author = HistoryItem(
                    ipAddress = NetworkUtils.getIpAddress(httpServletRequest), action = "CREATE_PASTE"
                ),
            )
        ).id

    override fun getPaste(id: String, password: String?): PasteEntity =
        pasteRepository.findById(id).orElseThrow { NoSuchElementException("Paste not found") }.also {
            if (!checkPassword(password, it)) throw IllegalAccessException("Invalid password")
        }

    override fun deletePaste(id: String) {
        pasteRepository.deleteById(id)
    }

    private fun calculateExpiration(expiration: String): LocalDateTime = LocalDateTime.now().plus(
        when (expiration) {
            "10m" -> Duration.ofMinutes(10)
            "1h" -> Duration.ofHours(1)
            "1d" -> Duration.ofDays(1)
            "1w" -> Period.ofWeeks(1)
            "1m" -> Period.ofMonths(1)
            "never" -> Period.ofYears(100) // Effectively never
            else -> throw IllegalArgumentException("Invalid expiration value")
        }
    )

    fun hashPassword(password: String): String = passwordEncoder.encode(password)

    private fun checkPassword(password: String, passwordHash: String): Boolean =
        passwordEncoder.matches(password, passwordHash)

    private fun checkPassword(password: String?, pasteEntity: PasteEntity): Boolean = when {
        pasteEntity.isPasswordProtected -> when {
            pasteEntity.passwordHash.isNullOrBlank() -> true
            password.isNullOrBlank() -> false
            else -> checkPassword(password, pasteEntity.passwordHash)
        }

        else -> true
    }

}