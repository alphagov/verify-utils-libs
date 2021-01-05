package uk.gov.ida.resources;

import javax.inject.Inject;
import org.apache.http.HttpStatus;
import uk.gov.ida.common.CommonUrls;
import uk.gov.ida.configuration.ServiceStatus;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path(CommonUrls.SERVICE_STATUS)
public class ServiceStatusResource {

    private ServiceStatus serviceStatus;

    @Inject
    public ServiceStatusResource() {

        this.serviceStatus = serviceStatus.getInstance();
    }

    @GET
    public Response isOnline(){
        if (serviceStatus.isServerStatusOK()){
            return Response.ok().build();
        } else {
            return Response.status(HttpStatus.SC_SERVICE_UNAVAILABLE).build();
        }
    }
}
