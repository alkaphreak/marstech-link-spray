package fr.marstech.mtlinkspray.enums

import lombok.Getter

@Getter
enum class ViewNameEnum( val viewName: String,  val viewPage: String) {
    HOME("index", "Home"),
    SPRAY("spray", "Spray"),
    SHORTENER("shortener", "Shortener"),
    ABUSE("abuse", "Abuse"),
    DASHBOARD("dashboard", "Dashboard"), ;
}
