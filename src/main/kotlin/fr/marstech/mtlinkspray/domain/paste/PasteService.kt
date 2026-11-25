package fr.marstech.mtlinkspray.domain.paste
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.domain.shared.PasswordHasher
import org.springframework.stereotype.Service
import java.time.LocalDateTime.now
import java.util.*

@Service
class PasteService(
    private val pasteStore: PasteStore,
    private val passwordHasher: PasswordHasher,
) {

    fun createPaste(draft: PasteDraft, client: ClientContext): String =
        pasteStore.create(
            Paste(
                id = UUID.randomUUID().toString(),
                title = draft.title,
                content = draft.content,
                language = PastebinTextLanguageEnum.fromNameOrDefault(draft.language),
                passwordHash = draft.password?.let { hashPassword(it) },
                expiresAt = ExpirationEnum.fromExpirationOrError(draft.expiration).let {
                    now().plus(it.temporalAmount)
                },
                isPrivate = draft.isPrivate,
                isPasswordProtected = !draft.password.isNullOrBlank(),
                authorIpAddress = client.ipAddress,
            )
        ).id

    fun isPasswordProtected(id: String): Boolean = getPaste(id).isPasswordProtected

    fun getPaste(id: String, password: String?): Paste = getPaste(id).also {
        if (!checkPassword(password, it)) throw IllegalAccessException("Invalid password")
    }

    private fun getPaste(id: String): Paste =
        pasteStore.findById(id) ?: throw NoSuchElementException("Paste not found")

    fun deletePaste(id: String) {
        pasteStore.deleteById(id)
    }

    fun hashPassword(password: String): String = passwordHasher.hash(password)

    private fun checkPassword(password: String, passwordHash: String): Boolean =
        passwordHasher.matches(password, passwordHash)

    private fun checkPassword(password: String?, paste: Paste): Boolean = when {
        paste.isPasswordProtected -> when {
            paste.passwordHash.isNullOrBlank() -> true
            password.isNullOrBlank() -> false
            else -> checkPassword(password, paste.passwordHash)
        }

        else -> true
    }

}