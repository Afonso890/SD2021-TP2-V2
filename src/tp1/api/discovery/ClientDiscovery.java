package tp1.api.discovery;
import java.io.IOException;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

/**
 * <p>A class to perform service discovery, based on periodic service contact endpoint 
 * announcements over multicast communication.</p>
 * 
 * <p>Servers announce their *name* and contact *uri* at regular intervals. The server actively
 * collects received announcements.</p>
 * 
 * <p>Service announcements have the following format:</p>
 * 
 * <p>&lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;</p>
 */
public class  ClientDiscovery {

	static {
		// addresses some multicast issues on some TCP/IP stacks
		System.setProperty("java.net.preferIPv4Stack", "true");
		// summarizes the logging format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
	
	
	// The pre-aggreed multicast endpoint assigned to perform discovery. 
	static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	static final int DISCOVERY_PERIOD = 1000;
	static final int DISCOVERY_TIMEOUT = 5000;
	static final int ZERO=0;
	static final int ONE=1;

	// Used separate the two fields that make up a service announcement.
	private static final String DELIMITER = "\t";

	/**
	 * Starts sending service announcements at regular intervals... 
	 */
	public static String start(String serviceId) {		
		long now = System.currentTimeMillis();
		long elapsed;

		try {
			@SuppressWarnings("resource")
			MulticastSocket ms = new MulticastSocket( DISCOVERY_ADDR.getPort());
			ms.joinGroup(DISCOVERY_ADDR, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
			// start thread to send periodic announcements
						
			// start thread to collect announcements
			DatagramPacket pkt = new DatagramPacket(new byte[1024], 1024);
			boolean go=true;
			while (go) {
				try {
					pkt.setLength(1024);
					ms.receive(pkt);
					
					String msg = new String( pkt.getData(), 0, pkt.getLength());
					String[] msgElems = msg.split(DELIMITER);
					if( msgElems.length == 2) {	//periodic announcement
						//System.out.printf( "FROM %s (%s) : %s\n", pkt.getAddress().getCanonicalHostName(), pkt.getAddress().getHostAddress(), msg);						
						if((msgElems[ZERO].equals(serviceId))) {
							return msgElems[ONE];
						}
					}
				} catch (IOException e) {
					// do nothing
				}
				elapsed=System.currentTimeMillis();
				if(elapsed-now>DISCOVERY_TIMEOUT) {
					go=false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
