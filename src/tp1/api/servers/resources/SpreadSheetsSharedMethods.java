package tp1.api.servers.resources;

import java.util.HashSet;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.Spreadsheet;
import tp1.api.SpreadsheetValuesWrapper;
import tp1.api.SpreadsheetWrapper;
import tp1.api.User;
import tp1.api.clients.GetUserClient;
import tp1.api.consts.Consts;
import tp1.api.discovery.Discovery;
import tp1.api.server.rest.UsersServer;
import tp1.api.storage.StorageInterface;
import tp1.impl.engine.SpreadsheetEngineImpl;
import tp1.util.GetAbstractSpreadSheet;

public class SpreadSheetsSharedMethods {
	//private static Logger Log = Logger.getLogger(SpreadSheetResource.class.getName());
	private Discovery martian;
	private String domainName;
	private StorageInterface spreadSheets;
	private Client client;
	private int ids;
	private String uri;
	private String users_domain;
	private String secrete;

	public SpreadSheetsSharedMethods(String domainName, Discovery martian, String uri, StorageInterface spreadSheets,String secret) {
		this.domainName=domainName;
		this.client = Consts.client;
		this.spreadSheets=spreadSheets;
		this.martian=martian;
		this.uri=uri;
		users_domain=domainName+":"+UsersServer.SERVICE;
		ids=0;
		this.secrete=secret;
	}
	private boolean validSecrete(String sec) {
		return secrete.equals(sec);
	}
	/**
	 * 
	 * @return the domain name of the server of users for this spreadsheet domain
	 */
	public String getUsersDomain() {
		return users_domain;
	}
	public Discovery getDiscovery() {
		return martian;
	}
	public String getUri() {
		return uri;
	}
	/**
	 * makes a request to the users server to verify the user
	 * @param owner
	 * @param password
	 * @param s
	 */
	public void passwordIsCorrect(String owner, String password, Status s) {
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
	public String userExists(String useremail) {	
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
	public Spreadsheet hasSpreadSheet(String spreadid) {
		Spreadsheet sp = spreadSheets.get(spreadid);
		if(sp==null) {
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		return sp;
	}
	public void validSheet(Spreadsheet sheet) {
		if(sheet.getSheetId()!=null || sheet.getColumns()<=Consts.ZERO || sheet.getRows()<=Consts.ZERO) {
			throw new WebApplicationException( Status.CONFLICT );
		}
	}
	public String getDomain() {
		return domainName;
	}
	
	public String createSpreadsheet(Spreadsheet sheet, String password) {
		passwordIsCorrect(sheet.getOwner(),password,Status.BAD_REQUEST);
		try {
			validSheet(sheet);
			synchronized (this) {
				ids++;
				//UUID.randomUUID().toString();
				String sheetId = ids+"$"+sheet.getOwner();
				sheet.setSheetId(sheetId);
				sheet.setSheetURL(uri+"_"+sheet.getSheetId());
				spreadSheets.put(sheet.getSheetId(),sheet);
			}
		}catch(Exception e) {
			throw new WebApplicationException( Status.BAD_REQUEST );
		}
		return sheet.getSheetId();
	}

	public void deleteSpreadsheet(String sheetId, String password) {
		synchronized (this) {
			Spreadsheet sp = hasSpreadSheet(sheetId);
			passwordIsCorrect(sp.getOwner(),password,Status.FORBIDDEN);
			sp = spreadSheets.remove(sheetId);
		}
	}
	public boolean hasAccess(Spreadsheet sp, String userId) {
		if(sp.getOwner().equalsIgnoreCase(userId)) {
			return true;
		}
		return sp.getSharedWith().contains(userId+"@"+domainName);
	}
	
	public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
		Spreadsheet sp;
		synchronized (spreadSheets) {
			sp =hasSpreadSheet(sheetId);
			if(!hasAccess(sp,userId)) {
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			User u = userExists2(userId,domainName);
			if(!u.getPassword().equals(password)) {
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			//passwordIsCorrect(userId,password,Status.FORBIDDEN);
		}
		return sp;
	}
	public String[][] getSpreadsheetValues(String sheetId, String userId, String password) {
		Spreadsheet sp =hasSpreadSheet(sheetId);
		if(!hasAccess(sp,userId)) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		passwordIsCorrect(userId,password,Status.FORBIDDEN);
		String [][] values=null;
		try {
			values=SpreadsheetEngineImpl.getInstance().computeSpreadsheetValues(GetAbstractSpreadSheet.getTheOne(sp,domainName,client,secrete));
		}catch(Exception e) {
			e.printStackTrace();
		}
		return values;
	}
	//import range
	
	public SpreadsheetValuesWrapper importRange(String sheetId,String range,String email,String secret) {
		if(!validSecrete(secret)) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		SpreadsheetWrapper spw = spreadSheets.getSpreadsheet(sheetId);
		if(spw==null) {
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		Spreadsheet sp = spw.getSheet();
		String [][] values;
		String userId = email.split("@")[0];
		if(sp.getOwner().equals(userId)||sp.getSharedWith().contains(email)){
			values=SpreadsheetEngineImpl.getInstance().computeSpreadsheetValues(GetAbstractSpreadSheet.getTheOne(sp,domainName,client,secret));
			return new  SpreadsheetValuesWrapper(values,spw.getTw_server());
		}else {
			throw new WebApplicationException(Status.FORBIDDEN);
		}	
	}

	
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		synchronized (spreadSheets) {
			Spreadsheet sp =hasSpreadSheet(sheetId);
			if(!hasAccess(sp,userId)) {
				throw new WebApplicationException(Status.FORBIDDEN);
			}
			passwordIsCorrect(userId,password,Status.FORBIDDEN);
			//sp.setCellRawValue(cell, rawValue);
			//cachedValues.remove(sheetId);
			spreadSheets.updateCell(sp,cell,rawValue);
		}
	}
	public void shareSpreadsheet(String sheetId, String userId, String password) {
		synchronized (spreadSheets) {
			Spreadsheet sp=hasSpreadSheet(sheetId);
			userExists(userId);
			passwordIsCorrect(sp.getOwner(),password,Status.FORBIDDEN);			
			if(sp.getSharedWith()==null) {
				sp.setSharedWith(new HashSet<String>());
			}
			if(!spreadSheets.share(sp, userId)) {
				throw new WebApplicationException( Status.CONFLICT );
			}
		}
	}
	
	public void unshareSpreadsheet(String sheetId, String userId, String password) {
		synchronized (spreadSheets) {
			userId = userExists(userId);
			Spreadsheet sp =hasSpreadSheet(sheetId);
			passwordIsCorrect(sp.getOwner(),password,Status.FORBIDDEN);
			if(!spreadSheets.unShare(sp,userId+"@"+domainName)) {
				throw new WebApplicationException( Status.NOT_FOUND );
			}
		}
	}
	/**
	 * 
	 * @param userId
	 */
	public void deleteSpreadsheetOfThisUSer(String userId,String secret) {
		if(!validSecrete(secret)) {
			System.out.println(this.secrete+"----------------- "+secret);
			System.err.println("SECRETE IS WRONG!");
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		synchronized (spreadSheets) {
			spreadSheets.deleteSheetsOfThisUser(userId);
		}		
	}
}