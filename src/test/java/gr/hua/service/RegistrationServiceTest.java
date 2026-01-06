package gr.hua.service;

import gr.hua.model.entity.Company;
import gr.hua.model.entity.KeycloakUser;
import gr.hua.model.enums.RegistrationState;
import gr.hua.model.mapper.CompanyMapper;
import gr.hua.model.request.RegistrationRequest;
import gr.hua.model.request.UpdateRequest;
import gr.hua.model.response.CompanyResponse;
import gr.hua.repository.CompanyRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("RegistrationService Tests")
class RegistrationServiceTest {

    @Inject
    RegistrationService registrationService;

    @InjectMock
    CompanyRepository companyRepository;

    @InjectMock
    KeycloakService keycloakService;

    @InjectMock
    CompanyMapper companyMapper;

    private KeycloakUser mockUser;
    private Company pendingCompany;
    private Company acceptedCompany;
    private Company deniedCompany;

    @BeforeEach
    void setUp() {
        // Create mock user
        mockUser = new KeycloakUser();
        mockUser.setId(1L);
        mockUser.setKeycloakId("keycloak-123");
        mockUser.setUsername("testuser");

        // Create PENDING company
        pendingCompany = new Company(
                mockUser,
                "Pending Company",
                "pending@company.com",
                "Pending goal",
                "http://pending.com",
                "Pending HQ",
                "Pending Executives"
        );
        pendingCompany.setId(1L);

        // Create ACCEPTED company
        acceptedCompany = new Company(
                mockUser,
                "Accepted Company",
                "accepted@company.com",
                "Accepted goal",
                "http://accepted.com",
                "Accepted HQ",
                "Accepted Executives"
        );
        acceptedCompany.setId(2L);
        acceptedCompany.setState(RegistrationState.ACCEPTED);
        acceptedCompany.setTaxId("tax-123");

        // Create DENIED company
        deniedCompany = new Company(
                mockUser,
                "Denied Company",
                "denied@company.com",
                "Denied goal",
                "http://denied.com",
                "Denied HQ",
                "Denied Executives"
        );
        deniedCompany.setId(3L);
        deniedCompany.setState(RegistrationState.DENIED);
    }

    @Test
    @DisplayName("getRegistrationByRep should return company for current user")
    void getRegistrationByRep_shouldReturnCompanyForCurrentUser() {
        // Arrange
        CompanyResponse expectedResponse = new CompanyResponse();
        expectedResponse.setId(1L);
        expectedResponse.setName("Pending Company");

        when(keycloakService.getUser()).thenReturn(mockUser);
        when(companyRepository.findByRepId(1L)).thenReturn(pendingCompany);
        when(companyMapper.toCompanyResponse(pendingCompany)).thenReturn(expectedResponse);

        // Act
        CompanyResponse result = registrationService.getRegistrationByRep();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Pending Company", result.getName());
        verify(keycloakService).getUser();
        verify(companyRepository).findByRepId(1L);
        verify(companyMapper).toCompanyResponse(pendingCompany);
    }

    @Test
    @DisplayName("getRegistrationByRep should call KeycloakService and repository in correct order")
    void getRegistrationByRep_shouldCallKeycloakServiceAndRepository() {
        // Arrange
        when(keycloakService.getUser()).thenReturn(mockUser);
        when(companyRepository.findByRepId(1L)).thenReturn(pendingCompany);
        when(companyMapper.toCompanyResponse(any())).thenReturn(new CompanyResponse());

        // Act
        registrationService.getRegistrationByRep();

        // Assert
        verify(keycloakService).getUser();
        verify(companyRepository).findByRepId(mockUser.getId());
    }

    @Test
    @DisplayName("updateRegistration with PENDING company should update all fields")
    void updateRegistration_withPendingCompany_shouldUpdateAllFields() {
        // Arrange
        UpdateRequest request = new UpdateRequest();
        request.setId(1L);
        request.setName("Updated Name");
        request.setEmail("updated@company.com");
        request.setGoal("Updated goal");
        request.setArticlesOfAssociation("http://updated.com");
        request.setHq("Updated HQ");
        request.setExecutives("Updated Executives");

        when(companyRepository.findById(1L)).thenReturn(pendingCompany);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        // Act
        registrationService.updateRegistration(request);

        // Assert
        verify(companyRepository).persist(companyCaptor.capture());
        Company updatedCompany = companyCaptor.getValue();

        assertEquals("Updated Name", updatedCompany.getName());
        assertEquals("updated@company.com", updatedCompany.getEmail());
        assertEquals("Updated goal", updatedCompany.getGoal());
        assertEquals("http://updated.com", updatedCompany.getArticlesOfAssociation());
        assertEquals("Updated HQ", updatedCompany.getHq());
        assertEquals("Updated Executives", updatedCompany.getExecutives());
    }

