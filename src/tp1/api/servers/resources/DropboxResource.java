package tp1.api.servers.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.Spreadsheet;
import tp1.api.User;
import tp1.api.clients.GetUserClient;
import tp1.api.consts.Consts;
import tp1.api.discovery.Discovery;
import tp1.api.dropbox.args.CreateFileArgs;
import tp1.api.dropbox.args.CreateFolderV2Args;
import tp1.api.dropbox.args.DownloadFileArgs;
import tp1.api.server.rest.UsersServer;
import tp1.api.service.rest.RestSpreadsheets;

public class DropboxResource  implements RestSpreadsheets{
	
	private static final String apiKey = "t5nzwsbik89p5s0";
	private static final String apiSecret = "31kkgco1l2vfzdp";
	private static final String accessTokenStr = "dRhKRkkXUXQAAAAAAAAAASVCYAvD2RIUKPod26ZwW6QYGmK8n4pRhB4GxQiBf5SG";

	protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
	private static final String CREATE_FOLDER_V2_URL = "https://api.dropboxapi.com/2/files/create_folder_v2";
	private static final String DOWNLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/download";
	private static final String UPLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/upload";
	
	private OAuth20Service service;
	private OAuth2AccessToken accessToken;
	
	private Gson json;
	

	public static Discovery martian;
	private String domainName;
	private Client client;
	private int ids;
	private String uri;
	private String directoryName;
	
	public DropboxResource(String domainName, Discovery martian, String uri,String directoryName) {
		this.domainName=domainName;
		this.client = Consts.client;
		SpreadSheetsSharedMethods.martian=martian;
		this.uri=uri;
		ids=0;
		
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
		accessToken = new OAuth2AccessToken(accessTokenStr);
		
		json = new Gson();
		
		//Directory name deveria ser o domainName ???
		this.directoryName = directoryName;
		createFolder(directoryName);
	}
	
	private boolean createFolder(String directoryName)
	{
		OAuthRequest createFolder = new OAuthRequest(Verb.POST, CREATE_FOLDER_V2_URL);
		createFolder.addHeader("Content-Type", JSON_CONTENT_TYPE);

		createFolder.setPayload(json.toJson(new CreateFolderV2Args(directoryName, false)));

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
	
	
	public String downloadFile( String filePath ) {
		OAuthRequest downloadFile = new OAuthRequest(Verb.POST,DOWNLOAD_FILE_URL);
		downloadFile.addHeader("Content-Type", Consts.TEXT_CONTENT_TYPE);
		
		downloadFile.addHeader("Dropbox-API-Arg",json.toJson(new DownloadFileArgs(filePath)));

		service.signRequest(accessToken, downloadFile);
		
		Response r = null;
		
		try {
			r = service.execute(downloadFile);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		String g = null;
		
		if(r.getCode() == 200) {
			try {
				System.out.println("Going to read the content of the file!");
				InputStream in = r.getStream();
				g = new String(in.readAllBytes());
				System.out.println(g);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return g;
		} else {
			System.err.println("HTTP Error Code: " + r.getCode() + ": " + r.getMessage());
			try {
				System.err.println(r.getBody());
			} catch (IOException e) {
				System.err.println("No body in the response");
			}
			return null;
		}
		
	}
	
	public boolean creteFile( String filePath, String msg) {
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
	
	/**
	 * makes a request to the users server to verify the user
	 * @param owner
	 * @param password
	 * @param s
	 */
	private void passwordIsCorrect(String owner, String password, Status s) {
		try {
			if(password==null||"".equals(password.trim())) {
				throw new WebApplicationException(s);
			}
			User u = GetUserClient.getUser(owner,password,domainName+":"+UsersServer.SERVICE,client,martian);
			if(u==null) {
				throw new WebApplicationException(s);
			}
		}catch (Exception e) {
			throw new WebApplicationException( s );
		}
	}
	
	/**
	 * makes a request to the users server to verify if the particula usar exists
	 * @param userId -> user email
	 * @return
	 */
	private String userExists(String useremail) {	
		String [] parms = useremail.split("@");
		useremail = parms[0];
		if(parms.length!=2) {
			throw new WebApplicationException( Status.NOT_FOUND);
		}
		userExists2(parms[0],parms[1]);
		return useremail;
	}
	/**
	 * makes a request to the users server to verify if the particula usar exists
	 * @param userId -> user id
	 * @return
	 */
	private User userExists2(String userId,String domainN) {
		try {
			User u = GetUserClient.getUser(userId, null,domainN+":"+UsersServer.SERVICE,client,martian);
			if(u==null) {
				throw new WebApplicationException( Status.NOT_FOUND );
			}
			return u;
		}catch (Exception e) {
			throw new WebApplicationException( Status.NOT_FOUND );
		}
	}
	
	private Spreadsheet hasSpreadSheet(String spreadid) {
		
		String path = this.directoryName + "/" + spreadid;
		String sheetStr = this.downloadFile(path);
		
		if(sheetStr==null) {
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		Spreadsheet sp = json.fromJson(sheetStr, Spreadsheet.class);
		if(sp==null) {
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		return sp;
	}
	private void validSheet(Spreadsheet sheet) {
		if(sheet.getSheetId()!=null || sheet.getColumns()<=Consts.ZERO || sheet.getRows()<=Consts.ZERO) {
			throw new WebApplicationException( Status.CONFLICT );
		}
	}
	
	public String createSpreadsheet(Spreadsheet sheet, String password) {
		passwordIsCorrect(sheet.getOwner(),password,Status.BAD_REQUEST);
		try {
			synchronized (this) {
				validSheet(sheet);
				ids++;
				//UUID.randomUUID().toString();
				String sheetId = ids+"";
				sheet.setSheetId(sheetId);
				sheet.setSheetURL(uri+"_"+sheet.getSheetId());
				String sheetJson = json.toJson(sheet);
				//Duvida se temos de fazer hasSpreadsheet aqui para verificar se existe
				this.hasSpreadSheet(sheet.getSheetId());
				String path = this.directoryName + "/" + sheet.getSheetId();
				this.creteFile(path, sheetJson);
				
			}
		}catch(Exception e) {
			throw new WebApplicationException( Status.BAD_REQUEST );
		}

		return sheet.getSheetId();
	}

	@Override
	public void deleteSpreadsheet(String sheetId, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteSpreadsheet(String userId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[][] getSpreadsheetValues(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[][] importRange(String sheetId, String range, String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shareSpreadsheet(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unshareSpreadsheet(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		
	}

	
}
