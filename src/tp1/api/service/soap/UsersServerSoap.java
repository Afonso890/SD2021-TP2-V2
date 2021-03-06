package tp1.api.service.soap;

import java.net.InetAddress;




import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import jakarta.xml.ws.Endpoint;
import tp1.api.discovery.Discovery;
import tp1.util.InsecureHostnameVerifier;

public class UsersServerSoap {

	private static Logger Log = Logger.getLogger(UsersServerSoap.class.getName());
	public static final String SERVERTYPE="SOAP";

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	public static final int PORT = 8080;
	public static final String SERVICE = "users";
	public static final String SOAP_USERS_PATH = "/soap/users";

	public static void main(String[] args) {
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			String serverURI = String.format("https://%s:%s/soap", ip, PORT);
			
			//This allows client code executed by this server to ignore hostname verification
			HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
			HttpsServer server = HttpsServer.create(new InetSocketAddress(ip, PORT), 0);
			/**
			 * We need to create an HttpsConfigurator that will
				contain the (default) SSLContext that will manage
				keystore and truststore.
			 */
			HttpsConfigurator configurator = new HttpsConfigurator(SSLContext.getDefault());
			server.setHttpsConfigurator(configurator);
			server.setExecutor(Executors.newCachedThreadPool());
			String domainName=args[0];
			String secrete = args[1];
			if(secrete==null) {
				Log.severe("SECRETE IS NULL");
				return;
			}
			Discovery d = Discovery.getDiscovery(SERVICE,serverURI,domainName);
			d.start();
			Endpoint soapUsersEndpoint = Endpoint.create(new UsersWS(domainName,d,secrete));
			
			soapUsersEndpoint.publish(server.createContext(SOAP_USERS_PATH));
			
			server.start();

			Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));

			//More code can be executed here...
		} catch( Exception e) {
			Log.severe(e.getMessage());
		}
	}

}
