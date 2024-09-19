package gr.hua.service;

import gr.hua.model.entity.KeycloakUser;
import gr.hua.repository.KeycloakUserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class KeycloakService {

    @Inject
    KeycloakUserRepository keycloakUserRepository;
    @Inject
    JsonWebToken jwt;

    @Transactional
    public KeycloakUser getUser() {
        String keycloakId = jwt.getSubject();
        String username = jwt.getName();

        KeycloakUser user = keycloakUserRepository.findByKeycloakId(keycloakId).firstResult();

        if (user == null) {
            user = createKeycloakUser(keycloakId,username);
        }
        return user;
    }

    private KeycloakUser createKeycloakUser(String keycloakId, String username) {
        KeycloakUser user = new KeycloakUser();
        user.setKeycloakId(keycloakId);
        user.setUsername(username);
        keycloakUserRepository.persist(user);
        return user;
    }

}
