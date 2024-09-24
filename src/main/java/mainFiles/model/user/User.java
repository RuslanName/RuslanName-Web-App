package mainFiles.model.user;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Getter
@Setter
@Entity(name = "usersData")
public class User {

    @Id
    private Long chatId;
    private String userName;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Timestamp registeredAt;

    @Override
    public String toString() {
        return "User {" +
                "chatId = \"" + chatId + "\"" +
                ", userName = \"" + userName + "\"" +
                ", firstName = \"" + firstName + "\"" +
                ", lastName = \"" + lastName + "\"" +
                ", phoneNumber = \"" + phoneNumber + "\"" +
                ", registeredAt = \"" + registeredAt + "\"" +
                "}";
    }
}
