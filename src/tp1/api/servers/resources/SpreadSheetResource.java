package tp1.api.servers.resources;
import jakarta.inject.Singleton;

import tp1.api.Spreadsheet;
import tp1.api.discovery.Discovery;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.api.storage.StorageInterface;

@Singleton
public class SpreadSheetResource implements RestSpreadsheets{
	//private static Logger Log = Logger.getLogger(SpreadSheetResource.class.getName());

	private final SpreadSheetsSharedMethods resource;

	public SpreadSheetResource(String domainName, Discovery martian,String uri, StorageInterface spreadSheets) {
		resource = new SpreadSheetsSharedMethods(domainName, martian, uri,spreadSheets);
	}
	
	@Override
	public String createSpreadsheet(Spreadsheet sheet, String password) {
		return resource.createSpreadsheet(sheet, password);
	}

	@Override
	public void deleteSpreadsheet(String sheetId, String password) {
		resource.deleteSpreadsheet(sheetId,password);
	}
	@Override
	public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
		return resource.getSpreadsheet(sheetId, userId, password);
	}

	@Override
	public String[][] getSpreadsheetValues(String sheetId, String userId, String password) {
		return resource.getSpreadsheetValues(sheetId, userId, password);
	}
	//import range
	@Override
	public String[][] importRange(String sheetId, String range,String email) {
		return resource.importRange(sheetId,range,email);
	}

	@Override
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		resource.updateCell(sheetId, cell, rawValue, userId, password);		
	}
	
	@Override
	public void shareSpreadsheet(String sheetId, String userId, String password) {
		resource.shareSpreadsheet(sheetId, userId, password);
	}

	@Override
	public void unshareSpreadsheet(String sheetId, String userId, String password) {
		resource.unshareSpreadsheet(sheetId, userId, password);
	}
	@Override
	public void deleteSpreadsheet(String userId) {
		resource.deleteSpreadsheet(userId);
	}
	

}
