package tp1.api.consts;

import com.google.gson.Gson;

import jakarta.ws.rs.client.Client;
import tp1.api.clients.GetUserClient;

public interface Consts {
	
	public static final int ZERO=0;
	public final static int MAX_RETRIES = 100;
	public final static long RETRY_PERIOD = 1000;
	public final static int CONNECTION_TIMEOUT = 10000;
	public final static int REPLY_TIMEOUT = 600;
	public final static Client client = GetUserClient.getClient(); //HELLO
	
	public static final String apiKey = "9chdle2yya045j0";
	public static final String apiSecret = "xybv9724j3tgl6v";
	public static final String accessTokenStr = "-0qR1RteBDkAAAAAAAAAAQI7pokaWYO3Kubs97VGt7zHdbv4ST6kSbzSKL3ohQRK";
	
	public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
	public static final String TEXT_CONTENT_TYPE="text/plain; charset=utf-8";
	
	public static final String TEXT_CONTENT_TYPE2 = "text/plain; charset=dropbox-cors-hack";
	//application/octet-stream
	public static final String OCTET_STREAM = "application/octet-stream";
	
	public static Gson json=new Gson();

}
