package mainFiles.services.enums;

import lombok.Getter;

@Getter
public enum ActionType {
    REGISTRATION(1),
    ACCESS_RIGHTS_GIVE(2),
    PRODUCT_ADD(3),
    PRODUCT_INFO(4),
    PRODUCT_DELETE(5),
    PRODUCT_VISIBILITY(6),
    PRODUCT_UPDATE_NAME(7),
    PRODUCT_UPDATE_PRICE(8),
    PRODUCT_UPDATE_ICON(9),
    PRODUCT_ADD_QUANTITY(10),
    PRODUCT_UPDATE_QUANTITY(11),
    USER_INFO(12),
    USER_ORDER_STATUS(13),
    USER_UPDATE_LOCK(14),
    PICKUP_POINT_ADD(15),
    PICKUP_POINT_DELETE(16),
    PICKUP_POINT_UPDATE(17);

    private final int code;

    ActionType(int code) {
        this.code = code;
    }
}



