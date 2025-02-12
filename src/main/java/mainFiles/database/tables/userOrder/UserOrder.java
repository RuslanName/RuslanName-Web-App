package mainFiles.database.tables.userOrder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import mainFiles.database.utils.customIdGenerator.AbstractEntity;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "user_orders_data")
public class UserOrder extends AbstractEntity<UserOrder> {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "chat_id", columnDefinition = "BIGINT")
    private Long chatId;

    @Column(name = "pickup_point_id", columnDefinition = "INTEGER")
    private Integer pickupPointId;

    @Column(name = "product_id", columnDefinition = "INTEGER")
    private Integer productId;

    @Column(name = "quantity", columnDefinition = "INTEGER")
    private Integer quantity;

    @Column(name = "price", columnDefinition = "INTEGER")
    private Integer price;

    @Column(name = "deliveryDate", columnDefinition = "TIMESTAMP")
    private Timestamp deliveryDate;

    @Column(name = "status", columnDefinition = "INTEGER")
    private Integer status;

    @Column(name = "registered_at", columnDefinition = "TIMESTAMP")
    private Timestamp registeredAt;
}