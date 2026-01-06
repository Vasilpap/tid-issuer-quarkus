package gr.hua.service;

import gr.hua.model.entity.Company;
import gr.hua.model.entity.KeycloakUser;
import gr.hua.model.enums.RegistrationDecision;
import gr.hua.model.enums.RegistrationState;
import gr.hua.model.mapper.CompanyMapper;
import gr.hua.model.request.ProcessRequest;
import gr.hua.model.response.CompanyResponse;
import gr.hua.repository.CompanyRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAcceptableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("IssuingService Tests")
class IssuingServiceTest {

    @Inject
    IssuingService issuingService;

    @InjectMock
    CompanyRepository companyRepository;

    @InjectMock
    CompanyMapper companyMapper;

    private Company pendingCompany;
    private Company acceptedCompany;
    private Company deniedCompany;
    private KeycloakUser representative;

    @BeforeEach
    void setUp() {
        // Create representative user
        representative = new KeycloakUser();
        representative.setId(1L);
        representative.setKeycloakId("keycloak-123");
        representative.setUsername("testuser");

        // Create PENDING company
        pendingCompany = new Company(
                representative,
                "Test Company",
                "test@company.com",
                "Test goal",
                "http://articles.com",
                "Test HQ",
                "Test Executives"
        );
        pendingCompany.setId(1L);

        // Create ACCEPTED company
        acceptedCompany = new Company(
                representative,
                "Accepted Company",
                "accepted@company.com",
                "Accepted goal",
                "http://accepted.com",
                "Accepted HQ",
                "Accepted Executives"
        );
        acceptedCompany.setId(2L);
        acceptedCompany.setState(RegistrationState.ACCEPTED);
        acceptedCompany.setTaxId("existing-tax-id");

        // Create DENIED company
        deniedCompany = new Company(
                representative,
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
    @DisplayName("getAllPending should return mapped pending companies")
    void getAllPending_shouldReturnMappedPendingCompanies() {
        // Arrange
        List<Company> companies = Arrays.asList(pendingCompany);
        CompanyResponse response = new CompanyResponse();
        response.setId(1L);
        response.setName("Test Company");

        when(companyRepository.findbyState(RegistrationState.PENDING)).thenReturn(companies);
        when(companyMapper.toCompanyResponseList(companies)).thenReturn(Arrays.asList(response));

        // Act
        List<CompanyResponse> result = issuingService.getAllPending();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Company", result.get(0).getName());
        verify(companyRepository).findbyState(RegistrationState.PENDING);
        verify(companyMapper).toCompanyResponseList(companies);
    }

    @Test
    @DisplayName("getAllPending should return empty list when no pending companies")
    void getAllPending_shouldReturnEmptyListWhenNoPendingCompanies() {
        // Arrange
        when(companyRepository.findbyState(RegistrationState.PENDING)).thenReturn(Collections.emptyList());
        when(companyMapper.toCompanyResponseList(anyList())).thenReturn(Collections.emptyList());

        // Act
        List<CompanyResponse> result = issuingService.getAllPending();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(companyRepository).findbyState(RegistrationState.PENDING);
    }

    @Test
    @DisplayName("processPending with ACCEPT decision should set taxId and ACCEPTED state")
    void processPending_withAcceptDecision_shouldSetTaxIdAndAcceptedState() {
        // Arrange
        ProcessRequest request = new ProcessRequest();
        request.setCompanyId(1L);
        request.setDecision(RegistrationDecision.ACCEPT);

        when(companyRepository.findByIdOptional(1L)).thenReturn(Optional.of(pendingCompany));

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        // Act
        issuingService.processPending(request);

        // Assert
        verify(companyRepository).persist(companyCaptor.capture());
        Company persistedCompany = companyCaptor.getValue();

        assertEquals(RegistrationState.ACCEPTED, persistedCompany.getState());
        assertNotNull(persistedCompany.getTaxId());
        // Verify it's a valid UUID format
        assertDoesNotThrow(() -> UUID.fromString(persistedCompany.getTaxId()));
    }

    @Test
    @DisplayName("processPending with DENY decision should set DENIED state")
    void processPending_withDenyDecision_shouldSetDeniedState() {
        // Arrange
        ProcessRequest request = new ProcessRequest();
        request.setCompanyId(1L);
        request.setDecision(RegistrationDecision.DENY);

        when(companyRepository.findByIdOptional(1L)).thenReturn(Optional.of(pendingCompany));

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        // Act
        issuingService.processPending(request);

        // Assert
        verify(companyRepository).persist(companyCaptor.capture());
        Company persistedCompany = companyCaptor.getValue();

        assertEquals(RegistrationState.DENIED, persistedCompany.getState());
        assertNull(persistedCompany.getTaxId());
    }

    @Test
    @DisplayName("processPending with non-PENDING company should throw NoSuchElementException")
    void processPending_withNonPendingCompany_shouldThrowNoSuchElementException() {
        // Arrange
        ProcessRequest request = new ProcessRequest();
        request.setCompanyId(2L);
        request.setDecision(RegistrationDecision.ACCEPT);

        when(companyRepository.findByIdOptional(2L)).thenReturn(Optional.of(acceptedCompany));

        // Act & Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            issuingService.processPending(request);
        });

        assertEquals("Company has been processed", exception.getMessage());
        verify(companyRepository, never()).persist(any(Company.class));
    }

