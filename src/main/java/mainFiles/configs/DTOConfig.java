package mainFiles.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTOConfig {
    private String url;
    private boolean allowEditing;
    private boolean requireTelegramAuth;
}
