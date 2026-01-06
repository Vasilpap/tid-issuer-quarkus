package gr.hua.resource;

import gr.hua.model.enums.RegistrationDecision;
import gr.hua.model.request.ProcessRequest;
import gr.hua.model.request.UpdateRequest;
import gr.hua.model.response.CompanyResponse;
import gr.hua.service.IssuingService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

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

    @GET
    public List<CompanyResponse> getPendingRegistration(){
        return issuingService.getAllPending();
    }

    @PUT
    public Response process(ProcessRequest processRequest){
        issuingService.processPending(processRequest);
        return Response.ok().build();
    }
}