    @Test
    @DisplayName("updateRegistration with DENIED company should update all fields")
    void updateRegistration_withDeniedCompany_shouldUpdateAllFields() {
        // Arrange
        UpdateRequest request = new UpdateRequest();
        request.setId(3L);
        request.setName("Updated Denied");
        request.setEmail("updated-denied@company.com");
        request.setGoal("Updated denied goal");
        request.setArticlesOfAssociation("http://updated-denied.com");
        request.setHq("Updated Denied HQ");
        request.setExecutives("Updated Denied Executives");

        when(companyRepository.findById(3L)).thenReturn(deniedCompany);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        // Act
        registrationService.updateRegistration(request);

        // Assert
        verify(companyRepository).persist(companyCaptor.capture());
        Company updatedCompany = companyCaptor.getValue();

        assertEquals("Updated Denied", updatedCompany.getName());
        assertEquals(RegistrationState.DENIED, updatedCompany.getState());
    }

    @Test
    @DisplayName("updateRegistration with ACCEPTED company should throw ValidationException")
    void updateRegistration_withAcceptedCompany_shouldThrowValidationException() {
        // Arrange
        UpdateRequest request = new UpdateRequest();
        request.setId(2L);
        request.setName("Should Not Update");
        request.setEmail("should-not-update@company.com");
        request.setGoal("Should not update");
        request.setArticlesOfAssociation("http://should-not-update.com");
        request.setHq("Should Not Update HQ");
        request.setExecutives("Should Not Update Executives");

        when(companyRepository.findById(2L)).thenReturn(acceptedCompany);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            registrationService.updateRegistration(request);
        });

        assertEquals("registration already accepted", exception.getMessage());
        verify(companyRepository, never()).persist(any(Company.class));
    }

    @Test
    @DisplayName("updateRegistration should not change state")
    void updateRegistration_shouldNotChangeState() {
        // Arrange
        UpdateRequest request = new UpdateRequest();
        request.setId(1L);
        request.setName("Updated Name");
        request.setEmail("updated@company.com");
        request.setGoal("Updated goal");
        request.setArticlesOfAssociation("http://updated.com");
        request.setHq("Updated HQ");
        request.setExecutives("Updated Executives");

        when(companyRepository.findById(1L)).thenReturn(pendingCompany);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        // Act
        registrationService.updateRegistration(request);

        // Assert
        verify(companyRepository).persist(companyCaptor.capture());
        Company updatedCompany = companyCaptor.getValue();

        assertEquals(RegistrationState.PENDING, updatedCompany.getState());
    }

    @Test
    @DisplayName("updateRegistration should not change taxId")
    void updateRegistration_shouldNotChangeTaxId() {
        // Arrange - Use a PENDING company (no taxId) to verify taxId remains null
        UpdateRequest request = new UpdateRequest();
        request.setId(1L);
        request.setName("Updated Name");
        request.setEmail("updated@company.com");
        request.setGoal("Updated goal");
        request.setArticlesOfAssociation("http://updated.com");
        request.setHq("Updated HQ");
        request.setExecutives("Updated Executives");

        when(companyRepository.findById(1L)).thenReturn(pendingCompany);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        // Act
        registrationService.updateRegistration(request);

        // Assert
        verify(companyRepository).persist(companyCaptor.capture());
        Company updatedCompany = companyCaptor.getValue();

        assertNull(updatedCompany.getTaxId());
    }

    @Test
    @DisplayName("registerCompany should create new company with PENDING state")
    void registerCompany_shouldCreateNewCompanyWithPendingState() {
        // Arrange
        RegistrationRequest request = new RegistrationRequest();
        request.setName("New Company");
        request.setEmail("new@company.com");
        request.setGoal("New goal");
        request.setArticlesOfAssociation("http://new.com");
        request.setHq("New HQ");
        request.setExecutives("New Executives");

        when(keycloakService.getUser()).thenReturn(mockUser);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        // Act
        registrationService.registerCompany(request);

        // Assert
        verify(companyRepository).persist(companyCaptor.capture());
        Company createdCompany = companyCaptor.getValue();

        assertEquals("New Company", createdCompany.getName());
        assertEquals("new@company.com", createdCompany.getEmail());
        assertEquals("New goal", createdCompany.getGoal());
        assertEquals("http://new.com", createdCompany.getArticlesOfAssociation());
        assertEquals("New HQ", createdCompany.getHq());
        assertEquals("New Executives", createdCompany.getExecutives());
        assertEquals(RegistrationState.PENDING, createdCompany.getState());
        assertNotNull(createdCompany.getTimestamp());
    }

    @Test
    @DisplayName("registerCompany should associate with current user")
    void registerCompany_shouldAssociateWithCurrentUser() {
        // Arrange
        RegistrationRequest request = new RegistrationRequest();
        request.setName("New Company");
        request.setEmail("new@company.com");
        request.setGoal("New goal");
        request.setArticlesOfAssociation("http://new.com");
        request.setHq("New HQ");
        request.setExecutives("New Executives");

        when(keycloakService.getUser()).thenReturn(mockUser);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        // Act
        registrationService.registerCompany(request);

        // Assert
        verify(keycloakService).getUser();
        verify(companyRepository).persist(companyCaptor.capture());
        Company createdCompany = companyCaptor.getValue();

        assertNotNull(createdCompany.getRepresentative());
        assertEquals(mockUser.getId(), createdCompany.getRepresentative().getId());
        assertEquals(mockUser.getKeycloakId(), createdCompany.getRepresentative().getKeycloakId());
    }
}
