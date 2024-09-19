package gr.hua.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UpdateRequest {

    @NotNull
    private Long Id;
    @NotNull
    private String name;
    @NotNull
    private String email;
    @NotNull
    private String goal;
    @NotNull
    private String articlesOfAssociation;
    @NotNull
    private String hq;
    @NotNull
    private String executives;

}
