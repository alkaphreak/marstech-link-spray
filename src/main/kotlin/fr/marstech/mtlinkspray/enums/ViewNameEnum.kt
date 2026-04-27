package fr.marstech.mtlinkspray.enums

enum class ViewNameEnum(
    val viewName: String,
    val viewPage: String,
    val orderInMenu: Int = 99,
    val isInMenu: Boolean = true
) {
    SPRAY(viewName = "spray", viewPage = "Spray", orderInMenu = 0),
    SHORTENER(viewName = "shortener", viewPage = "Shortener", orderInMenu = 1),
    UUID(viewName = "uuid", viewPage = "Uuid", orderInMenu = 2),
    RANDOM(viewName = "random", viewPage = "Random", orderInMenu = 3),
    PASTE(viewName = "paste", viewPage = "Pastebin", orderInMenu = 4),
    ABUSE(viewName = "abuse", viewPage = "Abuse"),

    DASHBOARD(viewName = "dashboard", viewPage = "Dashboard"),

    HOME(viewName = "index", viewPage = "Home", orderInMenu = -1, isInMenu = false),
    ERROR(viewName = "error", viewPage = "Error", orderInMenu = -1, isInMenu = false);

    companion object {
        @JvmStatic
        fun inMenu(): List<ViewNameEnum> = entries
            .filter(ViewNameEnum::isInMenu)
            .sortedBy(ViewNameEnum::orderInMenu)
    }
}
