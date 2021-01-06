package uk.gov.ida.filters;

import uk.gov.ida.configuration.ServiceStatus;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

public class ConnectionCloseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        ServiceStatus serviceStatus = ServiceStatus.getInstance();
        if (!serviceStatus.isServerStatusOK()) {
            responseContext.getHeaders().add("Connection", "close");
        }
    }
}
