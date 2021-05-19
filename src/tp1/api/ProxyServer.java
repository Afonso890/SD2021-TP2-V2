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
import tp1.api.servers.resources.DropboxResource;
import tp1.api.servers.resources.SpreadSheetResource;
import tp1.util.InsecureHostnameVerifier;

public class ProxyServer {
	
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

		ResourceConfig config = new ResourceConfig();

		String serverURI = String.format("https://%s:%s/rest", ip, PORT);
		
		
		martian = Discovery.getDiscovery(SERVICE,serverURI,domainName);
		martian.start();
		config.register(new DropboxResource(domainName,martian,serverURI,domainName));		
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
