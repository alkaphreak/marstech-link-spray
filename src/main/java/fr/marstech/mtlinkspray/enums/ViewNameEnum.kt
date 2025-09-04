package fr.marstech.mtlinkspray.enums

import lombok.Getter

@Getter
enum class ViewNameEnum(val viewName: String, val viewPage: String, val isInMenu: Boolean = true) {
    HOME("index", "Home"),
    SPRAY("spray", "Spray"),
    SHORTENER("shortener", "Shortener"),
    ABUSE("abuse", "Abuse"),
    UUID("uuid", "Uuid"),
    DASHBOARD("dashboard", "Dashboard"),
    ERROR("error", "Error", false);

    companion object {
        @JvmStatic
        fun inMenu(): List<ViewNameEnum> = entries.filter(ViewNameEnum::isInMenu)
    }
}
