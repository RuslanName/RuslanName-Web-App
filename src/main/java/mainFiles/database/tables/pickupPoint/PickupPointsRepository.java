package mainFiles.database.tables.pickupPoint;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("pickupPointsRepository")
public interface PickupPointsRepository extends CrudRepository<PickupPoint, Integer> {
}