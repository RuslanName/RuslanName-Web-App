package mainFiles.services.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum OrderStatus {
    ASSEMBLING(1, "#1e90ff", "В сборке"),
    SHIPPED(2, "#ff8c00", "Отправлен"),
    DELIVERED(3, "#50c878", "Доставлен"),
    RECEIVED(4, "#3cb371", "Получен"),
    CANCELLED(5, "#2b2b2b", "Отменён"),
    RETURNED(6, "#ff0000", "Возврат");

    private final int code;
    private final String color;
    private final String name;

    OrderStatus(int code, String color, String name) {
        this.code = code;
        this.color = color;
        this.name = name;
    }

    public static String getColorByCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .map(OrderStatus::getColor)
                .findFirst()
                .orElse("#000000");
    }

    public static String getNameByCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .map(OrderStatus::getName)
                .findFirst()
                .orElse("");
    }
}
