package tp1.api.workers;

import javax.net.ssl.HttpsURLConnection;

import tp1.api.discovery.Discovery;
import tp1.api.spreadsheet.clients.DeleteSpreadSheetClient;
import tp1.util.InsecureHostnameVerifier;

public class DeleteSpreadSheetWorker extends Thread {
	
	private Discovery martian;
	private String spreadServiceId;
	private String userid;
	private String secrete;
	public DeleteSpreadSheetWorker (String userid,String spreadServiceId,Discovery martian,String secret) {
		this.martian=martian;
		this.userid=userid;
		this.spreadServiceId=spreadServiceId;
		this.secrete=secret;
	}

	@Override
	public void run() {
		String [] uris = martian.knownUrisOf(spreadServiceId);
		if(uris==null) {
			return;
		}
		//String serverUrl =	
		String uri = uris[0];
		

		uris = uri.split("/");
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
		if(Discovery.SOAP.equalsIgnoreCase(uris[uris.length-1])) {
			DeleteSpreadSheetClient.soapDeleteSingUserSpreadSheets(userid,uri,secrete);
		}else {
			DeleteSpreadSheetClient.deleteSingUserSpreadSheets(userid,uri,secrete);
		}
	}

}
