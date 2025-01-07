package fr.marstech.mtlinkspray.enums;

import lombok.Getter;

@Getter
public enum ViewNameEnum {
    SPRAY("spray");

    private final String viewName;

    ViewNameEnum(String viewName) {
        this.viewName = viewName;
    }
}
