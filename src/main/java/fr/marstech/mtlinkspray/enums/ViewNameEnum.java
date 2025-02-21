package fr.marstech.mtlinkspray.enums;

import lombok.Getter;

@Getter
public enum ViewNameEnum {
    HOME("index", "Home"),
    SPRAY("spray", "Spray"),
    SHORTENER("shortener", "Shortener"),
    ABUSE("abuse", "Abuse"),
    ;

    private final String viewName;
    private final String viewPage;

    ViewNameEnum(String viewName, String viewPage) {
        this.viewName = viewName;
        this.viewPage = viewPage;
    }
}
