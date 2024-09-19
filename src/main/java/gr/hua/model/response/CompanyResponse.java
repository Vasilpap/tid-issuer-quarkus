package gr.hua.model.response;

import gr.hua.model.entity.KeycloakUser;
import gr.hua.model.enums.RegistrationState;
import lombok.*;

import java.sql.Timestamp;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private Long id;
    private KeycloakUser representative;
    private String name;
    private String email;
    private Long taxId;
    private RegistrationState state;
    private Timestamp timestamp;
    private String goal;
    private String articlesOfAssociation;
    private String hq;
    private String executives;

}
