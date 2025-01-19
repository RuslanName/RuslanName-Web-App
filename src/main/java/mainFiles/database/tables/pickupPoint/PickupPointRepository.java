package mainFiles.database.tables.pickupPoint;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("pickupPointsRepository")
public interface PickupPointRepository extends CrudRepository<PickupPoint, Integer> {
}