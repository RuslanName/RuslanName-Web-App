// package mainFiles.model.product;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.List;

// @RestController
// @RequestMapping("/api/products_data")
// @CrossOrigin(origins = "https://magazin-ruslanname.amvera.io")
// public class ProductsController {

//     @Autowired
//     private ProductsRepository productsRepository;

//     @GetMapping
//     public List<Product> getAllProducts() {
//         return (List<Product>) productsRepository.findAll();
//     }
// }

package mainFiles.model.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/products_data")
@CrossOrigin(origins = "https://magazin-ruslanname.amvera.io")
public class ProductsController {

    @Autowired
    private ProductsRepository productsRepository;

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        List<Product> products = (List<Product>) productsRepository.findAll();
        List<ProductDTO> productDTOs = new ArrayList<>();

        for (Product product : products) {
            ProductDTO productDTO = convertToDTO(product);
            productDTOs.add(productDTO);
        }

        return productDTOs;
    }

    private ProductDTO convertToDTO(Product product) {
        // Конвертируем изображение в base64
        String base64Image = product.getImage() != null ? 
                Base64.getEncoder().encodeToString(product.getImage()) : null;
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getQuantity(),
                product.getPrice(),
                base64Image,
                product.getVisibility()
        );
    }
}

