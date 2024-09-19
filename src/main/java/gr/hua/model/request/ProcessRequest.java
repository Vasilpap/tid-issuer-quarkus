package gr.hua.model.request;

import gr.hua.model.enums.RegistrationDecision;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProcessRequest {

    @NotNull
    private Long companyId;
    @NotNull
    private RegistrationDecision decision;
}
