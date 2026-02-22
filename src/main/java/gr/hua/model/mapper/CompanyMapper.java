package gr.hua.model.mapper;

import gr.hua.model.entity.ArticleDocument;
import gr.hua.model.entity.Company;
import gr.hua.model.response.ArticleDocumentResponse;
import gr.hua.model.response.CompanyResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;


@Mapper(
        componentModel = "cdi",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CompanyMapper {

    CompanyResponse toCompanyResponse(Company company);

    List<CompanyResponse> toCompanyResponseList(List<Company> companyList);

    ArticleDocumentResponse toArticleDocumentResponse(ArticleDocument document);

    List<ArticleDocumentResponse> toArticleDocumentResponseList(List<ArticleDocument> documents);
}
