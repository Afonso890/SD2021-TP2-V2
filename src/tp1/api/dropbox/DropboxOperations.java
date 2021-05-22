package tp1.api.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.sun.net.httpserver.Authenticator.Result;

import sd2021.aula9.dropbox.replies.ListFolderReturn;
import sd2021.aula9.dropbox.replies.ListFolderReturn.FolderEntry;
import tp1.api.Spreadsheet;
import tp1.api.consts.Consts;
import tp1.api.dropbox.args.AccessFileV2Args;
import tp1.api.dropbox.args.CreateFileArgs;
import tp1.api.dropbox.args.CreateFolderV2Args;
import tp1.api.dropbox.args.DownloadFileArgs;
import tp1.api.dropbox.args.ListFolderArgs;
import tp1.api.dropbox.args.ListFolderContinueArgs;


public class DropboxOperations {
	private OAuth20Service service;
	private OAuth2AccessToken accessToken;
	private Gson json;
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	private static final String CREATE_FOLDER_V2_URL = "https://api.dropboxapi.com/2/files/create_folder_v2";
	private static final String UPLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/upload";
	private static final String DOWNLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/download";
	private static final String DELETE_FILE_V2_URL = "https://api.dropboxapi.com/2/files/delete_v2";
	private static final String LIST_FOLDER_URL = "https://api.dropboxapi.com/2/files/list_folder";
	private static final String LIST_FOLDER_CONTINUE_URL = "https://api.dropboxapi.com/2/files/list_folder/continue";
	private String directoryName;

	public DropboxOperations(String directoryName) {
		service = new ServiceBuilder(Consts.apiKey).apiSecret(Consts.apiSecret).build(DropboxApi20.INSTANCE);
		accessToken = new OAuth2AccessToken(Consts.accessTokenStr);	
		json=new Gson();
		this.directoryName=directoryName;	}
	
	
	
	public boolean createFolder() {
		OAuthRequest createFolder = new OAuthRequest(Verb.POST, CREATE_FOLDER_V2_URL);
		createFolder.addHeader("Content-Type", JSON_CONTENT_TYPE);

		createFolder.setPayload(Consts.json.toJson(new CreateFolderV2Args(directoryName, false)));
		
		service.signRequest(accessToken, createFolder);
		
		Response r = null;
		
		try {
			r = service.execute(createFolder);
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
	public boolean createFile(Spreadsheet sheet) {
		OAuthRequest downloadFile = new OAuthRequest(Verb.POST,UPLOAD_FILE_URL);
		downloadFile.addHeader("Content-Type", Consts.TEXT_CONTENT_TYPE2);
		String filePath=directoryName+"/"+sheet.getSheetId();
		String args=json.toJson(new CreateFileArgs(filePath));
		downloadFile.addHeader("Dropbox-API-Arg",args);
		String msg=json.toJson(sheet);
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
	public Spreadsheet downloadFile( String sheetid ) {
		OAuthRequest downloadFile = new OAuthRequest(Verb.POST,DOWNLOAD_FILE_URL);
		downloadFile.addHeader("Content-Type", Consts.TEXT_CONTENT_TYPE);
		String filePath=directoryName+"/"+sheetid;
		downloadFile.addHeader("Dropbox-API-Arg",json.toJson(new DownloadFileArgs(filePath)));

		service.signRequest(accessToken, downloadFile);
		
		Response r = null;
		
		try {
			r = service.execute(downloadFile);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		Spreadsheet sp=null;
		if(r.getCode() == 200) {
			try {
				InputStream in = r.getStream();
				String g = new String(in.readAllBytes());
				sp=json.fromJson(g,Spreadsheet.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("HTTP Error Code: " + r.getCode() + ": " + r.getMessage());
			try {
				System.err.println(r.getBody());
			} catch (IOException e) {
				System.err.println("No body in the response");
			}
		}
		return sp;
	}
	
	public Spreadsheet delete(String sheetid) {
		OAuthRequest deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_V2_URL);
		deleteFile.addHeader("Content-Type", JSON_CONTENT_TYPE);
		
		String filePath=directoryName +"/"+sheetid;
		
		deleteFile.setPayload(json.toJson(new AccessFileV2Args(filePath)));
		service.signRequest(accessToken, deleteFile);
		
		Response r = null;
		Spreadsheet sp=null;
		
		try{
			r = service.execute(deleteFile);
			
			if (r.getCode() == 200) {
				InputStream in = r.getStream();
				String g = new String(in.readAllBytes());
				sp=json.fromJson(g,Spreadsheet.class);
				System.err.println("Dropbox file was deleted with success");
				return sp;
			} else {
				System.err.println("HTTP Error Code: " + r.getCode() + ": " + r.getMessage());
				try {
					System.err.println(r.getBody());
				} catch (IOException e) {
					System.err.println("No body in the response");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return sp;
	}
	
	public List<String> listDirectory() {
		List<String> directoryContents = new ArrayList<String>();
		
		OAuthRequest listDirectory = new OAuthRequest(Verb.POST, LIST_FOLDER_URL);
		listDirectory.addHeader("Content-Type", JSON_CONTENT_TYPE);
		listDirectory.setPayload(json.toJson(new ListFolderArgs(directoryName, false)));
		
		service.signRequest(accessToken, listDirectory);
		
		Response r = null;
		
		try {
			while(true) {
				r = service.execute(listDirectory);
				
				if(r.getCode() != 200) {
					System.err.println("Failed to list directory '" + directoryName + "'. Status " + r.getCode() + ": " + r.getMessage());
					System.err.println(r.getBody());
					return null;
				}
				
				ListFolderReturn reply = json.fromJson(r.getBody(), ListFolderReturn.class);
				
				for(FolderEntry e: reply.getEntries()) {
					directoryContents.add(e.toString());
				}
				
				if(reply.has_more()) {
					//There are more elements to read, prepare a new request (now a continuation)
					listDirectory = new OAuthRequest(Verb.POST, LIST_FOLDER_CONTINUE_URL);
					listDirectory.addHeader("Content-Type", JSON_CONTENT_TYPE);
					//In this case the arguments is just an object containing the cursor that was returned in the previous reply.
					listDirectory.setPayload(json.toJson(new ListFolderContinueArgs(reply.getCursor())));
					service.signRequest(accessToken, listDirectory);
				} else {
					break; //There are no more elements to read. Operation can terminate.
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
			
		return directoryContents;
	}
	
}