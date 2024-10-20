package mainFiles.model.userOrders;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

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

        return "UserProducts {" +
                "id = \"" + id + "\"" +
                ", chatId = \"" + chatId + "\"" +
                ", productId = \"" + productId + "\"" +
                ", quantity = \"" + quantity + "\"" +
                '}';
    }
}