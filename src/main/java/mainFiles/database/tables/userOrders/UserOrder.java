package mainFiles.database.tables.userOrders;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@Entity(name = "userOrdersData")
public class UserOrder {

    @Id
    private int id;

    private Long chatId;
    private int productId;
    private int quantity;

    @Override
    public String toString() {

        return "UserOrder {" +
                "id = \"" + id + "\"" +
                ", chatId = \"" + chatId + "\"" +
                ", productId = \"" + productId + "\"" +
                ", quantity = \"" + quantity + "\"" +
                '}';
    }
}