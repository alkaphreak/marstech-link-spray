package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.PasteRequest
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.PasteEntity
import fr.marstech.mtlinkspray.enums.ExpirationEnum
import fr.marstech.mtlinkspray.enums.PastebinTextLanguageEnum
import fr.marstech.mtlinkspray.repository.PasteRepository
import fr.marstech.mtlinkspray.utils.NetworkUtils
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime.now
import java.util.*

@Service
class PasteServiceImpl(
    private val pasteRepository: PasteRepository
) : PasteService {

    private val passwordEncoder = BCryptPasswordEncoder()

    override fun createPaste(
        request: PasteRequest,
        httpServletRequest: HttpServletRequest
    ): String =
        pasteRepository.save(
            PasteEntity(
                id = UUID.randomUUID().toString(),
                title = request.title,
                content = request.content,
                language = PastebinTextLanguageEnum.fromNameOrDefault(request.language),
                passwordHash = request.password?.let { hashPassword(it) },
                expiresAt = ExpirationEnum.fromExpirationOrError(request.expiration).let {
                    now().plus(it.temporalAmount)
                },
                isPrivate = request.isPrivate,
                isPasswordProtected = !request.password.isNullOrBlank(),
                author = HistoryItem(
                    ipAddress = NetworkUtils.getIpAddress(httpServletRequest),
                    action = "CREATE_PASTE"
                ),
            )
        ).id

    override fun isPassordProtected(id: String): Boolean = getPaste(id).isPasswordProtected

    override fun getPaste(id: String, password: String?): PasteEntity = getPaste(id).also {
        if (!checkPassword(password, it)) throw IllegalAccessException("Invalid password")
    }

    private fun getPaste(id: String): PasteEntity =
        pasteRepository.findById(id).orElseThrow { NoSuchElementException("Paste not found") }

    override fun deletePaste(id: String) {
        pasteRepository.deleteById(id)
    }

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