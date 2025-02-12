package mainFiles.controllers;

import mainFiles.configs.BotConfig;

import mainFiles.database.tables.pickupPoint.PickupPoint;
import mainFiles.database.tables.pickupPoint.PickupPointsRepository;
import mainFiles.database.tables.product.Product;
import mainFiles.database.tables.product.ProductsRepository;
import mainFiles.database.tables.user.UsersRepository;
import mainFiles.database.tables.userCart.UserCart;
import mainFiles.database.tables.userCart.UserCartsRepository;
import mainFiles.database.tables.userOrder.UserOrder;
import mainFiles.database.tables.userOrder.UserOrdersRepository;
import mainFiles.database.tables.userOrderRegistration.UserOrderRegistration;
import mainFiles.database.tables.userOrderRegistration.UserOrdersRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/activate_bot")
public class BotCallbackController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private UserCartsRepository userCartsRepository;

    @Autowired
    private UserOrdersRepository userOrdersRepository;

    @Autowired
    private UserOrdersRegistrationRepository userOrdersRegistrationRepository;

    @Autowired
    private PickupPointsRepository pickupPointsRepository;

    private final String BOT_API_URL;

    public BotCallbackController(BotConfig config) {
        this.BOT_API_URL = "https://api.telegram.org/bot" + config.getToken();
    }

    @PostMapping
    public void activateBot(@RequestBody Map<String, Object> requestBody) {
        long chatId = Long.parseLong(requestBody.get("chatId").toString());
        int action = Integer.parseInt(requestBody.get("action").toString());

        if (action == 1) {
            userOrdersRegistrationRepository.deleteByChatId(chatId);

            var userCarts = userCartsRepository.findByChatIdAndSelected(chatId, true);

            for (UserCart userCart : userCarts) {
                UserOrderRegistration userOrderRegistration = new UserOrderRegistration();
                Product product = productsRepository.findById(userCart.getProductId()).get();

                int currentProductQuantity = product.getQuantity() - userCart.getQuantity();

                userOrderRegistration.setChatId(chatId);
                userOrderRegistration.setProductId(userCart.getProductId());

                if (currentProductQuantity >= 0) {
                    userOrderRegistration.setQuantity(userCart.getQuantity());
                }

                else {
                    currentProductQuantity = 0;

                    userOrderRegistration.setQuantity(product.getQuantity());
                }

                userOrdersRegistrationRepository.save(userOrderRegistration);

                product.setQuantity(currentProductQuantity);

                if (currentProductQuantity == 0) {
                    product.setVisibility(false);
                }

                productsRepository.save(product);
            }
        }

        else if (action == 2) {
            var userOrdersRegistration = userOrdersRegistrationRepository.findByChatId(chatId);

            for (UserOrderRegistration userOrderRegistration : userOrdersRegistration) {
                int productId = userOrderRegistration.getProductId();

                UserOrder userOrder = new UserOrder();

                userOrder.setChatId(chatId);
                userOrder.setProductId(productId);
                userOrder.setQuantity(userOrderRegistration.getQuantity());
                userOrder.setPrice(productsRepository.findById(productId).get().getPrice());

                int defaultPickupPoint = usersRepository.findById(chatId).get().getDefaultPickupPointId();

                userOrder.setPickupPointId(defaultPickupPoint);
                userOrder.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                int deliveryTime = pickupPointsRepository.findById(defaultPickupPoint).get().getDeliveryTime();

                Timestamp deliveryDate = Timestamp.valueOf(timestamp.toLocalDateTime().plusHours(deliveryTime));

                userOrder.setDeliveryDate(deliveryDate);
                userOrder.setStatus(1);

                userOrdersRepository.save(userOrder);

                int productQuantity = productsRepository.findById(productId).get().getQuantity();

                var userCarts = userCartsRepository.findByProductIdAndQuantityGreaterThan(productId, productQuantity);

                for (UserCart userCart : userCarts) {
                    if (productQuantity > 0) {
                        userCart.setQuantity(productQuantity);

                        userCartsRepository.save(userCart);
                    }

                    else {
                        userCartsRepository.delete(userCart);
                    }
                }
            }

            userOrdersRegistrationRepository.deleteByChatId(chatId);
            userCartsRepository.deleteSelectedByChatId(chatId);

            sendMessage(chatId, "Транзакция прошла успешно");
        }

        else if (action == 3) {
            var userOrderRegistrations = userOrdersRegistrationRepository.findByChatId(chatId);

            for (UserOrderRegistration userOrderRegistration : userOrderRegistrations) {
                Product product = productsRepository.findById(userOrderRegistration.getProductId()).get();

                if (product.getQuantity() == 0) {
                    product.setVisibility(true);
                }

                product.setQuantity(product.getQuantity() + userOrderRegistration.getQuantity());

                productsRepository.save(product);
            }

            userOrdersRegistrationRepository.deleteByChatId(chatId);
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
    }

    private void sendMessage(Long chatId, String message) {
        String url = BOT_API_URL + "/sendMessage?chat_id=" + chatId + "&text=" + message;

        try {
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}