package gr.hua.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RegistrationRequest {

    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    @NotNull
    @Email
    @Size(max = 255)
    private String email;

    @NotNull
    @Size(min = 1, max = 1000)
    private String goal;

    @NotNull
    @Size(min = 1, max = 500)
    private String hq;

    @NotNull
    @Size(min = 1, max = 1000)
    private String executives;

}
