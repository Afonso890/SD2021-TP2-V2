package tp1.api.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import tp1.api.Spreadsheet;
import tp1.api.SpreadsheetWrapper;

public class MemoryStorage implements StorageInterface {
	private Map<String,SpreadsheetWrapper> spreadSheets;


	public MemoryStorage() {
		spreadSheets = new HashMap<String, SpreadsheetWrapper>();
	}

	@Override
	public Spreadsheet get(String sheetid) {
		try {
			return spreadSheets.get(sheetid).getSheet();
		}catch(Exception e) {
			return null;
		}
	}
	@Override
	public Spreadsheet put(String sheetid, Spreadsheet sheet) {
		SpreadsheetWrapper sw = new SpreadsheetWrapper(sheet,System.currentTimeMillis());
		spreadSheets.put(sheetid,sw);
		return sheet;
	}

	@Override
	public Spreadsheet remove(String sheetid) {
		try {
			return spreadSheets.remove(sheetid).getSheet();
		}catch(Exception e) {
			return null;
		}
	}

	@Override
	public void updateCell(Spreadsheet sp, String cell, String rawValue) {
		sp.setCellRawValue(cell, rawValue);		
	}

	@Override
	public boolean share(Spreadsheet sp, String userid) {
		return sp.getSharedWith().add(userid);
	}

	@Override
	public boolean unShare(Spreadsheet sp, String userid) {
		return sp.getSharedWith().remove(userid);
	}

	@Override
	public void deleteSheetsOfThisUser(String userid) {
		Iterator<Entry<String,SpreadsheetWrapper>> it = spreadSheets.entrySet().iterator();
		Entry<String,SpreadsheetWrapper> sp;
		while(it.hasNext()) {
			sp=it.next();
			if(sp.getValue().getSheet().getOwner().equals(userid)) {
				it.remove();
			}
		}
	}

	@Override
	public SpreadsheetWrapper getSpreadsheet(String sheetid) {
		// TODO Auto-generated method stub
		return spreadSheets.get(sheetid);
	}
}
