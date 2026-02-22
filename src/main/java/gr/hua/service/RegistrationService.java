package gr.hua.service;

import gr.hua.model.entity.ArticleDocument;
import gr.hua.model.entity.Company;
import gr.hua.model.entity.KeycloakUser;
import gr.hua.model.enums.RegistrationState;
import gr.hua.model.mapper.CompanyMapper;
import gr.hua.model.request.RegistrationRequest;
import gr.hua.model.request.UpdateRequest;
import gr.hua.model.response.ArticleDocumentResponse;
import gr.hua.model.response.CompanyResponse;
import gr.hua.repository.ArticleDocumentRepository;
import gr.hua.repository.CompanyRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class RegistrationService {

    @Inject
    CompanyRepository companyRepository;
    @Inject
    KeycloakService keycloakService;
    @Inject
    StorageService storageService;
    @Inject
    ArticleDocumentRepository articleDocumentRepository;

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
            throw new NotFoundException("No registration found");
        }

        if (company.getState() == RegistrationState.ACCEPTED) {
            throw new ValidationException("registration already accepted");
        }

        company.setName(updateRequest.getName());
        company.setEmail(updateRequest.getEmail());
        company.setGoal(updateRequest.getGoal());
        company.setHq(updateRequest.getHq());
        company.setExecutives(updateRequest.getExecutives());

        if (company.getState() == RegistrationState.DENIED) {
            company.setState(RegistrationState.PENDING);
        }

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
                request.getHq(),
                request.getExecutives()
        );
        companyRepository.persist(company);
    }

    @Transactional
    public void deleteRegistration() {
        KeycloakUser user = keycloakService.getUser();
        Company company = companyRepository.findByRepId(user.getId());

        if (company == null) {
            throw new NotFoundException("No registration found");
        }

        if (company.getState() == RegistrationState.ACCEPTED) {
            throw new ValidationException("Cannot delete an accepted registration");
        }

        List<ArticleDocument> documents = articleDocumentRepository.findByCompanyId(company.getId());
        for (ArticleDocument document : documents) {
            storageService.deleteFile(document.getObjectKey());
        }

        companyRepository.delete(company);
    }

    @Transactional
    public List<ArticleDocumentResponse> uploadFiles(List<FileUpload> files) {
        KeycloakUser user = keycloakService.getUser();
        Company company = companyRepository.findByRepId(user.getId());

        if (company == null) {
            throw new NotFoundException("No registration found. Create a registration first.");
        }

        if (company.getState() == RegistrationState.ACCEPTED) {
            throw new ValidationException("Cannot modify an accepted registration");
        }

        List<ArticleDocumentResponse> responses = new ArrayList<>();

        for (FileUpload file : files) {
            try (InputStream is = Files.newInputStream(file.uploadedFile())) {
                String objectKey = storageService.uploadFile(
                        is,
                        file.fileName(),
                        file.contentType(),
                        Files.size(file.uploadedFile())
                );

                ArticleDocument doc = new ArticleDocument(
                        company,
                        objectKey,
                        file.fileName(),
                        file.contentType(),
                        Files.size(file.uploadedFile())
                );
                articleDocumentRepository.persist(doc);
                responses.add(companyMapper.toArticleDocumentResponse(doc));
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload file: " + file.fileName(), e);
            }
        }

        return responses;
    }

    @Transactional
    public void deleteFile(Long fileId) {
        KeycloakUser user = keycloakService.getUser();
        Company company = companyRepository.findByRepId(user.getId());

        if (company == null) {
            throw new NotFoundException("No registration found");
        }

        ArticleDocument doc = articleDocumentRepository.findById(fileId);
        if (doc == null) {
            throw new NotFoundException("File not found");
        }

        if (!doc.getCompany().getId().equals(company.getId())) {
            throw new ForbiddenException("You can only delete your own files");
        }

        if (company.getState() == RegistrationState.ACCEPTED) {
            throw new ValidationException("Cannot modify an accepted registration");
        }

        storageService.deleteFile(doc.getObjectKey());
        articleDocumentRepository.delete(doc);
    }

    public InputStream downloadFile(Long fileId) {
        KeycloakUser user = keycloakService.getUser();
        Company company = companyRepository.findByRepId(user.getId());

        if (company == null) {
            throw new NotFoundException("No registration found");
        }

        ArticleDocument doc = articleDocumentRepository.findById(fileId);
        if (doc == null) {
            throw new NotFoundException("File not found");
        }

        if (!doc.getCompany().getId().equals(company.getId())) {
            throw new ForbiddenException("You can only download your own files");
        }

        return storageService.downloadFile(doc.getObjectKey());
    }

    public InputStream downloadFileForReview(Long companyId, Long fileId) {
        ArticleDocument doc = articleDocumentRepository.findById(fileId);
        if (doc == null) {
            throw new NotFoundException("File not found");
        }

        if (!doc.getCompany().getId().equals(companyId)) {
            throw new NotFoundException("File not found for this company");
        }

        return storageService.downloadFile(doc.getObjectKey());
    }

    public ArticleDocument getArticleDocument(Long fileId) {
        ArticleDocument doc = articleDocumentRepository.findById(fileId);
        if (doc == null) {
            throw new NotFoundException("File not found");
        }
        return doc;
    }

    public ArticleDocument getArticleDocumentForCompany(Long companyId, Long fileId) {
        ArticleDocument doc = articleDocumentRepository.findById(fileId);
        if (doc == null || !doc.getCompany().getId().equals(companyId)) {
            throw new NotFoundException("File not found for this company");
        }
        return doc;
    }
}
