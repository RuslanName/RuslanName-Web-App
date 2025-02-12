package mainFiles.controllers;

import mainFiles.database.tables.product.Product;
import mainFiles.database.tables.product.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products_data")
@CrossOrigin(origins = "${web.url}")
public class ProductsController {

    @Autowired
    private ProductsRepository productsRepository;

    @GetMapping
    public List<Map<String, Object>> getAllProducts() {
        List<Map<String, Object>> response = new ArrayList<>();

        for (Product product : productsRepository.findAll()) {

            Map<String, Object> productData = new HashMap<>();

            productData.put("id", product.getId());
            productData.put("name", product.getName());
            productData.put("price", product.getPrice());
            productData.put("quantity", product.getQuantity());
            productData.put("visibility", product.isVisibility());
            productData.put("iconPath", product.getIconPath());

            response.add(productData);
        }
        
        return response;
    }
}
