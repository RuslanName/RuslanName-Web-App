package mainFiles.model.product;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity(name = "ProductsData")
public class Product {
    @Id
    private int id;
    private String name;
    private int quantity;
    private int price;

    @Override
    public String toString() {

        return "Product {" +
                "id = \"" + id + "\"" +
                ", name = \"" + name + "\"" +
                ", quantity = \"" + quantity + "\"" +
                ", price = \"" +  price + "\"" +
//                ", image = \"" +  image + "\"" +
                '}';
    }
}

