package fr.marstech.mtlinkspray.enums

enum class PastebinTextLanguageEnum {
    TEXT,
    KOTLIN;

    companion object {
        fun fromName(language: String): PastebinTextLanguageEnum? =
            entries.firstOrNull { it.name == language }

        fun fromNameOrDefault(language: String): PastebinTextLanguageEnum =
            fromName(language) ?: TEXT
    }
}