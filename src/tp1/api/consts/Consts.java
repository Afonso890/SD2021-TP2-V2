package tp1.api.consts;

import jakarta.ws.rs.client.Client;
import tp1.api.clients.GetUserClient;

public interface Consts {
	
	public static final int ZERO=0;
	public final static int MAX_RETRIES = 100;
	public final static long RETRY_PERIOD = 1000;
	public final static int CONNECTION_TIMEOUT = 10000;
	public final static int REPLY_TIMEOUT = 600;
	public final static Client client = GetUserClient.getClient();

}
