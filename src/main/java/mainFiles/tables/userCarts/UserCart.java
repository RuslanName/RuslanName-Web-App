package mainFiles.tables.userCarts;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@Entity(name = "userCartsData")
public class UserCart {

    @Id
    private int id;

    private Long chatId;
    private int productId;
    private int quantity;
    private boolean selected;

    @Override
    public String toString() {

        return "UserCart {" +
                "id = \"" + id + "\"" +
                ", chatId = \"" + chatId + "\"" +
                ", productId = \"" + productId + "\"" +
                ", quantity = \"" + quantity + "\"" +
                '}';
    }

    public boolean getSelected() {
        return selected;
    }
}