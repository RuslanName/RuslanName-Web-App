package mainFiles.database.tables.userOrderRegistration;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import mainFiles.database.utils.customIdGenerator.AbstractEntity;

@Getter
@Setter
@Entity
@Table(name = "user_orders_registration_data")
public class UserOrderRegistration extends AbstractEntity<UserOrderRegistration> {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "chat_id", columnDefinition = "BIGINT")
    private Long chatId;

    @Column(name = "product_id", columnDefinition = "INTEGER")
    private Integer productId;

    @Column(name = "quantity", columnDefinition = "INTEGER")
    private Integer quantity;
}