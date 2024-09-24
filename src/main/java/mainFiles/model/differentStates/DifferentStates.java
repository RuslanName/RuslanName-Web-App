package mainFiles.model.differentStates;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity(name = "differentStatesData")
public class DifferentStates {
    @Id
    private Long chatId;
    private int first;
    private int second;
    private String message;

    @Override
    public String toString() {

        return "OtherState {" +
                "chatId = \"" + chatId + "\"" +
                ", first = \"" + first + "\"" +
                ", second = \"" + second + "\"" +
                ", message = \"" + message + "\"" +
                '}';
    }
}