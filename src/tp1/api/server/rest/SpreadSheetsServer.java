package tp1.api.server.rest;

import java.io.File;
import java.net.InetAddress;


import java.net.URI;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import tp1.api.discovery.Discovery;
import tp1.api.servers.resources.SpreadSheetResource;
import tp1.api.storage.MemoryStorage;
import tp1.api.storage.StorageInterface;
import tp1.util.InsecureHostnameVerifier;

public class SpreadSheetsServer {

	private static Logger Log = Logger.getLogger(SpreadSheetsServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}
	
	public static final int PORT = 8080;
	public static final String SERVICE = "sheets";
	
	public static Discovery martian=null;
	
	public static void main(String[] args) {
		if(args.length==0) {
			System.err.println( "Use: java -cp /home/sd/sd2021.jar sd2021.aula2.server.SpreadSheetsServer serverName");
			return;
		}
		startServer(args,new MemoryStorage(),false);
	}
	public static void startServer(String[] args, StorageInterface storage,boolean dropbox) {
		try {
		String ip = InetAddress.getLocalHost().getHostAddress();
		String domainName = args[0];
		String secrete=null;
		if(dropbox) {
			secrete=args[2];
		}else {
			secrete=args[1];
		}
		if(secrete==null) {
			Log.severe("SECRETE IS NULL");
			return;
		}
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
		System.out.println("ARGS DO PROXY");
		config.register(new SpreadSheetResource(domainName,martian,serverURI, storage,secrete));		
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
