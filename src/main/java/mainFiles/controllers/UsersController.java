package mainFiles.controllers;

import mainFiles.database.tables.pickupPoint.PickupPoint;
import mainFiles.database.tables.pickupPoint.PickupPointsRepository;
import mainFiles.database.tables.product.Product;
import mainFiles.database.tables.product.ProductsRepository;
import mainFiles.database.tables.user.User;
import mainFiles.database.tables.user.UsersRepository;
import mainFiles.database.tables.userCart.UserCart;
import mainFiles.database.tables.userCart.UserCartsRepository;
import mainFiles.database.tables.userOrder.UserOrder;
import mainFiles.database.tables.userOrder.UserOrdersRepository;
import mainFiles.database.tables.userOrderHistory.UserOrderHistory;
import mainFiles.database.tables.userOrderHistory.UserOrdersHistoryRepository;
import mainFiles.database.tables.userOrderRegistration.UserOrderRegistration;
import mainFiles.services.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/users_data")
@CrossOrigin(origins = "${web.url}")
public class UsersController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private UserCartsRepository userCartsRepository;

    @Autowired
    private UserOrdersRepository userOrdersRepository;

    @Autowired
    private UserOrdersHistoryRepository userOrdersHistoryRepository;

    @Autowired
    private PickupPointsRepository pickupPointsRepository;

    @GetMapping("/{chatId}")
    public Map<String, Object> getUser(@PathVariable long chatId) {
        Map<String, Object> response = new HashMap<>();

        User user = usersRepository.findById(chatId).orElse(null);

        if (user != null) {
            response.put("isRegistered", true);
            response.put("isBlocked", user.isBlock());
            response.put("defaultPickupPointId", user.getDefaultPickupPointId());
        }

        else {
            response.put("isRegistered", false);
        }

        return response;
    }

    @PostMapping("/{chatId}/update")
    public void updateUser(@PathVariable long chatId, @RequestBody Map<String, Object> requestBody) {
        User user = usersRepository.findById(chatId).get();

        if (requestBody.containsKey("defaultPickupPointId")) {
            int defaultPickupPointId = Integer.parseInt(requestBody.get("defaultPickupPointId").toString());

            user.setDefaultPickupPointId(defaultPickupPointId);
        }

        usersRepository.save(user);
    }

    @GetMapping("/{chatId}/cart")
    public Map<String, Object> getUserCart(@PathVariable long chatId) {
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> cartItems = new ArrayList<>();

        Map<Integer, Map<String, Object>> products = new HashMap<>();

        var userCarts = userCartsRepository.findByChatId(chatId);

        for (UserCart userCart : userCarts) {
            int productId = userCart.getProductId();
            Product product = productsRepository.findById(productId).get();

            Map<String, Object> cartItem = new HashMap<>();

            cartItem.put("id", userCart.getId());
            cartItem.put("productId", productId);
            cartItem.put("quantity", userCart.getQuantity());
            cartItem.put("selected", userCart.isSelected());

            cartItems.add(cartItem);

            Map<String, Object> productData = new HashMap<>();

            productData.put("name", product.getName());
            productData.put("quantity", product.getQuantity());
            productData.put("price", product.getPrice());
            productData.put("visibility", product.isVisibility());
            productData.put("iconPath", product.getIconPath());

            products.put(productId, productData);
        }

        response.put("cart", cartItems);
        response.put("products", products);

        return response;
    }

    @PostMapping("/{chatId}/cart/add")
    public void addUserCart(@PathVariable long chatId, @RequestBody Map<String, Object> requestBody) {
        int productId = Integer.parseInt(requestBody.get("productId").toString());
        int quantity = Integer.parseInt(requestBody.get("quantity").toString());

        UserCart userCart = new UserCart();

        userCart.setChatId(chatId);
        userCart.setProductId(productId);
        userCart.setQuantity(quantity);
        userCart.setSelected(true);

        userCartsRepository.save(userCart);
    }

    @PostMapping("/{chatId}/cart/update")
    public void updateUserCart(@PathVariable long chatId, @RequestBody Map<String, Object> requestBody) {
        Integer id = Integer.valueOf(requestBody.get("id").toString());

        UserCart userCart = userCartsRepository.findByProductIdAndChatId(id, chatId).get(0);

        if (requestBody.containsKey("quantity")) {
            int quantity = Integer.parseInt(requestBody.get("quantity").toString());

            if (productsRepository.findById(userCart.getProductId()).get().getQuantity() >= quantity && quantity > 0) {
                userCart.setQuantity(quantity);

                userCartsRepository.save(userCart);
            }

            else if (quantity == 0) {
                userCartsRepository.delete(userCart);
            }
        }

        if (requestBody.containsKey("selected")) {
            boolean selected = Boolean.parseBoolean(requestBody.get("selected").toString());

            userCart.setSelected(selected);

            userCartsRepository.save(userCart);
        }
    }

    @GetMapping("/{chatId}/orders")
    public Map<String, Object> getUserOrders(@PathVariable long chatId) {
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> orderItems = new ArrayList<>();
        Map<Integer, Map<String, Object>> products = new HashMap<>();
        Map<Integer, Map<String, Object>> pickupPoints = new HashMap<>();

        var userOrders = userOrdersRepository.findByChatId(chatId);

        for (UserOrder userOrder : userOrders) {
            int productId = userOrder.getProductId();
            int pickupPointId = userOrder.getPickupPointId();
            Product product = productsRepository.findById(productId).orElse(null);
            PickupPoint pickupPoint = pickupPointsRepository.findById(pickupPointId).orElse(null);

            Map<String, Object> orderItem = new HashMap<>();

            orderItem.put("id", userOrder.getId());
            orderItem.put("pickupPointId", pickupPointId);
            orderItem.put("productId", productId);
            orderItem.put("quantity", userOrder.getQuantity());
            orderItem.put("price", userOrder.getPrice() * userOrder.getQuantity());

            Timestamp timestamp = userOrder.getDeliveryDate();

            TimeZone mskTimeZone = TimeZone.getTimeZone("Europe/Moscow");

            SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("ru"));
            sdf.setTimeZone(mskTimeZone);

            String deliveryDate = sdf.format(timestamp);

            orderItem.put("deliveryDate", deliveryDate);

            int status = userOrder.getStatus();

            orderItem.put("statusName", OrderStatus.getNameByCode(status));
            orderItem.put("statusColor", OrderStatus.getColorByCode(status));
            orderItem.put("cancelButton", status <= OrderStatus.ASSEMBLING.getCode());

            orderItems.add(orderItem);

            Map<String, Object> productData = new HashMap<>();

            productData.put("name", product.getName());

            products.put(productId, productData);

            Map<String, Object> pickupPointsData = new HashMap<>();

            pickupPointsData.put("name", pickupPoint.getName());

            pickupPoints.put(pickupPointId, pickupPointsData);
        }

        response.put("orders", orderItems);
        response.put("products", products);
        response.put("pickupPoints", pickupPoints);

        return response;
    }

    @PostMapping("/{chatId}/orders/cancel")
    public void cancelUserOrder(@PathVariable long chatId, @RequestBody Map<String, Object> requestBody) {
        Integer id = Integer.valueOf(requestBody.get("orderId").toString());

        UserOrder userOrder = userOrdersRepository.findById(id).get();

        Product product = productsRepository.findById(userOrder.getProductId()).get();

        if (product.getQuantity() == 0) {
            product.setVisibility(true);
        }

        product.setQuantity(product.getQuantity() + userOrder.getQuantity());

        productsRepository.save(product);

        UserOrderHistory userOrderHistory = new UserOrderHistory();

        userOrderHistory.setChatId(chatId);
        userOrderHistory.setPickupPointId(userOrder.getPickupPointId());
        userOrderHistory.setProductId(userOrder.getProductId());
        userOrderHistory.setQuantity(userOrder.getQuantity());
        userOrderHistory.setPrice(userOrder.getQuantity() * userOrder.getPrice());
        userOrderHistory.setStatus(OrderStatus.CANCELLED.getCode());
        userOrderHistory.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

        userOrdersHistoryRepository.save(userOrderHistory);

        userOrdersRepository.deleteById(id);
}

    @GetMapping("/{chatId}/orders_history")
    public Map<String, Object> getUserOrdersHistory(@PathVariable long chatId) {
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> orderItems = new ArrayList<>();
        Map<Integer, Map<String, Object>> products = new HashMap<>();
        Map<Integer, Map<String, Object>> pickupPoints = new HashMap<>();

        var userOrdersHistory = userOrdersHistoryRepository.findByChatId(chatId);

        for (UserOrderHistory userOrderHistory : userOrdersHistory) {
            int productId = userOrderHistory.getProductId();
            int pickupPointId = userOrderHistory.getPickupPointId();
            Product product = productsRepository.findById(productId).orElse(null);
            PickupPoint pickupPoint = pickupPointsRepository.findById(pickupPointId).orElse(null);

            Map<String, Object> orderHistoryItem = new HashMap<>();

            orderHistoryItem.put("id", userOrderHistory.getId());
            orderHistoryItem.put("pickupPointId", pickupPointId);
            orderHistoryItem.put("productId", productId);
            orderHistoryItem.put("quantity", userOrderHistory.getQuantity());
            orderHistoryItem.put("price", userOrderHistory.getPrice() * userOrderHistory.getQuantity());

            Timestamp timestamp = userOrderHistory.getRegisteredAt();

            TimeZone mskTimeZone = TimeZone.getTimeZone("Europe/Moscow");

            SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("ru"));
            sdf.setTimeZone(mskTimeZone);

            String registredAt = sdf.format(timestamp);

            orderHistoryItem.put("date", registredAt);

            int status = userOrderHistory.getStatus();

            orderHistoryItem.put("statusName", OrderStatus.getNameByCode(status));
            orderHistoryItem.put("statusColor", OrderStatus.getColorByCode(status));
            orderHistoryItem.put("returnButton",
                    status == OrderStatus.RECEIVED.getCode()
                            && ChronoUnit.DAYS.between(
                            timestamp.toLocalDateTime(),
                            LocalDateTime.now(ZoneId.of("Europe/Moscow"))
                    ) <= 14
            );

            orderItems.add(orderHistoryItem);

            Map<String, Object> productData = new HashMap<>();

            productData.put("name", product.getName());

            products.put(productId, productData);

            Map<String, Object> pickupPointsData = new HashMap<>();

            pickupPointsData.put("name", pickupPoint.getName());

            pickupPoints.put(pickupPointId, pickupPointsData);
        }

        response.put("orders", orderItems);
        response.put("products", products);
        response.put("pickupPoints", pickupPoints);

        return response;
    }

    @PostMapping("/{chatId}/orders_history/return")
    public Map<String, Object> returnUserOrderHistory(@PathVariable String chatId, @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        Integer id = Integer.valueOf(requestBody.get("orderId").toString());

        UserOrderHistory userOrderHistory = userOrdersHistoryRepository.findById(id).get();

        userOrderHistory.setStatus(OrderStatus.RETURNED.getCode());

        userOrdersHistoryRepository.save(userOrderHistory);

        response.put("status", "success");

        return response;
    }
}

