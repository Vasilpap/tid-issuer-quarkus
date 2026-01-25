package gr.hua.service;

import gr.hua.model.entity.Company;
import gr.hua.model.entity.KeycloakUser;
import gr.hua.model.enums.RegistrationState;
import gr.hua.model.mapper.CompanyMapper;
import gr.hua.model.request.RegistrationRequest;
import gr.hua.model.request.UpdateRequest;
import gr.hua.model.response.CompanyResponse;
import gr.hua.repository.CompanyRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class RegistrationService {

    @Inject
    CompanyRepository companyRepository;
    @Inject
    KeycloakService keycloakService;

    private final CompanyMapper companyMapper;

    public CompanyResponse getRegistrationByRep() {
        KeycloakUser user = keycloakService.getUser();
        Company company = companyRepository.findByRepId(user.getId());

        if (company == null) {
            return null;
        }

        return companyMapper.toCompanyResponse(company);
    }

    @Transactional
    public void updateRegistration(UpdateRequest updateRequest) {
        KeycloakUser user = keycloakService.getUser();
        Company company = companyRepository.findByRepId(user.getId());

        if (company == null) {
            throw new jakarta.ws.rs.NotFoundException("No registration found");
        }

        if (company.getState() == RegistrationState.ACCEPTED) {
            throw new ValidationException("registration already accepted");
        }

        // Update fields (ID from request ignored for safety)
        company.setName(updateRequest.getName());
        company.setEmail(updateRequest.getEmail());
        company.setGoal(updateRequest.getGoal());
        company.setArticlesOfAssociation(updateRequest.getArticlesOfAssociation());
        company.setHq(updateRequest.getHq());
        company.setExecutives(updateRequest.getExecutives());

        companyRepository.persist(company);
    }

    @Transactional
    public void registerCompany(RegistrationRequest request) {
        KeycloakUser user = keycloakService.getUser();
        Company company = new Company(
                user,
                request.getName(),
                request.getEmail(),
                request.getGoal(),
                request.getArticlesOfAssociation(),
                request.getHq(),
                request.getExecutives()
        );
        companyRepository.persist(company);
    }
}
