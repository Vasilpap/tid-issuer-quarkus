package gr.hua.model.entity;

import gr.hua.model.enums.RegistrationState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @OneToOne
    private KeycloakUser representative;

    private String name;

    @Column(unique = true)
    private String email;
    private String taxId;

    private RegistrationState state;
    private Timestamp timestamp;

    private String goal;
    private String hq;
    private String executives;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleDocument> articleDocuments = new ArrayList<>();


    public Company(KeycloakUser representative, String name, String email, String goal, String hq, String executives) {
        this.representative = representative;
        this.name = name;
        this.email = email;
        this.goal = goal;
        this.hq = hq;
        this.executives = executives;

        this.state = RegistrationState.PENDING;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }
}
