package mainFiles.database.tables.pickupPoint;

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
@Table(name = "pickup_points_data")
public class PickupPoint extends AbstractEntity<PickupPoint> {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "delivery_time", columnDefinition = "INTEGER")
    private Integer deliveryTime;

    @Column(name = "registered_at", columnDefinition = "TIMESTAMP")
    private Timestamp registeredAt;
}