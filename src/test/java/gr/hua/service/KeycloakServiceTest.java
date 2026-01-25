package gr.hua.service;

import gr.hua.model.entity.KeycloakUser;
import gr.hua.repository.KeycloakUserRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("KeycloakService Tests")
class KeycloakServiceTest {

    @Inject
    KeycloakService keycloakService;

    @InjectMock
    KeycloakUserRepository keycloakUserRepository;

    @InjectMock
    JsonWebToken jwt;  // No @IdToken - service uses access token

    private KeycloakUser existingUser;

    @BeforeEach
    void setUp() {
        // Create existing user
        existingUser = new KeycloakUser();
        existingUser.setId(1L);
        existingUser.setKeycloakId("keycloak-123");
        existingUser.setUsername("existinguser");
    }

    @Test
    @DisplayName("getUser with existing user should return existing user")
    void getUser_withExistingUser_shouldReturnExistingUser() {
        // Arrange
        @SuppressWarnings("unchecked")
        PanacheQuery<KeycloakUser> mockQuery = mock(PanacheQuery.class);

        when(jwt.getSubject()).thenReturn("keycloak-123");
        when(jwt.getName()).thenReturn("existinguser");
        when(keycloakUserRepository.findByKeycloakId("keycloak-123")).thenReturn(mockQuery);
        when(mockQuery.firstResult()).thenReturn(existingUser);

        // Act
        KeycloakUser result = keycloakService.getUser();

        // Assert
        assertNotNull(result);
        assertEquals("keycloak-123", result.getKeycloakId());
        assertEquals("existinguser", result.getUsername());
        assertEquals(1L, result.getId());
        verify(keycloakUserRepository).findByKeycloakId("keycloak-123");
        verify(keycloakUserRepository, never()).persist(any(KeycloakUser.class));
    }

    @Test
    @DisplayName("getUser with non-existent user should create and return new user")
    void getUser_withNonExistentUser_shouldCreateAndReturnNewUser() {
        // Arrange
        @SuppressWarnings("unchecked")
        PanacheQuery<KeycloakUser> mockQuery = mock(PanacheQuery.class);

        when(jwt.getSubject()).thenReturn("new-keycloak-id");
        when(jwt.getName()).thenReturn("newuser");
        when(keycloakUserRepository.findByKeycloakId("new-keycloak-id")).thenReturn(mockQuery);
        when(mockQuery.firstResult()).thenReturn(null);

        ArgumentCaptor<KeycloakUser> userCaptor = ArgumentCaptor.forClass(KeycloakUser.class);

        // Act
        KeycloakUser result = keycloakService.getUser();

        // Assert
        assertNotNull(result);
        assertEquals("new-keycloak-id", result.getKeycloakId());
        assertEquals("newuser", result.getUsername());

        verify(keycloakUserRepository).persist(userCaptor.capture());
        KeycloakUser persistedUser = userCaptor.getValue();
        assertEquals("new-keycloak-id", persistedUser.getKeycloakId());
        assertEquals("newuser", persistedUser.getUsername());
    }

    @Test
    @DisplayName("getUser should set keycloakId from JWT subject")
    void getUser_shouldSetKeycloakIdFromJwtSubject() {
        // Arrange
        @SuppressWarnings("unchecked")
        PanacheQuery<KeycloakUser> mockQuery = mock(PanacheQuery.class);

        when(jwt.getSubject()).thenReturn("subject-123");
        when(jwt.getName()).thenReturn("testuser");
        when(keycloakUserRepository.findByKeycloakId("subject-123")).thenReturn(mockQuery);
        when(mockQuery.firstResult()).thenReturn(null);

        // Act
        KeycloakUser result = keycloakService.getUser();

        // Assert
        assertEquals("subject-123", result.getKeycloakId());
        verify(jwt).getSubject();
    }

    @Test
    @DisplayName("getUser should set username from JWT name")
    void getUser_shouldSetUsernameFromJwtName() {
        // Arrange
        @SuppressWarnings("unchecked")
        PanacheQuery<KeycloakUser> mockQuery = mock(PanacheQuery.class);

        when(jwt.getSubject()).thenReturn("keycloak-456");
        when(jwt.getName()).thenReturn("testusername");
        when(keycloakUserRepository.findByKeycloakId("keycloak-456")).thenReturn(mockQuery);
        when(mockQuery.firstResult()).thenReturn(null);

        // Act
        KeycloakUser result = keycloakService.getUser();

        // Assert
        assertEquals("testusername", result.getUsername());
        verify(jwt).getName();
    }

    @Test
    @DisplayName("getUser with multiple calls should not create duplicates")
    void getUser_withMultipleCalls_shouldNotCreateDuplicates() {
        // Arrange
        @SuppressWarnings("unchecked")
        PanacheQuery<KeycloakUser> mockQuery = mock(PanacheQuery.class);

        String keycloakId = "keycloak-789";
        String username = "multiuser";

        // First call - user doesn't exist
        when(jwt.getSubject()).thenReturn(keycloakId);
        when(jwt.getName()).thenReturn(username);
        when(keycloakUserRepository.findByKeycloakId(keycloakId)).thenReturn(mockQuery);
        when(mockQuery.firstResult())
                .thenReturn(null)  // First call: doesn't exist
                .thenReturn(existingUser);  // Second call: exists

        // Act
        KeycloakUser firstCall = keycloakService.getUser();
        KeycloakUser secondCall = keycloakService.getUser();

        // Assert
        assertNotNull(firstCall);
        assertNotNull(secondCall);

        // Verify persist was called only once (during first call)
        verify(keycloakUserRepository, times(1)).persist(any(KeycloakUser.class));

        // Verify findByKeycloakId was called twice (once per call)
        verify(keycloakUserRepository, times(2)).findByKeycloakId(keycloakId);
    }
}
