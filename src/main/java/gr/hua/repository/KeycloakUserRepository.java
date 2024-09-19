package gr.hua.repository;

import gr.hua.model.entity.KeycloakUser;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeycloakUserRepository implements PanacheRepository<KeycloakUser> {
    public PanacheQuery<KeycloakUser> findByKeycloakId(String keycloakId) {
        return find("keycloakId",keycloakId);
    }
}
