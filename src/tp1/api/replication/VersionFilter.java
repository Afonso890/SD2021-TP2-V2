package tp1.api.replication;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import tp1.api.service.rest.RestSpreadsheetsReplication;

public class VersionFilter implements ContainerResponseFilter {

	KafkaOperationsHandler repManager;

    public VersionFilter( KafkaOperationsHandler repManager) {
        this.repManager = repManager;
    }
	public VersionFilter() {
		// TODO Auto-generated constructor stub
	}

	 @Override
	    public void filter(ContainerRequestContext request, ContainerResponseContext response) 
	                throws IOException {
	    	response.getHeaders().add(RestSpreadsheetsReplication.HEADER_VERSION, repManager.getVersionNumber());
	    }

}
