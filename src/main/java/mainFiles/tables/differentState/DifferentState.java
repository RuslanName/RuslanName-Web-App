package mainFiles.tables.differentState;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@Entity(name = "differentStatesData")
public class DifferentState {

    @Id
    private Long chatId;

    private int action;

    private String step_1;
    private String step_2;
    private String step_3;
    private String step_4;
    private String step_5;
}

// Actions id
// 1 - registration
// 2 - add product
// 3 - delete product
// 4 - visibility product
// 5 - add quantity product
// 6 - update quantity product
// 7 - give access rights