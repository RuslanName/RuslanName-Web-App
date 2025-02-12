package mainFiles.database.tables.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import mainFiles.database.utils.customIdGenerator.AbstractEntity;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "products_data")
public class Product extends AbstractEntity<Product> {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER")
    private Integer id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "quantity", columnDefinition = "INTEGER")
    private Integer quantity;

    @Column(name = "price", columnDefinition = "INTEGER")
    private Integer price;

    @Column(name = "icon_path", columnDefinition = "VARCHAR(255)")
    private String iconPath;

    @Column(name = "visibility", columnDefinition = "BOOLEAN")
    private boolean visibility;

    @Column(name = "registered_at", columnDefinition = "TIMESTAMP")
    private Timestamp registeredAt;
}






