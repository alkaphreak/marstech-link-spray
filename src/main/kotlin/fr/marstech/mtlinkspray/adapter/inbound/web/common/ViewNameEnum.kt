package fr.marstech.mtlinkspray.adapter.inbound.web.common
enum class ViewNameEnum(
    val viewName: String,
    val viewPage: String,
    val orderInMenu: Int = 99,
    val isInMenu: Boolean = true,
    /** One line shown on the homepage tool card (MLS-271); required for every menu entry. */
    val description: String = ""
) {
    SPRAY(viewName = "spray", viewPage = "Spray", orderInMenu = 0, description = "Open many links from one link."),
    SHORTENER(viewName = "shortener", viewPage = "Shortener", orderInMenu = 1, description = "Shorten a long URL."),
    UUID(viewName = "uuid", viewPage = "Uuid", orderInMenu = 2, description = "Random v4 UUIDs."),
    RANDOM(viewName = "random", viewPage = "Random", orderInMenu = 3, description = "A random number in a range."),
    PASTE(viewName = "paste", viewPage = "Pastebin", orderInMenu = 4, description = "Share text by URL."),
    QR(viewName = "qr", viewPage = "QR Code", orderInMenu = 5, description = "Text or link to QR image."),
    MAILTO(viewName = "mailto", viewPage = "Mailto", orderInMenu = 6, description = "Prefilled mailto: links."),
    DNS(viewName = "dns", viewPage = "DNS", orderInMenu = 7, description = "Look up DNS records."),
    IP(viewName = "ip", viewPage = "IP", orderInMenu = 8, description = "Your IP, network, country and WebRTC leak check."),
    OCR(viewName = "ocr", viewPage = "OCR", orderInMenu = 9, description = "Text out of an image."),
    FINGERPRINT(viewName = "fingerprint", viewPage = "Fingerprint", orderInMenu = 10, description = "What your browser exposes."),
    SLEEP(viewName = "sleep", viewPage = "Sleep", orderInMenu = 11, description = "Bedtimes or wake-up times on 90-minute cycles."),
    CRON(viewName = "cron", viewPage = "Cron", orderInMenu = 12, description = "Scheduled HTTP pings with failure alerts."),
    ABUSE(viewName = "abuse", viewPage = "Abuse", description = "Report a link that misuses the service."),
    PRIVACY(viewName = "privacy", viewPage = "Privacy", orderInMenu = -1, isInMenu = false),

    DASHBOARD(viewName = "dashboard", viewPage = "Dashboard", description = "Your links on one shareable page."),

    ADMIN_API_KEYS(viewName = "admin/api-keys", viewPage = "API Keys Admin", orderInMenu = -1, isInMenu = false),
    ADMIN_SIGNATURES(viewName = "admin/signatures", viewPage = "Signatures Admin", orderInMenu = -1, isInMenu = false),

    HOME(viewName = "index", viewPage = "Home", orderInMenu = -1, isInMenu = false),
    ERROR(viewName = "error", viewPage = "Error", orderInMenu = -1, isInMenu = false);

    companion object {
        @JvmStatic
        fun inMenu(): List<ViewNameEnum> = entries
            .filter(ViewNameEnum::isInMenu)
            .sortedBy(ViewNameEnum::orderInMenu)
    }
}
