package tp1.api.replication;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import tp1.api.service.rest.RestSpreadsheetsReplication;

public class VersionFilter implements ContainerResponseFilter {

	private VersionNumber versionNumber;
	public VersionFilter() {
		// TODO Auto-generated constructor stub
	}
	public VersionFilter(VersionNumber v) {
		versionNumber=v;
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		responseContext.getHeaders().add(RestSpreadsheetsReplication.HEADER_VERSION,versionNumber.getVersionNumber());
	}

}
