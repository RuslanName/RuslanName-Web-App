package mainFiles.model.differentState;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity(name = "differentStatesData")
public class DifferentState {

    @Id
    private Long chatId;

    private String action;

    private Integer productId;
    private String productName;
    private Integer productQuantity;
    private Integer productPrice;
}