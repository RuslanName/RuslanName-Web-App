package mainFiles.services.enums;

import lombok.Getter;

@Getter
public enum AccessRights {
    OWNER(0),
    ADMINISTRATOR(1),
    PICKUP_POINT(2),
    USER(3);

    private final int code;

    AccessRights(int code) {
        this.code = code;
    }
}