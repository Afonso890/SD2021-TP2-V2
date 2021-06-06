package tp1.api.discovery;

import java.io.IOException;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

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
public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());
	public static final String SOAP="soap"; 
	public static final String REST="rest";  

	static {
		// addresses some multicast issues on some TCP/IP stacks
		System.setProperty("java.net.preferIPv4Stack", "true");
		// summarizes the logging format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
	
	
	// The pre-aggreed multicast endpoint assigned to perform discovery. 
	static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	static final int DISCOVERY_PERIOD = 1000;
	static final int DISCOVERY_TIMEOUT = 20000;
	static final int ZERO=0;
	static final int ONE=1;

	// Used separate the two fields that make up a service announcement.
	private static final String DELIMITER = "\t";

	private InetSocketAddress addr;
	private String domainName;
	private String serviceName;
	private String serviceURI;
	private String serviceId;
	
	private Map<String,Wrapper> services;
	private long id;
	/**
	 * @param  serviceName the name of the service to announce
	 * @param  serviceURI an uri string - representing the contact endpoint of the service being announced
	 */
	public Discovery( InetSocketAddress addr,String serviceName,String serviceURI, String domain_Name) {
		services = new HashMap<String, Wrapper>();
		id=System.nanoTime();
		//System.out.println("DISCOVERY  STARTED --------------> "+id);
		this.addr = addr;
		this.serviceName = serviceName;
		this.serviceURI  = serviceURI;
		this.domainName= domain_Name;
		this.serviceId=domain_Name+":"+serviceName;		
	}
	
	public String getServiceUri() {
		return serviceURI;
	}
	public String serviceId() {
		return serviceId;
	}
	private synchronized void removeLastKnownService() {
		long now= System.currentTimeMillis();
		Entry<String,Wrapper> en;
		Iterator<Entry<String,Wrapper>> its = services.entrySet().iterator();
		while(its.hasNext()) {
			en = its.next();
			if(now-en.getValue().getLastTime()>DISCOVERY_TIMEOUT) {
				its.remove();
			}
		}
	}
	
	/**
	 * Starts sending service announcements at regular intervals... 
	 */
	public void start() {
		Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s", addr, domainName,serviceURI));
		//<nome-do-domínio>:<serviço><tab><uri-do-servidor>
		byte[] announceBytes = String.format("%s:%s%s%s",domainName,serviceName,DELIMITER,serviceURI).getBytes();
		DatagramPacket announcePkt = new DatagramPacket(announceBytes, announceBytes.length, addr);
		
		//int [] times = {DISCOVERY_PERIOD*2,DISCOVERY_PERIOD*3,DISCOVERY_PERIOD*4};
		//Random rd = new Random();
		//times[rd.nextInt(times.length)]
		try {
			MulticastSocket ms = new MulticastSocket( addr.getPort());
			ms.joinGroup(addr, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
			// start thread to send periodic announcements
			new Thread(() -> {
				for (;;) {
					try {
						ms.send(announcePkt);
						Thread.sleep(DISCOVERY_PERIOD);
					} catch (Exception e) {
						e.printStackTrace();
						// do nothing
					}
				}
			}).start();
			
			// start thread to collect announcements
			
			new Thread(() -> {
				Wrapper wp;
				DatagramPacket pkt = new DatagramPacket(new byte[1024], 1024);
				for (;;) {
					try {
						pkt.setLength(1024);
						ms.receive(pkt);
						
						String msg = new String( pkt.getData(), 0, pkt.getLength());
						String[] msgElems = msg.split(DELIMITER);
						if( msgElems.length == 2) {	//periodic announcement
							synchronized (services) {
								if(!(serviceId.equals(msgElems[ZERO]))) {
									wp = services.get(msgElems[ZERO]);
									if(wp==null) {
										wp = new Wrapper(msgElems[ONE]);
										services.put(msgElems[ZERO],wp);
									}else {
										wp.addUriString(msgElems[ONE]);
									}
								}
								removeLastKnownService();
							}
						}
					} catch (IOException e) {
						// do nothing
					}
				}
			}).start();
			
			/*periodically removes the uris from terminated servers*/
			new Thread(() -> {
				for (;;) {
					try {
						Thread.sleep(DISCOVERY_PERIOD);
						removeLastKnownService();
					} catch (Exception e) {}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	/**
	 * Returns the known servers for a service.
	 * 
	 * @param  serviceNameArg the name of the service being discovered
	 * @return an array of URI with the service instances discovered. 
	 * 
	 */
	public String [] knownUrisOf(String serviceNameArg) {
		//System.out.println("GOING TO GET THE USER "+serviceNameArg+" id "+id+" lengh "+services.size());
		synchronized (services) {
			Wrapper wp =services.get(serviceNameArg);
			if(wp==null) {
				return null;
			}
			return wp.getUris();
		}
	}
	public long getId() {
		return id;
	}
	public static Discovery getDiscovery(String serviceNameArg,String serviceURI, String domain_Name) {
		return new Discovery(DISCOVERY_ADDR,serviceNameArg,serviceURI,domain_Name);
	}
}
