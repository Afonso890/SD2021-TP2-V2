package tp1.api;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import tp1.api.discovery.Discovery;
import tp1.api.server.rest.SpreadSheetsServer;
import tp1.api.servers.resources.SpreadSheetResource;
import tp1.util.InsecureHostnameVerifier;

public class ProxyServer {
	
	private static final String apiKey = "t5nzwsbik89p5s0";
	private static final String apiSecret = "31kkgco1l2vfzdp";
	private static final String accessTokenStr = "dRhKRkkXUXQAAAAAAAAAASVCYAvD2RIUKPod26ZwW6QYGmK8n4pRhB4GxQiBf5SG";

	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
	private static final String CREATE_FOLDER_V2_URL = "https://api.dropboxapi.com/2/files/create_folder_v2";
	
	private OAuth20Service service;
	private OAuth2AccessToken accessToken;
	
	private Gson json;
	
	private static Logger Log = Logger.getLogger(SpreadSheetsServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}
	
	public static final int PORT = 8080;
	public static final String SERVICE = "sheets";
	
	public static Discovery martian=null;
	
	public static void main(String[] args) {
		try {
			if(args.length!=1) {
				System.err.println( "Use: java -cp /home/sd/sd2021.jar sd2021.aula2.server.SpreadSheetsServer serverName");
				return;
			}
		String ip = InetAddress.getLocalHost().getHostAddress();
		String domainName = args[0];
		//This allows client code executed by this server to ignore hostname verification
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
		/*
		 * Multiple resources (i.e., services) can be
			registered. They should have different
			(top level) @Path annotations.
		 */
		ResourceConfig config = new ResourceConfig();
		//SpreadSheetResource.class
		/*
		 * This defines the server URL. If the
			machine IP address is 192.168.1.103 the
			URL will become:
			http://192.168.1.103:8080/rest
		 */
		String serverURI = String.format("https://%s:%s/rest", ip, PORT);
		
		
		martian = Discovery.getDiscovery(SERVICE,serverURI,domainName);
		martian.start();
		config.register(new SpreadSheetResource(domainName,martian,serverURI));		
		/*
		 * This effectively starts the server (with
			their own threads to handle client
			requests).
		 */
		JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config, SSLContext.getDefault());
		Log.info(String.format("%s Server ready @ %s; FROM : %s \n",  SERVICE, serverURI,domainName));
		
		//More code can be executed here...
		} catch( Exception e) {
			Log.severe(e.getMessage());
		}
	}

}
