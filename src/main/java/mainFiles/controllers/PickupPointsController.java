package mainFiles.controllers;

import mainFiles.database.tables.pickupPoint.PickupPoint;
import mainFiles.database.tables.pickupPoint.PickupPointsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/pickup_points")
@CrossOrigin
public class PickupPointsController {

    @Value("${web.url}")
    private String URL;

    private final PickupPointsRepository pickupPointsRepository;

    public PickupPointsController(PickupPointsRepository pickupPointsRepository) {
        this.pickupPointsRepository = pickupPointsRepository;
    }

    @GetMapping
    @CrossOrigin(origins = "${web.url}")
    public List<PickupPoint> getAllPickupPoints() {
        return StreamSupport.stream(pickupPointsRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }
}
