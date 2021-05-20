package tp1.api.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import tp1.api.Spreadsheet;

public class MemoryStorage implements StorageInterface {
	private Map<String,Spreadsheet> spreadSheets;

	public MemoryStorage() {
		// TODO Auto-generated constructor stub
		spreadSheets = new HashMap<String, Spreadsheet>();
	}

	@Override
	public Spreadsheet get(String sheetid) {
		// TODO Auto-generated method stub
		return spreadSheets.get(sheetid);
	}

	@Override
	public Spreadsheet put(String sheetid, Spreadsheet sheet) {
		// TODO Auto-generated method stub
		return spreadSheets.put(sheetid, sheet);
	}

	@Override
	public Spreadsheet remove(String sheetid) {
		// TODO Auto-generated method stub
		return spreadSheets.remove(sheetid);
	}

	@Override
	public Iterator<Entry<String, Spreadsheet>> entries() {
		// TODO Auto-generated method stub
		return spreadSheets.entrySet().iterator();
	}

}
