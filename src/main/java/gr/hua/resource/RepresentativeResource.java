package gr.hua.resource;

import gr.hua.model.request.UpdateRequest;
import gr.hua.model.response.CompanyResponse;
import gr.hua.service.RegistationService;
import gr.hua.model.request.RegistrationRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RolesAllowed("Representative")
@RequiredArgsConstructor
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Path("/api/registration")
public class RepresentativeResource {

    @Inject
    RegistationService registationService;

    @GET
    @APIResponse(
            name = "Find registration of Rep's Company ",
            responseCode = "200",
            content  = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CompanyResponse.class))
    )
    public CompanyResponse findCompanyRegistration() {

        return registationService.getRegistrationByRep();
    }

    @PUT
    @APIResponse(
            name = "Find user consents",
            responseCode = "200",
            content  = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CompanyResponse.class))
    )

    public Response updateCompanyRegistration(UpdateRequest request) {
        registationService.updateRegistration(request);
        return Response.accepted().build();
    }

    @POST
    /*
    @APIResponse(
            name = "Find user consents",
            responseCode = "200",
            content  = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = .class))
    )
     */
    public Response registerCompany(RegistrationRequest request) {

        registationService.registerCompany(request);
        return Response.accepted().build();
    }

}
