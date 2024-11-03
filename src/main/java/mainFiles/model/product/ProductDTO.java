package mainFiles.model.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductDTO {
    private int id;
    private String name;
    private int quantity;
    private int price;
    private String image;  // Изображение в формате base64
    private boolean visibility;
}
