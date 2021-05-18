package tp1.api.dropbox;

import java.io.IOException;

import java.util.Scanner;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import tp1.api.consts.Consts;
import tp1.api.dropbox.args.CreateFileArgs;

public class CreateFile {

	private static final String UPLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/upload";

	private OAuth20Service service;
	private OAuth2AccessToken accessToken;
	
	private Gson json;
	
	public CreateFile() {
		service = new ServiceBuilder(Consts.apiKey).apiSecret(Consts.apiSecret).build(DropboxApi20.INSTANCE);
		accessToken = new OAuth2AccessToken(Consts.accessTokenStr);
		
		json = new Gson();
	}
	
	public boolean execute( String filePath, String msg) {
		OAuthRequest downloadFile = new OAuthRequest(Verb.POST,UPLOAD_FILE_URL);
		downloadFile.addHeader("Content-Type", Consts.TEXT_CONTENT_TYPE2);
		
		msg=json.toJson(new CreateFileArgs(filePath));
		downloadFile.addHeader("Dropbox-API-Arg",msg);

		downloadFile.setPayload(msg);

		service.signRequest(accessToken, downloadFile);
		
		Response r = null;
		
		try {
			r = service.execute(downloadFile);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		if(r.getCode() == 200) {
			return true;
		} else {
			System.err.println("HTTP Error Code: " + r.getCode() + ": " + r.getMessage());
			try {
				System.err.println(r.getBody());
			} catch (IOException e) {
				System.err.println("No body in the response");
			}
			return false;
		}
		
	}
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		CreateFile cd = new CreateFile();
		
		System.out.println("What do you want to write");
		String msg = sc.nextLine().trim();
		String filePath ="/sd2021_aula9/test/ok.txt";
		sc.close();
		
		boolean success = cd.execute(filePath,msg);
		if(success)
			System.out.println("Filepath '" + filePath + "' created successfuly.");
		else
			System.out.println("Failed to read file '" + filePath + "'");
	}

}
