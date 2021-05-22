package tp1.api.consts;

import org.pac4j.scribe.builder.api.DropboxApi20;


import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
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
	
	public static final String apiKey = "t5nzwsbik89p5s0";
	public static final String apiSecret = "31kkgco1l2vfzdp";
	public static final String accessTokenStr = "SsfyAnfENp4AAAAAAAAAAYEO8s8x8x4TM2QbVzeHAtJvxS4_1GzNcd8Y3En7Qr32";
	
	public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
	public static final String TEXT_CONTENT_TYPE="text/plain; charset=utf-8";
	
	public static final String TEXT_CONTENT_TYPE2 = "text/plain; charset=dropbox-cors-hack";
	//application/octet-stream
	public static final String OCTET_STREAM = "application/octet-stream";
	
	public static Gson json=new Gson();
	public static OAuth20Service service = new ServiceBuilder(Consts.apiKey).apiSecret(Consts.apiSecret).build(DropboxApi20.INSTANCE);
	public static OAuth2AccessToken accessToken =  new OAuth2AccessToken(Consts.accessTokenStr);
	
}
