package fr.marstech.mtlinkspray.domain.shared

/** One-way hashing of secrets (API key secrets, paste passwords), implemented with BCrypt. */
interface PasswordHasher {
    fun hash(raw: String): String

    fun matches(raw: String, hash: String): Boolean
}
