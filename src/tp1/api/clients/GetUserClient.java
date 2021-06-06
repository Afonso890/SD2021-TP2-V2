package tp1.api.clients;

import java.net.URL;


import javax.net.ssl.HttpsURLConnection;
import org.glassfish.jersey.client.ClientConfig;

import org.glassfish.jersey.client.ClientProperties;

import javax.xml.namespace.QName;
import com.sun.xml.ws.client.BindingProviderProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;

import tp1.api.User;
import tp1.api.consts.Consts;
import tp1.api.discovery.Discovery;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.soap.SoapUsers;
import tp1.util.InsecureHostnameVerifier;

public class GetUserClient {
	
	public final static String USERS_WSDL = "/users/?wsdl";

	
	public static Client getClient() {

		ClientConfig config = new ClientConfig();
		//how much time until we timeout when opening the TCP connection to the server
		config.property(ClientProperties.CONNECT_TIMEOUT, Consts.CONNECTION_TIMEOUT);
		//how much time do we wait for the reply of the server after sending the request
		config.property(ClientProperties.READ_TIMEOUT, Consts.REPLY_TIMEOUT);
		Client client = ClientBuilder.newClient(config);
		return client;
	}
	public static User getUser(String userId, String password, String serviceId, Client client, Discovery martian) {
		
		User u=null;
		String [] uris = martian.knownUrisOf(serviceId);
		if(uris==null) {
			return null;
		}
		
		String serverUrl =uris[0];

		uris = serverUrl.split("/");
		if(Discovery.SOAP.equals(uris[uris.length-1])) {
			return getSoapUser(serverUrl,userId,password);
		}
		
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
		WebTarget target = client.target( serverUrl ).path( RestUsers.PATH );

		short retries = 0;
		boolean success = false;
		while(!success && retries <Consts.MAX_RETRIES) {
			
			try {
				if(password!=null) {
					target = target.path(userId).queryParam("password", password);
				}else {
					target = target.path("has/"+userId);
				}
			Response r = target.request()
					.accept(MediaType.APPLICATION_JSON)
					.get();

			if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() ) {
				//System.out.println("Success:");
				u = r.readEntity(User.class);
				//System.out.println( "User : " + u);
			} else
				System.out.println("Error, HTTP error status: " + r.getStatus() );
			
			success = true;
			} catch (ProcessingException pe) {
				System.out.println("Timeout occurred");
				pe.printStackTrace();
				retries++;
				try { Thread.sleep( Consts.RETRY_PERIOD ); } catch (InterruptedException e) {
					//nothing to be done here, if this happens we will just retry sooner.
				}
				System.out.println("Retrying to execute request.");
			}
		}
		return u;
	}
	
	private static User getSoapUser(String serverUrl,String userId,String password) {
		//Obtaining s stub for the remote soap service
		SoapUsers users = null;
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());

		try {
			QName QNAME = new QName(SoapUsers.NAMESPACE, SoapUsers.NAME);
			Service service = Service.create( new URL(serverUrl + USERS_WSDL), QNAME );
			users = service.getPort( SoapUsers.class );
		} catch ( WebServiceException e) {
			System.err.println("Could not contact the server: " + e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		//Set timeouts for executing operations
		((BindingProvider) users).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT,Consts.CONNECTION_TIMEOUT);
		((BindingProvider) users).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, Consts.REPLY_TIMEOUT);
	
		//System.out.println("Sending request to server. "+serverUrl);

		short retries = 0;
		boolean success = false;

		while(!success && retries < Consts.MAX_RETRIES) {
			try {
				User u =null;
				if(password==null) {
					u = users.hasThisUser(userId);
				}else {
					u = users.getUser(userId, password);
				}
				//System.out.println("User information: " + u.toString());
				success = true;
				return u;
			} catch (tp1.api.service.soap.UsersException e) {
				System.out.println("Cound not get user: " + e.getMessage());
				e.printStackTrace();
				success = true;
			} catch (WebServiceException wse) {
				System.out.println("Communication error.");
				wse.printStackTrace();
				retries++;
				try { Thread.sleep( Consts.RETRY_PERIOD ); } 
				catch (InterruptedException e) {
					//nothing to be done here, if this happens we will just retry sooner.
				}
				System.out.println("Retrying to execute request.");
			}
		}
		return null;
	}
}