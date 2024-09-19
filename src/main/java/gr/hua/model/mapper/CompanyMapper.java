package gr.hua.model.mapper;

import gr.hua.model.entity.Company;
import gr.hua.model.response.CompanyResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;


@Mapper(
        componentModel = "cdi",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CompanyMapper {

    //@Mapping(target = "representative", source = "representative.id")
    CompanyResponse toCompanyResponse(Company company);

    List<CompanyResponse> toCompanyResponseList(List<Company> companyList);
}
