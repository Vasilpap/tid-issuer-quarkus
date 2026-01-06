package gr.hua.model.mapper;

import gr.hua.model.entity.Company;
import gr.hua.model.entity.KeycloakUser;
import gr.hua.model.enums.RegistrationState;
import gr.hua.model.response.CompanyResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@DisplayName("CompanyMapper Tests")
class CompanyMapperTest {

    @Inject
    CompanyMapper companyMapper;

    private Company company;
    private KeycloakUser representative;
    private Timestamp testTimestamp;

    @BeforeEach
    void setUp() {
        // Create representative
        representative = new KeycloakUser();
        representative.setId(1L);
        representative.setKeycloakId("keycloak-123");
        representative.setUsername("testuser");

        // Create test timestamp
        testTimestamp = new Timestamp(System.currentTimeMillis());

        // Create complete company entity
        company = new Company(
                representative,
                "Test Company",
                "test@company.com",
                "Test goal",
                "http://articles.com",
                "Test HQ",
                "Test Executives"
        );
        company.setId(1L);
        company.setTaxId("TAX-123");
        company.setState(RegistrationState.ACCEPTED);
        company.setTimestamp(testTimestamp);
    }

    @Test
    @DisplayName("toCompanyResponse should map all fields correctly")
    void toCompanyResponse_shouldMapAllFields() {
        // Act
        CompanyResponse response = companyMapper.toCompanyResponse(company);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Company", response.getName());
        assertEquals("test@company.com", response.getEmail());
        assertEquals("TAX-123", response.getTaxId());
        assertEquals(RegistrationState.ACCEPTED, response.getState());
        assertEquals(testTimestamp, response.getTimestamp());
        assertEquals("Test goal", response.getGoal());
        assertEquals("http://articles.com", response.getArticlesOfAssociation());
        assertEquals("Test HQ", response.getHq());
        assertEquals("Test Executives", response.getExecutives());
    }

    @Test
    @DisplayName("toCompanyResponse should map representative correctly")
    void toCompanyResponse_shouldMapRepresentative() {
        // Act
        CompanyResponse response = companyMapper.toCompanyResponse(company);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getRepresentative());
        assertEquals(1L, response.getRepresentative().getId());
        assertEquals("keycloak-123", response.getRepresentative().getKeycloakId());
        assertEquals("testuser", response.getRepresentative().getUsername());
    }

    @Test
    @DisplayName("toCompanyResponse with null taxId should map null taxId")
    void toCompanyResponse_withNullTaxId_shouldMapNullTaxId() {
        // Arrange
        Company pendingCompany = new Company(
                representative,
                "Pending Company",
                "pending@company.com",
                "Pending goal",
                "http://pending.com",
                "Pending HQ",
                "Pending Executives"
        );
        pendingCompany.setId(2L);
        // taxId is null by default for PENDING state

        // Act
        CompanyResponse response = companyMapper.toCompanyResponse(pendingCompany);

        // Assert
        assertNotNull(response);
        assertNull(response.getTaxId());
        assertEquals(RegistrationState.PENDING, response.getState());
    }

    @Test
    @DisplayName("toCompanyResponse with null company should return null")
    void toCompanyResponse_withNullCompany_shouldReturnNull() {
        // Act
        CompanyResponse response = companyMapper.toCompanyResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    @DisplayName("toCompanyResponseList should map all companies")
    void toCompanyResponseList_shouldMapAllCompanies() {
        // Arrange
        Company company2 = new Company(
                representative,
                "Company 2",
                "company2@test.com",
                "Goal 2",
                "http://articles2.com",
                "HQ 2",
                "Executives 2"
        );
        company2.setId(2L);
        company2.setState(RegistrationState.PENDING);

        Company company3 = new Company(
                representative,
                "Company 3",
                "company3@test.com",
                "Goal 3",
                "http://articles3.com",
                "HQ 3",
                "Executives 3"
        );
        company3.setId(3L);
        company3.setState(RegistrationState.DENIED);

        List<Company> companies = Arrays.asList(company, company2, company3);

        // Act
        List<CompanyResponse> responses = companyMapper.toCompanyResponseList(companies);

        // Assert
        assertNotNull(responses);
        assertEquals(3, responses.size());

        assertEquals("Test Company", responses.get(0).getName());
        assertEquals("Company 2", responses.get(1).getName());
        assertEquals("Company 3", responses.get(2).getName());
    }

    @Test
    @DisplayName("toCompanyResponseList with empty list should return empty list")
    void toCompanyResponseList_withEmptyList_shouldReturnEmptyList() {
        // Act
        List<CompanyResponse> responses = companyMapper.toCompanyResponseList(Collections.emptyList());

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("toCompanyResponseList should preserve order")
    void toCompanyResponseList_shouldPreserveOrder() {
        // Arrange
        Company first = new Company(representative, "First", "first@test.com", "Goal 1", "http://1.com", "HQ 1", "Exec 1");
        first.setId(1L);

        Company second = new Company(representative, "Second", "second@test.com", "Goal 2", "http://2.com", "HQ 2", "Exec 2");
        second.setId(2L);

        Company third = new Company(representative, "Third", "third@test.com", "Goal 3", "http://3.com", "HQ 3", "Exec 3");
        third.setId(3L);

        List<Company> companies = Arrays.asList(first, second, third);

        // Act
        List<CompanyResponse> responses = companyMapper.toCompanyResponseList(companies);

        // Assert
        assertEquals(3, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(2L, responses.get(1).getId());
        assertEquals(3L, responses.get(2).getId());
    }

    @Test
    @DisplayName("toCompanyResponse should map different states correctly")
    void toCompanyResponse_shouldMapDifferentStates() {
        // Test PENDING state
        Company pending = new Company(representative, "Pending", "pending@test.com", "Goal", "http://url.com", "HQ", "Exec");
        pending.setId(1L);
        CompanyResponse pendingResponse = companyMapper.toCompanyResponse(pending);
        assertEquals(RegistrationState.PENDING, pendingResponse.getState());

        // Test ACCEPTED state
        Company accepted = new Company(representative, "Accepted", "accepted@test.com", "Goal", "http://url.com", "HQ", "Exec");
        accepted.setId(2L);
        accepted.setState(RegistrationState.ACCEPTED);
        accepted.setTaxId("TAX-456");
        CompanyResponse acceptedResponse = companyMapper.toCompanyResponse(accepted);
        assertEquals(RegistrationState.ACCEPTED, acceptedResponse.getState());
        assertEquals("TAX-456", acceptedResponse.getTaxId());

        // Test DENIED state
        Company denied = new Company(representative, "Denied", "denied@test.com", "Goal", "http://url.com", "HQ", "Exec");
        denied.setId(3L);
        denied.setState(RegistrationState.DENIED);
        CompanyResponse deniedResponse = companyMapper.toCompanyResponse(denied);
        assertEquals(RegistrationState.DENIED, deniedResponse.getState());
    }
}
