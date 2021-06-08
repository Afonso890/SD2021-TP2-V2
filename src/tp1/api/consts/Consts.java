package tp1.api.consts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.pac4j.scribe.builder.api.DropboxApi20;


import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import jakarta.ws.rs.client.Client;
import tp1.api.clients.GetUserClient;

public class Consts {
	
	public static final int ZERO=0;
	public final static int MAX_RETRIES = 3;
	public final static long RETRY_PERIOD = 1000;
	public final static int CONNECTION_TIMEOUT = 10000;
	public final static int REPLY_TIMEOUT = 600;
	public final static Client client = GetUserClient.getClient(); //HELLO
	
	/*
	public static final String apiKey = "t5nzwsbik89p5s0";
	public static final String apiSecret = "31kkgco1l2vfzdp";
	public static final String accessTokenStr = "SsfyAnfENp4AAAAAAAAAAYEO8s8x8x4TM2QbVzeHAtJvxS4_1GzNcd8Y3En7Qr32";*/
	
	public static String apiKey;
	public static String apiSecret;
	public static String accessTokenStr;
	
	public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
	public static final String TEXT_CONTENT_TYPE="text/plain; charset=utf-8";
	
	public static final String TEXT_CONTENT_TYPE2 = "text/plain; charset=dropbox-cors-hack";
	//application/octet-stream
	public static final String OCTET_STREAM = "application/octet-stream";
	
	public static Gson json=new Gson();
	public static OAuth20Service service;
	public static OAuth2AccessToken accessToken;
	
	
	public Consts()
	{
		System.out.println("GOT TO THE CONSTRUCTORRRRRRRRRRRRRRRRRRRRRRR");
		ArrayList<String> keys = new ArrayList<String>();
		File file=new File("/home/sd/keys/dropboxKeys.txt");
    	System.out.println("DID THE FILEEEEEEEEEEEEEEEEEEEEE" + file);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	System.out.println("THIS IS THE KEYYYYYYYYYYYYYYY" + line);
		      keys.add(line);
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(keys.size() == 3)
		{
			this.apiKey = keys.get(0);
			this.apiSecret = keys.get(1);
			this.accessTokenStr = keys.get(2);
			
			service = new ServiceBuilder(this.apiKey).apiSecret(this.apiSecret).build(DropboxApi20.INSTANCE);
			accessToken =  new OAuth2AccessToken(this.accessTokenStr);
		}
		
		
	}
	
}
