package fr.marstech.mtlinkspray.domain.shared

/**
 * One-way hashing of secrets, implemented with BCrypt. Used for API key secrets; paste passwords
 * move onto it with the `paste` domain (MLS-276).
 */
interface PasswordHasher {
    fun hash(raw: String): String

    fun matches(raw: String, hash: String): Boolean
}
