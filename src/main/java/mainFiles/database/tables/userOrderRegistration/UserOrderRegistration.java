package mainFiles.database.tables.userOrderRegistration;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_orders_registration_data")
public class UserOrderRegistration {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER")
    private int id;

    @Column(name = "chat_id", columnDefinition = "BIGINT")
    private Long chatId;

    @Column(name = "product_id", columnDefinition = "INTEGER")
    private int productId;

    @Column(name = "quantity", columnDefinition = "INTEGER")
    private int quantity;
}