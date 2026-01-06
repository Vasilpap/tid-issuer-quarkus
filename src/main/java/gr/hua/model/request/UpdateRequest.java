package gr.hua.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UpdateRequest {

    @NotNull
    private Long id;

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
    @URL
    @Size(max = 500)
    private String articlesOfAssociation;

    @NotNull
    @Size(min = 1, max = 500)
    private String hq;

    @NotNull
    @Size(min = 1, max = 1000)
    private String executives;

}
