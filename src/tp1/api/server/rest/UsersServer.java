package tp1.api.server.rest;
import java.net.InetAddress;

import java.net.URI;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import tp1.api.discovery.Discovery;
import tp1.api.servers.resources.UsersResource;
import tp1.util.InsecureHostnameVerifier;

public class UsersServer {

	private static Logger Log = Logger.getLogger(UsersServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}
	
	public static final int PORT = 8080;
	public static final String SERVICE = "users";
	
	public static void main(String[] args) {
		try {
			if(args.length==0) {
				System.err.println( "Use: java -cp /home/sd/sd2021.jar sd2021.aula2.server.UsersServer domainName"+ " secreto "+args[1]);
				return;
			}
		String domainName = args[0];
		String secrete = args[1];
		if(secrete==null) {
			Log.severe("SECRETE CANNOT BE NULL!");
		}
		String ip = InetAddress.getLocalHost().getHostAddress();
		/*
		 * Multiple resources (i.e., services) can be
			registered. They should have different
			(top level) @Path annotations.
		 */
		//This allows client code executed by this server to ignore hostname verification
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
		String serverURI = String.format("https://%s:%s/rest", ip, PORT);

		ResourceConfig config = new ResourceConfig();
		Discovery d = Discovery.getDiscovery(SERVICE,serverURI,domainName);
		d.start();
		config.register(new UsersResource(domainName,d,secrete));

		/*
		 * This defines the server URL. If the
			machine IP address is 192.168.1.103 the
			URL will become:
			http://192.168.1.103:8080/rest
		 */
		

		/*
		 * This effectively starts the server (with
			their own threads to handle client
			requests).
		 */
		JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config, SSLContext.getDefault());
		Log.info(String.format("%s Server ready @ %s WITH serverName: %s \n",  SERVICE, serverURI,domainName));
		
		//More code can be executed here...
		} catch( Exception e) {
			Log.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
}
