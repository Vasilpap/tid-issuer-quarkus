package gr.hua.resource;

import gr.hua.model.entity.ArticleDocument;
import gr.hua.model.request.UpdateRequest;
import gr.hua.model.response.ArticleDocumentResponse;
import gr.hua.model.response.CompanyResponse;
import gr.hua.service.RegistrationService;
import gr.hua.model.request.RegistrationRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.InputStream;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.*;

@RolesAllowed("Representative")
@RequiredArgsConstructor
@Path("/api/registration")
public class RepresentativeResource {

    @Inject
    RegistrationService registrationService;

    @GET
    @Produces(APPLICATION_JSON)
    @APIResponse(
            name = "Find registration of Rep's Company",
            responseCode = "200",
            content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CompanyResponse.class))
    )
    public CompanyResponse findCompanyRegistration() {
        return registrationService.getRegistrationByRep();
    }

    @PUT
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response updateCompanyRegistration(UpdateRequest request) {
        registrationService.updateRegistration(request);
        return Response.ok().build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response registerCompany(RegistrationRequest request) {
        registrationService.registerCompany(request);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    public Response deleteCompanyRegistration() {
        registrationService.deleteRegistration();
        return Response.noContent().build();
    }

    @POST
    @Path("/files")
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    public List<ArticleDocumentResponse> uploadFiles(@RestForm("files") List<FileUpload> files) {
        return registrationService.uploadFiles(files);
    }

    @DELETE
    @Path("/files/{id}")
    @Produces(APPLICATION_JSON)
    public Response deleteFile(@PathParam("id") Long id) {
        registrationService.deleteFile(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/files/{id}")
    public Response downloadFile(@PathParam("id") Long id) {
        ArticleDocument doc = registrationService.getArticleDocument(id);
        InputStream stream = registrationService.downloadFile(id);
        return Response.ok(stream, doc.getContentType())
                .header("Content-Disposition", "attachment; filename=\"" + doc.getOriginalFilename() + "\"")
                .build();
    }
}
