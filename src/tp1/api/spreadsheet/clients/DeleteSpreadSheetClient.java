package tp1.api.spreadsheet.clients;

import java.net.URL;

import javax.xml.namespace.QName;

import com.sun.xml.ws.client.BindingProviderProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import tp1.api.consts.Consts;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.api.service.soap.SoapSpreadsheets;

public class DeleteSpreadSheetClient {
	
	public static void deleteSingUserSpreadSheets(String userId, String serverUrl,String secrete) {
		/*
		ClientConfig config = new ClientConfig();
		//how much time until we timeout when opening the TCP connection to the server
		config.property(ClientProperties.CONNECT_TIMEOUT, Consts.CONNECTION_TIMEOUT);
		//how much time do we wait for the reply of the server after sending the request
		config.property(ClientProperties.READ_TIMEOUT, Consts.CONNECTION_TIMEOUT);
		Client client = ClientBuilder.newClient(config); */

		WebTarget target = Consts.client.target( serverUrl ).path( RestSpreadsheets.PATH );

		short retries = 0;
		boolean success = false;

		while(!success && retries < Consts.MAX_RETRIES) {
			try {
				//queryParam("password", password)
				Response r = target.path("removeSheets/"+userId).queryParam("secrete",secrete)
						.request()
						.accept(MediaType.APPLICATION_JSON)
						.delete();
				if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() )
					System.out.println("Success, spreadsheet removed after removing the owner, id: " + r.readEntity(String.class) );
				else
					System.out.println("Error, HTTP error status: " + r.getStatus());
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
	}
	public static void soapDeleteSingUserSpreadSheets(String userId, String serverUrl,String secrete) {
			
		SoapSpreadsheets sheets = null;
		try {
			QName QNAME = new QName(SoapSpreadsheets.NAMESPACE, SoapSpreadsheets.NAME);
			Service service = Service.create( new URL(serverUrl + ImportRange.SHEETS_WSDL), QNAME );
			sheets = service.getPort( SoapSpreadsheets.class );
		} catch ( WebServiceException e) {
			System.err.println("Could not contact the server: " + e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			System.out.println("soemething else happened!");
			e.printStackTrace();
			return;
		}
		
		//Set timeouts for executing operations
		((BindingProvider) sheets).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT,Consts.CONNECTION_TIMEOUT);
		((BindingProvider) sheets).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT,Consts.REPLY_TIMEOUT);
	
		//System.out.println("Sending request to server. "+serverUrl);

		short retries = 0;
		boolean success = false;

		while(!success && retries <Consts.MAX_RETRIES) {
			try {
				sheets.deleteSpreadsheet2(userId,secrete);
				success = true;
			}catch (tp1.api.service.soap.SheetsException e) {
				System.out.println("Cound not delete the sheets of user "+userId+", -->" + e.getMessage());
				success = true;
			}catch (WebServiceException wse) {
				System.out.println("Communication error.");
				wse.printStackTrace();
				retries++;
				try { Thread.sleep(Consts.RETRY_PERIOD ); } catch (InterruptedException e) {
					//nothing to be done here, if this happens we will just retry sooner.
				}
				System.out.println("Retrying to execute request.");
			}
		}
		return;
	}
}
