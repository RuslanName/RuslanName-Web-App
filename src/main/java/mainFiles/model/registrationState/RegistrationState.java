package mainFiles.model.registrationState;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity(name = "RegistrationStateData")
public class RegistrationState {

    @Id
    private Long chatId;
    private String userName;
    private String phoneNumber;
}