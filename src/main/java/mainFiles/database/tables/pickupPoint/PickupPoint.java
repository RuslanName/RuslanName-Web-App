package mainFiles.database.tables.pickupPoint;

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
@Table(name = "pickup_points_data")
public class PickupPoint {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER")
    private int id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "registered_at", columnDefinition = "TIMESTAMP")
    private Timestamp registeredAt;
}