package gr.hua.repository;

import gr.hua.model.entity.ArticleDocument;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ArticleDocumentRepository implements PanacheRepository<ArticleDocument> {

    public List<ArticleDocument> findByCompanyId(Long companyId) {
        return find("company.id", companyId).list();
    }
}
