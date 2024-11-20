package mainFiles.tables.pickupPoint;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;




@Getter
@Setter
@Entity(name = "pickupPointsData")
public class PickupPoint {

    @Id
    private int id;

    private String name;
    private Timestamp registeredAt;

    @Override
    public String toString() {
        return "PickupPoint {" +
                "id = \"" + id + "\"" +
                ", name = \"" + name + "\"" +
                ", registeredAt = \"" + registeredAt + "\"" +
                "}";
    }
}