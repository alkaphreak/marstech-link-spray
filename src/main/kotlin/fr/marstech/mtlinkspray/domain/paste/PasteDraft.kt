package fr.marstech.mtlinkspray.domain.paste

/** A paste to create, as submitted: [language] and [expiration] are parsed by the domain. */
data class PasteDraft(
    val title: String? = null,
    val content: String,
    val language: String = PastebinTextLanguageEnum.TEXT.name,
    val password: String? = null,
    val expiration: String = ExpirationEnum.NEVER.expiration,
    val isPrivate: Boolean = false,
)
