package mainFiles.database.tables.userOrderHistory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "user_orders_history_data")
public class UserOrderHistory {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER")
    private int id;

    @Column(name = "chat_id", columnDefinition = "BIGINT")
    private Long chatId;

    @Column(name = "product_id", columnDefinition = "INTEGER")
    private int productId;

    @Column(name = "quantity", columnDefinition = "INTEGER")
    private int quantity;

    @Column(name = "registered_at", columnDefinition = "TIMESTAMP")
    private Timestamp registeredAt;

    @Column(name = "received_at", columnDefinition = "TIMESTAMP")
    private Timestamp receivedAt;
}