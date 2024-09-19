package gr.hua.repository;

import gr.hua.model.entity.Company;
import gr.hua.model.entity.KeycloakUser;
import gr.hua.model.enums.RegistrationState;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.security.Timestamp;
import java.util.List;

@ApplicationScoped
public class CompanyRepository implements PanacheRepository<Company> {


    public Company findByRepId(Long repId){
        return find("representative.id", repId).firstResult();
    }

    public List<Company> findbyState(RegistrationState registrationState) {
        return find("state",registrationState).list();
    }
}
