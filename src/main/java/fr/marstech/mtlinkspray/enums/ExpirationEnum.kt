package fr.marstech.mtlinkspray.enums

enum class ExpirationEnum(
    val expiration: String
) {
    NEVER("NEVER"),
    ONE_HOUR("ONE_HOUR"),
    ONE_DAY("ONE_DAY"),
    ONE_WEEK("ONE_WEEK"),
    ONE_MONTH("ONE_MONTH"),
    ONE_YEAR("ONE_YEAR"),
}
