package mainFiles.database.tables.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@Entity(name = "ProductsData")
public class Product {
    @Id
    private int id;

    private String name;
    private int quantity;
    private int price;
    private String iconPath;

    private boolean visibility;

    @Override
    public String toString() {
        return "Product {" +
                "id = \"" + id + "\"" +
                ", name = \"" + name + "\"" +
                ", quantity = \"" + quantity + "\"" +
                ", price = \"" + price + "\"" +
                ", iconPath = \"" + iconPath + "\"" +
                '}';
    }

    public boolean getVisibility() {
        return visibility;
    }
}






