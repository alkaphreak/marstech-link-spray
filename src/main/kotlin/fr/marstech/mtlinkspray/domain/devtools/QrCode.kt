package fr.marstech.mtlinkspray.domain.devtools

/** What to render: [fg] and [bg] are RRGGBB hex values, [size] the side in pixels (validated by the caller). */
data class QrCode(
    val text: String,
    val size: Int,
    val fg: String,
    val bg: String,
    val svg: Boolean,
)
