package tp1.api.proxy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import tp1.api.discovery.Discovery;
import tp1.api.server.rest.SpreadSheetsServer;
import tp1.api.storage.DropBoxStorage;

public class ProxyServer {
	
	//private static Logger Log = Logger.getLogger(SpreadSheetsServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}
	
	public static final int PORT = 8080;
	public static final String SERVICE = "sheets";
	
	public static Discovery martian=null;
	
	public static void main(String[] args) {
		System.out.println("PROXY SERVEEEEEEEEEEEEEEEEEEEERRRRRRRRRRRRRRRRR");
		boolean clean = Boolean.valueOf(args[1]);
		SpreadSheetsServer.startServer(args,new DropBoxStorage(args[0],clean),true);
	}

}
