package tp1.api.servers.resources;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.stream.Stream;

import jakarta.inject.Singleton;

import tp1.api.Spreadsheet;
import tp1.api.SpreadsheetValuesWrapper;
import tp1.api.discovery.Discovery;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.api.storage.StorageInterface;

@Singleton
public class SpreadSheetResource implements RestSpreadsheets{
	//private static Logger Log = Logger.getLogger(SpreadSheetResource.class.getName());

	private final SpreadSheetsSharedMethods resource;
	public SpreadSheetResource(String domainName, Discovery martian,String uri, StorageInterface spreadSheets,String secrete) {
		resource = new SpreadSheetsSharedMethods(domainName, martian, uri, spreadSheets,secrete);
	}
	public String getDomain() {
		return resource.getDomain();
	}
	
	@Override
	public String createSpreadsheet(Spreadsheet sheet, String password) {
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

		String result = resource.createSpreadsheet(sheet, password);

		//publish
		return result;
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
	public SpreadsheetValuesWrapper importRange(String sheetId, String range,String email,String secret) {
		return resource.importRange(sheetId,range,email,secret);
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
	public void deleteSpreadsheetOfThisUser(String userId, String secrete) {
		resource.deleteSpreadsheetOfThisUSer(userId, secrete);
	}
		

}