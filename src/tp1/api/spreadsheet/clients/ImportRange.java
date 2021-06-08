package tp1.api.spreadsheet.clients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
import tp1.api.SpreadsheetValuesWrapper;
import tp1.api.consts.Consts;
import tp1.api.discovery.Discovery;
import tp1.api.nfs_cache.SheetsCache;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.api.service.soap.SoapSpreadsheets;
import tp1.util.GoogleReturnValues;
import tp1.util.InsecureHostnameVerifier;

public class ImportRange {

	public final static String SHEETS_WSDL = "/spreadsheets/?wsdl";
	private final static SheetsCache cache = new SheetsCache();
	private static String apiKey ="AIzaSyBlavaHw1h9Th_RouiUa70iMWAhB18oizk";
	private static String GOOGLE_IMPORT_RANGE_TAG="google";
		
	public static String [][] importRange(String url, String range, String email,Client client,String secreto) {
		
		String sheetId;
		String urls [];
	    
		if(url.contains(GOOGLE_IMPORT_RANGE_TAG))
		{
			urls=url.split("/");
			sheetId=urls[3];
		    return importRangeGoogle(url, sheetId, range);
		}
		urls  = url.split("_");
		sheetId=urls[1];
		String [][] values=cache.getValues(sheetId,range); 
		if(values!=null) {
			return values;
		}
		
		url=urls[0];
		urls=url.split("/");

		if(Discovery.SOAP.equals(urls[urls.length-1])) {
			return importRangeSoap(url,range, sheetId, email,secreto);
		}
		WebTarget target = client.target(url).path(RestSpreadsheets.PATH);

		short retries = 0;
		boolean success = false;

		while(!success && retries <Consts.MAX_RETRIES) {
			try {
				//queryParam("password", password)
				Response r = target.path(sheetId).path(range).path(email).queryParam("secreto",secreto)
						.request()
						.accept(MediaType.APPLICATION_JSON)
						.get();
				if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() ) {
					SpreadsheetValuesWrapper result;
					result = r.readEntity(SpreadsheetValuesWrapper.class);
					cache.addValues(result.getValues(), sheetId,result.getServer_tw());
					return cache.extractValues(result.getValues(),range);	
				}				
				else {
					System.out.println("Error, HTTP error status: " + r.getStatus() );
				}
				success = true;
			} catch (ProcessingException pe) {
				pe.printStackTrace();
				retries++;
				try { Thread.sleep(Consts.RETRY_PERIOD ); } catch (InterruptedException e) {
					//nothing to be done here, if this happens we will just retry sooner.
				}
				System.out.println("Retrying to execute request.");
			}
		}
		return cache.getValuesInFailure(sheetId,range);
	}
	private static String [][] importRangeSoap(String url, String range, String sheetId,String email,String secreto){
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
						SpreadsheetValuesWrapper result;
						result = sheets.importRange(sheetId,range,email,secreto);
						cache.addValues(result.getValues(),sheetId,result.getServer_tw());
						success = true;
						return cache.extractValues(result.getValues(),range);	
					} catch (tp1.api.service.soap.SheetsException e) {
						System.out.println("Cound not get import range: " + e.getMessage());
						success = true;
					} catch (WebServiceException wse) {
						wse.printStackTrace();
						retries++;
						try { Thread.sleep(Consts.RETRY_PERIOD ); } catch (InterruptedException e) {
							//nothing to be done here, if this happens we will just retry sooner.
						}
						System.out.println("Retrying to execute request.");
					}
				}
				return cache.getValuesInFailure(sheetId,range);
			}
	
	public static String [][] importRangeGoogle(String url,String sheetId, String range)
		{	
		String mat[][]=null;
			try {
				String urlGoogle =  url.substring(0, 30);
				urlGoogle = urlGoogle + "v4/spreadsheets/" + sheetId + "/values/" + range + "?key=" + apiKey;
				HttpURLConnection con = (HttpURLConnection) new URL(urlGoogle).openConnection();
				
				con.setRequestMethod("GET");
				con.setRequestProperty("Content-Type", "application/json");
				
				//int status = con.getResponseCode();
				/**if(status!=Status.OK.ordinal()) {
					return mat;
				}*/
		
				BufferedReader in = new BufferedReader(
				  new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
				    content.append(inputLine);
				}
				in.close();
				con.disconnect();
				
				GoogleReturnValues values = Consts.json.fromJson(content.toString(), GoogleReturnValues.class);
				mat = values.getValues();				
			}catch(Exception e) {
			}
		 return mat ;
		}	
}

