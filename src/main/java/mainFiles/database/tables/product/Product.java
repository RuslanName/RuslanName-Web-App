package mainFiles.database.tables.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products_data")
public class Product {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER")
    private int id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "quantity", columnDefinition = "INTEGER")
    private int quantity;

    @Column(name = "price", columnDefinition = "INTEGER")
    private int price;

    @Column(name = "icon_path", columnDefinition = "VARCHAR(255)")
    private String iconPath;

    @Column(name = "visibility", columnDefinition = "BOOLEAN")
    private boolean visibility;

    public boolean getVisibility() {
        return visibility;
    }
}






