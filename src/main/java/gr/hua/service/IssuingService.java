package gr.hua.service;

import gr.hua.model.entity.Company;
import gr.hua.model.enums.RegistrationDecision;
import gr.hua.model.enums.RegistrationState;
import gr.hua.model.mapper.CompanyMapper;
import gr.hua.model.response.CompanyResponse;
import gr.hua.repository.CompanyRepository;
import io.quarkus.vertx.http.runtime.devmode.ResourceNotFoundData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.resource.ResourceException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class IssuingService {

    @Inject
    private CompanyRepository companyRepository;

    private final CompanyMapper companyMapper;

    public List<CompanyResponse> getAllPending(){
        List<Company> companies = companyRepository.findbyState(RegistrationState.PENDING);
        return companyMapper.toCompanyResponseList(companies);
    }

    public void processPending(Long companyId, RegistrationDecision decision){
        Company company = companyRepository.findByIdOptional(companyId)
                .orElseThrow(()->new NoSuchElementException("Company not found")
                );
        if (company.getState() != RegistrationState.PENDING) {
            throw new NoSuchElementException("Company has been processed");
        }
        if(decision == RegistrationDecision.ACCEPT){
            company.setTaxId(generateTaxId());
            company.setState(RegistrationState.ACCEPTED);
        } else if(decision == RegistrationDecision.DENY) {
            company.setState(RegistrationState.DENIED);
        }
    }

    private String generateTaxId() {
        return UUID.randomUUID().toString();
    }
}
