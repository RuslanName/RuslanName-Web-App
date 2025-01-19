package mainFiles.services.enums;

import lombok.Getter;

@Getter
public enum ActionType {
    REGISTRATION(1),
    ACCESS_RIGHTS_GIVE(2),
    PRODUCT(3),
    PRODUCT_ADD(4),
    PRODUCT_UPDATE_NAME(5),
    PRODUCT_UPDATE_PRICE(6),
    PRODUCT_UPDATE_ICON(7),
    PRODUCT_ADD_QUANTITY(8),
    PRODUCT_UPDATE_QUANTITY(9),
    USER(10),
    USER_ORDER_STATUS(11),
    USER_UPDATE_LOCK(12),
    PICKUP_POINT(13),
    PICKUP_POINT_ADD(14),
    PICKUP_POINT_DELETE(15),
    PICKUP_POINT_UPDATE_LOCATION(16);

    private final int code;

    ActionType(int code) {
        this.code = code;
    }
}



