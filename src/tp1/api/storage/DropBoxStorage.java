package tp1.api.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;

import tp1.api.Spreadsheet;
import tp1.api.dropbox.DropboxOperations;

public class DropBoxStorage implements StorageInterface{
	DropboxOperations dp;
	public DropBoxStorage(String domainName) {
		dp=new DropboxOperations("/"+domainName);
	}

	@Override
	public Spreadsheet get(String sheetid) {
		return dp.downloadFile(sheetid);
	}

	@Override
	public Spreadsheet put(String sheetid, Spreadsheet sheet) {
		
		dp.createFile(sheet);
		return sheet;
	}

	@Override
	public Spreadsheet remove(String sheetid) {
		return dp.delete(sheetid);
	}

	@Override
	public Iterator<Entry<String, Spreadsheet>> entries() {
		List<String> listJson = dp.listDirectory();
		Map<String,Spreadsheet> spreadSheets = new HashMap<String, Spreadsheet>();
		
		Gson json=new Gson();
		
		Spreadsheet sp;

		for (String jsonSheet : listJson) {
			
			sp=json.fromJson(jsonSheet,Spreadsheet.class);
			spreadSheets.put(sp.getSheetId(), sp);
			
		}
		return spreadSheets.entrySet().iterator();
	}

}
