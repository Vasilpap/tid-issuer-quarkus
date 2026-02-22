package gr.hua.resource;

import gr.hua.model.entity.Company;
import gr.hua.model.entity.KeycloakUser;
import gr.hua.model.enums.RegistrationState;
import gr.hua.model.request.RegistrationRequest;
import gr.hua.model.request.UpdateRequest;
import gr.hua.repository.CompanyRepository;
import gr.hua.repository.KeycloakUserRepository;
import gr.hua.service.KeycloakService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("RepresentativeResource Integration Tests")
class RepresentativeResourceIT {

    private static final String BASE_PATH = "/api/registration";
    private static final String TEST_KEYCLOAK_ID = "test-rep-keycloak-id";

    @Inject
    CompanyRepository companyRepository;

    @Inject
    KeycloakUserRepository keycloakUserRepository;

    @InjectMock
    KeycloakService keycloakService;

    private KeycloakUser testRepresentative;
    private Company testCompany;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up - delete companies first due to foreign key constraint
        companyRepository.deleteAll();
        keycloakUserRepository.deleteAll();

        // Create test representative matching JWT subject
        testRepresentative = new KeycloakUser();
        testRepresentative.setKeycloakId(TEST_KEYCLOAK_ID);
        testRepresentative.setUsername("testrepresentative");
        keycloakUserRepository.persist(testRepresentative);

        // Create PENDING company for representative (one company per representative due to unique constraint)
        testCompany = new Company(
                testRepresentative,
                "Test Company",
                "test@test.com",
                "Test goal",
                "Test HQ",
                "Test Executives"
        );
        companyRepository.persist(testCompany);

        // Mock KeycloakService to return testRepresentative
        when(keycloakService.getUser()).thenReturn(testRepresentative);
    }

    @Test
    @TestSecurity(user = "rep1", roles = "Representative")
    @DisplayName("GET /api/registration with Representative role should return 200 and company")
    void getRegistration_withRepresentativeRole_shouldReturn200AndCompany() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo("Test Company"))
                .body("state", equalTo("PENDING"));
    }

    @Test
    @TestSecurity(user = "rep2", roles = "Representative")
    @DisplayName("PUT /api/registration with PENDING company should return 200 and update fields")
    @Transactional
    void updateRegistration_withPendingCompany_shouldReturn200AndUpdateFields() {
        // Arrange
        UpdateRequest request = new UpdateRequest();
        request.setId(testCompany.getId());
        request.setName("Updated Company Name");
        request.setEmail("updated@test.com");
        request.setGoal("Updated goal");
        request.setHq("Updated HQ");
        request.setExecutives("Updated Executives");

        // Act
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(BASE_PATH)
                .then()
                .statusCode(200);

        // Assert - Verify database
        Company updatedCompany = companyRepository.findById(testCompany.getId());
        assertNotNull(updatedCompany);
        assertEquals("Updated Company Name", updatedCompany.getName());
        assertEquals("updated@test.com", updatedCompany.getEmail());
        assertEquals(RegistrationState.PENDING, updatedCompany.getState());
    }

    @Test
    @TestSecurity(user = "rep3", roles = "Representative")
    @DisplayName("DELETE /api/registration with Representative role should return 204 and remove company")
    @Transactional
    void deleteRegistration_withRepresentativeRole_shouldReturn204AndRemoveCompany() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(BASE_PATH)
                .then()
                .statusCode(204);

        Company deletedCompany = companyRepository.findByRepId(testRepresentative.getId());
        assertNull(deletedCompany);
    }

    @Test
    @DisplayName("GET /api/registration without authentication should return 401")
    void getRegistration_withoutAuthentication_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "employee", roles = "Employee")
    @DisplayName("GET /api/registration with Employee role should return 403")
    void getRegistration_withEmployeeRole_shouldReturn403() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("PUT /api/registration without authentication should return 401")
    void updateRegistration_withoutAuthentication_shouldReturn401() {
        UpdateRequest request = new UpdateRequest();
        request.setId(testCompany.getId());
        request.setName("Should Fail");
        request.setEmail("fail@test.com");
        request.setGoal("Fail");
        request.setHq("Fail HQ");
        request.setExecutives("Fail Executives");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(BASE_PATH)
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "employee", roles = "Employee")
    @DisplayName("PUT /api/registration with Employee role should return 403")
    void updateRegistration_withEmployeeRole_shouldReturn403() {
        UpdateRequest request = new UpdateRequest();
        request.setId(testCompany.getId());
        request.setName("Should Fail");
        request.setEmail("fail@test.com");
        request.setGoal("Fail");
        request.setHq("Fail HQ");
        request.setExecutives("Fail Executives");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(BASE_PATH)
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("POST /api/registration without authentication should return 401")
    void registerCompany_withoutAuthentication_shouldReturn401() {
        RegistrationRequest request = new RegistrationRequest();
        request.setName("Should Fail");
        request.setEmail("fail@test.com");
        request.setGoal("Fail");
        request.setHq("Fail HQ");
        request.setExecutives("Fail Executives");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("DELETE /api/registration without authentication should return 401")
    void deleteRegistration_withoutAuthentication_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(BASE_PATH)
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "employee", roles = "Employee")
    @DisplayName("POST /api/registration with Employee role should return 403")
    void registerCompany_withEmployeeRole_shouldReturn403() {
        RegistrationRequest request = new RegistrationRequest();
        request.setName("Should Fail");
        request.setEmail("fail@test.com");
        request.setGoal("Fail");
        request.setHq("Fail HQ");
        request.setExecutives("Fail Executives");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "employee", roles = "Employee")
    @DisplayName("DELETE /api/registration with Employee role should return 403")
    void deleteRegistration_withEmployeeRole_shouldReturn403() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(BASE_PATH)
                .then()
                .statusCode(403);
    }
}
