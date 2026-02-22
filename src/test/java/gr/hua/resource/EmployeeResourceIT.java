package gr.hua.resource;

import gr.hua.model.entity.Company;
import gr.hua.model.entity.KeycloakUser;
import gr.hua.model.enums.RegistrationDecision;
import gr.hua.model.enums.RegistrationState;
import gr.hua.model.request.ProcessRequest;
import gr.hua.repository.CompanyRepository;
import gr.hua.repository.KeycloakUserRepository;
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

@QuarkusTest
@DisplayName("EmployeeResource Integration Tests")
class EmployeeResourceIT {

    private static final String BASE_PATH = "/api/processing";

    @Inject
    CompanyRepository companyRepository;

    @Inject
    KeycloakUserRepository keycloakUserRepository;

    private KeycloakUser testRepresentative;
    private Company testCompany;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up
        companyRepository.deleteAll();
        keycloakUserRepository.deleteAll();

        // Create test data
        testRepresentative = new KeycloakUser();
        testRepresentative.setKeycloakId("test-rep-id");
        testRepresentative.setUsername("testrepresentative");
        keycloakUserRepository.persist(testRepresentative);

        testCompany = new Company(
                testRepresentative,
                "Test Pending Company",
                "pending@test.com",
                "Test goal",
                "Test HQ",
                "Test Executives"
        );
        companyRepository.persist(testCompany);
    }

    @Test
    @TestSecurity(user = "employee", roles = "Employee")
    @DisplayName("GET /api/processing with Employee role should return 200 and list of companies")
    void getPending_withEmployeeRole_shouldReturn200AndListOfCompanies() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", notNullValue())
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @TestSecurity(user = "employee", roles = "Employee")
    @DisplayName("GET /api/processing with Employee role should return CompanyResponse structure")
    void getPending_withEmployeeRole_shouldReturnCompanyResponseStructure() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].id", notNullValue())
                .body("[0].name", equalTo("Test Pending Company"))
                .body("[0].state", equalTo("PENDING"));
    }

    @Test
    @TestSecurity(user = "employee", roles = "Employee")
    @DisplayName("PUT /api/processing with Employee role and valid request should return 200")
    void process_withEmployeeRole_andValidRequest_shouldReturn200Ok() {
        ProcessRequest request = new ProcessRequest();
        request.setCompanyId(testCompany.getId());
        request.setDecision(RegistrationDecision.ACCEPT);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(BASE_PATH)
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "employee", roles = "Employee")
    @DisplayName("PUT /api/processing with ACCEPT decision should update database to ACCEPTED state")
    @Transactional
    void process_withEmployeeRole_andAcceptDecision_shouldUpdateDatabase() {
        // Arrange
        ProcessRequest request = new ProcessRequest();
        request.setCompanyId(testCompany.getId());
        request.setDecision(RegistrationDecision.ACCEPT);

        // Act
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(BASE_PATH)
                .then()
                .statusCode(200);

        // Assert - Verify database state
        Company updatedCompany = companyRepository.findById(testCompany.getId());
        assertNotNull(updatedCompany);
        assertEquals(RegistrationState.ACCEPTED, updatedCompany.getState());
        assertNotNull(updatedCompany.getTaxId());
    }

    @Test
    @TestSecurity(user = "employee", roles = "Employee")
    @DisplayName("PUT /api/processing with DENY decision should update database to DENIED state")
    @Transactional
    void process_withEmployeeRole_andDenyDecision_shouldUpdateDatabase() {
        // Arrange
        ProcessRequest request = new ProcessRequest();
        request.setCompanyId(testCompany.getId());
        request.setDecision(RegistrationDecision.DENY);

        // Act
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(BASE_PATH)
                .then()
                .statusCode(200);

        // Assert - Verify database state
        Company updatedCompany = companyRepository.findById(testCompany.getId());
        assertNotNull(updatedCompany);
        assertEquals(RegistrationState.DENIED, updatedCompany.getState());
        assertNull(updatedCompany.getTaxId());
    }

    @Test
    @TestSecurity(user = "employee", roles = "Employee")
    @DisplayName("PUT /api/processing with invalid company ID should return 500")
    void process_withInvalidCompanyId_shouldReturn500() {
        ProcessRequest request = new ProcessRequest();
        request.setCompanyId(999L);
        request.setDecision(RegistrationDecision.ACCEPT);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(BASE_PATH)
                .then()
                .statusCode(500);
    }

    @Test
    @DisplayName("GET /api/processing without authentication should return 401")
    void getPending_withoutAuthentication_shouldReturn401Unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "representative", roles = "Representative")
    @DisplayName("GET /api/processing with Representative role should return 403")
    void getPending_withRepresentativeRole_shouldReturn403Forbidden() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("PUT /api/processing without authentication should return 401")
    void process_withoutAuthentication_shouldReturn401Unauthorized() {
        ProcessRequest request = new ProcessRequest();
        request.setCompanyId(testCompany.getId());
        request.setDecision(RegistrationDecision.ACCEPT);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(BASE_PATH)
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "representative", roles = "Representative")
    @DisplayName("PUT /api/processing with Representative role should return 403")
    void process_withRepresentativeRole_shouldReturn403Forbidden() {
        ProcessRequest request = new ProcessRequest();
        request.setCompanyId(testCompany.getId());
        request.setDecision(RegistrationDecision.ACCEPT);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(BASE_PATH)
                .then()
                .statusCode(403);
    }
}
