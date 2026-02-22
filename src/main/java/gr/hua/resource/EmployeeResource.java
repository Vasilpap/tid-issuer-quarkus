package gr.hua.resource;

import gr.hua.model.entity.ArticleDocument;
import gr.hua.model.request.ProcessRequest;
import gr.hua.model.response.CompanyResponse;
import gr.hua.service.IssuingService;
import gr.hua.service.RegistrationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@RolesAllowed("Employee")
@RequiredArgsConstructor
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Path("/api/processing")
public class EmployeeResource {

    @Inject
    IssuingService issuingService;

    @Inject
    RegistrationService registrationService;

    @GET
    public List<CompanyResponse> getPendingRegistration() {
        return issuingService.getAllPending();
    }

    @PUT
    public Response process(ProcessRequest processRequest) {
        issuingService.processPending(processRequest);
        return Response.ok().build();
    }

    @GET
    @Path("/{companyId}/files/{fileId}")
    public Response downloadFile(@PathParam("companyId") Long companyId, @PathParam("fileId") Long fileId) {
        ArticleDocument doc = registrationService.getArticleDocumentForCompany(companyId, fileId);
        InputStream stream = registrationService.downloadFileForReview(companyId, fileId);
        return Response.ok(stream, doc.getContentType())
                .header("Content-Disposition", "attachment; filename=\"" + doc.getOriginalFilename() + "\"")
                .build();
    }
}
