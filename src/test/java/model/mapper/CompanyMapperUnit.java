package model.mapper;

import gr.hua.model.entity.Company;
import gr.hua.model.mapper.CompanyMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class CompanyMapperUnit {

    private final CompanyMapper companyMapper = Mappers.getMapper(CompanyMapper.class);

    @Test
    @DisplayName("CompanyMapperUnit:toInfoDto")
    public void toCompanyResponse() {
        Company company = new Company();

    }
}
