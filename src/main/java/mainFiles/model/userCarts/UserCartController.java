@RestController
@RequestMapping("/api/user_carts_data")
@CrossOrigin(origins = "https://magazin-ruslanname.amvera.io")
public class UserCartController {

    @Autowired
    private UserCartService userCartService;

    @PostMapping("/add")
    public String addProductToCart(@RequestParam long chatId, @RequestParam int productId, @RequestParam int quantity) {
        userCartService.addProductToCart(chatId, productId, quantity);
        return "Product added to cart successfully!";
    }
}
