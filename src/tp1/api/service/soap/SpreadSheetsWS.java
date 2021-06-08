package tp1.api.service.soap;

import jakarta.jws.WebService;

import tp1.api.Spreadsheet;
import tp1.api.SpreadsheetValuesWrapper;
import tp1.api.discovery.Discovery;
import tp1.api.servers.resources.SpreadSheetsSharedMethods;
import tp1.api.storage.StorageInterface;

@WebService(serviceName=SoapSpreadsheets.NAME, targetNamespace=SoapSpreadsheets.NAMESPACE, endpointInterface=SoapSpreadsheets.INTERFACE)
public class SpreadSheetsWS implements SoapSpreadsheets{
	
	private final SpreadSheetsSharedMethods resource;

	public SpreadSheetsWS(String domainName, Discovery martian, String uri, StorageInterface spreadSheets,String secret) {
		resource = new SpreadSheetsSharedMethods(domainName, martian, uri,spreadSheets,secret);
	}
	@Override
	public String createSpreadsheet(Spreadsheet sheet, String password) throws SheetsException {
		try{
			return resource.createSpreadsheet(sheet, password);
		}catch(Exception e) {
			throw new SheetsException(e.getLocalizedMessage());
		}
	}

	@Override
	public void deleteSpreadsheet(String sheetId, String password) throws SheetsException {
		try{
			resource.deleteSpreadsheet(sheetId, password);
		}catch(Exception e) {
			throw new SheetsException(e.getLocalizedMessage());
		}
	}

	@Override
	public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) throws SheetsException {
		try{
			return resource.getSpreadsheet(sheetId, userId, password);
		}catch(Exception e) {
			throw new SheetsException(e.getLocalizedMessage());
		}
	}

	@Override
	public void shareSpreadsheet(String sheetId, String userId, String password) throws SheetsException {
		try{
			resource.shareSpreadsheet(sheetId,userId, password);
		}catch(Exception e) {
			throw new SheetsException(e.getLocalizedMessage());
		}
	}

	@Override
	public void unshareSpreadsheet(String sheetId, String userId, String password) throws SheetsException {
		try{
			resource.unshareSpreadsheet(sheetId, userId, password);
		}catch(Exception e) {
			throw new SheetsException(e.getLocalizedMessage());
		}
	}

	@Override
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password)
			throws SheetsException {
		try{
			resource.updateCell(sheetId, cell, rawValue, userId, password);
		}catch(Exception e) {
			throw new SheetsException(e.getLocalizedMessage());
		}
	}

	@Override
	public String[][] getSpreadsheetValues(String sheetId, String userId, String password) throws SheetsException {
		try{
			return resource.getSpreadsheetValues(sheetId, userId, password);
		}catch(Exception e) {
			throw new SheetsException(e.getLocalizedMessage());
		}
	}
	@Override
	public SpreadsheetValuesWrapper importRange(String sheetId, String range,String email,String secret) throws SheetsException {
		try{
			return resource.importRange(sheetId,range,email,secret);
		}catch(Exception e) {
			throw new SheetsException(e.getLocalizedMessage());
		}
	}
	@Override
	public void deleteSpreadsheet2(String userId,String secret) throws SheetsException {
		resource.deleteSpreadsheetOfThisUSer(userId,secret);
	}

}
