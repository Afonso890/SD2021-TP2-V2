package tp1.api.dropbox;

import java.io.IOException;

import java.io.InputStream;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import tp1.api.consts.Consts;
import tp1.api.dropbox.args.DownloadFileArgs;

public class DownloadFile {

	private static final String DOWNLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/download";

	private OAuth20Service service;
	private OAuth2AccessToken accessToken;
	
	private Gson json;
	
	public DownloadFile() {
		service = new ServiceBuilder(Consts.apiKey).apiSecret(Consts.apiSecret).build(DropboxApi20.INSTANCE);
		accessToken = new OAuth2AccessToken(Consts.accessTokenStr);
		
		json = new Gson();
	}
	
	public boolean execute( String filePath ) {
		OAuthRequest downloadFile = new OAuthRequest(Verb.POST,DOWNLOAD_FILE_URL);
		downloadFile.addHeader("Content-Type", Consts.TEXT_CONTENT_TYPE);
		
		downloadFile.addHeader("Dropbox-API-Arg",json.toJson(new DownloadFileArgs(filePath)));

		//createFolder.setPayload(json.toJson(new CreateFolderV2Args(directoryName, false)));

		service.signRequest(accessToken, downloadFile);
		
		Response r = null;
		
		try {
			r = service.execute(downloadFile);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		if(r.getCode() == 200) {
			try {
				System.out.println("Going to read the content of the file!");
				InputStream in = r.getStream();
				String g = new String(in.readAllBytes());
				System.out.println(g);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		//Scanner sc = new Scanner(System.in);
		
		DownloadFile cd = new DownloadFile();
		
		//System.out.println("Provide the name of the directory to be created:");
		//String directory = sc.nextLine().trim();
		String filePath ="/sd2021_aula9/test/ok.txt";
		//sc.close();
		
		boolean success = cd.execute(filePath);
		if(success)
			System.out.println("Filepath '" + filePath + "' created successfuly.");
		else
			System.out.println("Failed to read file '" + filePath + "'");
	}

}
