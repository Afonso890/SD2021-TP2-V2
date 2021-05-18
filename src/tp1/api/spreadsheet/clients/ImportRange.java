package tp1.api.spreadsheet.clients;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.namespace.QName;

import com.sun.xml.ws.client.BindingProviderProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import tp1.api.consts.Consts;
import tp1.api.discovery.Discovery;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.api.service.soap.SoapSpreadsheets;
import tp1.util.InsecureHostnameVerifier;

public class ImportRange {

	public final static String SHEETS_WSDL = "/spreadsheets/?wsdl";

	
	public static String [][] importRange(String url, String range, String email,Client client) {
		String urls [] = url.split("_");
		String sheetId=urls[1];
		url=urls[0];
		urls=url.split("/");
		//HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
		if(Discovery.SOAP.equals(urls[urls.length-1])) {
			return importRangeSoap(url,range, sheetId, email);
		}
		/*
		ClientConfig config = new ClientConfig();
		//how much time until we timeout when opening the TCP connection to the server
		config.property(ClientProperties.CONNECT_TIMEOUT,Consts.CONNECTION_TIMEOUT);
		//how much time do we wait for the reply of the server after sending the request
		config.property(ClientProperties.READ_TIMEOUT, Consts.CONNECTION_TIMEOUT);
		Client client = ClientBuilder.newClient(config);*/
		//System.out.println("Sending request to server: "+serviceId);
		//System.out.println("GETTING SPREADSHEET, PATH: "+serverUrl);
		WebTarget target = client.target(url).path(RestSpreadsheets.PATH);

		short retries = 0;
		boolean success = false;

		while(!success && retries <Consts.MAX_RETRIES) {
			try {
				//queryParam("password", password)
				Response r = target.path(sheetId).path(range).path(email)
						.request()
						.accept(MediaType.APPLICATION_JSON)
						.get();
				if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() )
					return r.readEntity(String[][].class);
				else
					System.out.println("Error, HTTP error status: " + r.getStatus() );
				success = true;
			} catch (ProcessingException pe) {
				System.out.println("Timeout occurred");
				pe.printStackTrace();
				retries++;
				try { Thread.sleep(Consts.RETRY_PERIOD ); } catch (InterruptedException e) {
					//nothing to be done here, if this happens we will just retry sooner.
				}
				System.out.println("Retrying to execute request.");
			}
		}
		return null;
	}
	private static String [][] importRangeSoap(String url, String range, String sheetId,String email){
		//Obtaining s stub for the remote soap service
				SoapSpreadsheets sheets = null;
				HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
				try {
					QName QNAME = new QName(SoapSpreadsheets.NAMESPACE, SoapSpreadsheets.NAME);
					Service service = Service.create( new URL(url + SHEETS_WSDL), QNAME );
					sheets = service.getPort( SoapSpreadsheets.class );
				} catch ( WebServiceException e) {
					System.err.println("Could not contact the server: " + e.getMessage());
					System.exit(1);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				
				//Set timeouts for executing operations
				((BindingProvider) sheets).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT,Consts.CONNECTION_TIMEOUT);
				((BindingProvider) sheets).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT,Consts.REPLY_TIMEOUT);
			
				//System.out.println("Sending request to server. "+serverUrl);

				short retries = 0;
				boolean success = false;

				while(!success && retries <Consts.MAX_RETRIES) {
					try {
						String [][] values =null;
						values = sheets.importRange(sheetId,range,email);
						success = true;
						return values;
					} catch (tp1.api.service.soap.SheetsException e) {
						System.out.println("Cound not get import range: " + e.getMessage());
						success = true;
					} catch (WebServiceException wse) {
						System.out.println("Communication error.");
						wse.printStackTrace();
						retries++;
						try { Thread.sleep(Consts.RETRY_PERIOD ); } catch (InterruptedException e) {
							//nothing to be done here, if this happens we will just retry sooner.
						}
						System.out.println("Retrying to execute request.");
					}
				}
				return null;

			}
	}