    @Test
    @DisplayName("processPending with non-existent company should throw NoSuchElementException")
    void processPending_withNonExistentCompany_shouldThrowNoSuchElementException() {
        // Arrange
        ProcessRequest request = new ProcessRequest();
        request.setCompanyId(999L);
        request.setDecision(RegistrationDecision.ACCEPT);

        when(companyRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            issuingService.processPending(request);
        });

        assertEquals("Company not found", exception.getMessage());
        verify(companyRepository, never()).persist(any(Company.class));
    }

    @Test
    @DisplayName("processPending with null decision should throw NotAcceptableException")
    void processPending_withNullDecision_shouldThrowNotAcceptableException() {
        // Arrange
        ProcessRequest request = new ProcessRequest();
        request.setCompanyId(1L);
        request.setDecision(null);

        when(companyRepository.findByIdOptional(1L)).thenReturn(Optional.of(pendingCompany));

        // Act & Assert
        assertThrows(NotAcceptableException.class, () -> {
            issuingService.processPending(request);
        });

        verify(companyRepository, never()).persist(any(Company.class));
    }

    @Test
    @DisplayName("processPending should generate unique taxIds for multiple ACCEPT operations")
    void processPending_shouldGenerateUniqueTaxIds() {
        // Arrange
        Company company1 = new Company(
                representative,
                "Company 1",
                "company1@test.com",
                "Goal 1",
                "http://art1.com",
                "HQ 1",
                "Exec 1"
        );
        company1.setId(10L);

        Company company2 = new Company(
                representative,
                "Company 2",
                "company2@test.com",
                "Goal 2",
                "http://art2.com",
                "HQ 2",
                "Exec 2"
        );
        company2.setId(11L);

        ProcessRequest request1 = new ProcessRequest();
        request1.setCompanyId(10L);
        request1.setDecision(RegistrationDecision.ACCEPT);

        ProcessRequest request2 = new ProcessRequest();
        request2.setCompanyId(11L);
        request2.setDecision(RegistrationDecision.ACCEPT);

        when(companyRepository.findByIdOptional(10L)).thenReturn(Optional.of(company1));
        when(companyRepository.findByIdOptional(11L)).thenReturn(Optional.of(company2));

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

        // Act
        issuingService.processPending(request1);
        issuingService.processPending(request2);

        // Assert
        verify(companyRepository, times(2)).persist(companyCaptor.capture());
        List<Company> persistedCompanies = companyCaptor.getAllValues();

        String taxId1 = persistedCompanies.get(0).getTaxId();
        String taxId2 = persistedCompanies.get(1).getTaxId();

        assertNotNull(taxId1);
        assertNotNull(taxId2);
        assertNotEquals(taxId1, taxId2, "Tax IDs should be unique");
    }
}
