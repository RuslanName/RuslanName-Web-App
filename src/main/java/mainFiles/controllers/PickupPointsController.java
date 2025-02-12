package mainFiles.controllers;

import mainFiles.database.tables.pickupPoint.PickupPoint;
import mainFiles.database.tables.pickupPoint.PickupPointsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/pickup_points_data")
@CrossOrigin(origins = "${web.url}")
public class PickupPointsController {

    @Autowired
    private PickupPointsRepository pickupPointsRepository;

    @GetMapping
    public List<Map<String, Object>> getAllPickupPoints() {
        List<Map<String, Object>> response = new ArrayList<>();

        for (PickupPoint pickupPoint : pickupPointsRepository.findAll()) {
            Map<String, Object> pickupPointData = new HashMap<>();

            pickupPointData.put("id", pickupPoint.getId());
            pickupPointData.put("name", pickupPoint.getName());

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            Timestamp deliveryTimestamp  = Timestamp.valueOf(timestamp.toLocalDateTime().plusHours(pickupPoint.getDeliveryTime()));

            TimeZone mskTimeZone = TimeZone.getTimeZone("Europe/Moscow");

            SimpleDateFormat sdf = new SimpleDateFormat("d MMMM", new Locale("ru"));
            sdf.setTimeZone(mskTimeZone);

            String deliveryDate = sdf.format(deliveryTimestamp);

            pickupPointData.put("deliveryDate", deliveryDate);

            response.add(pickupPointData);
        }

        return response;
    }
}