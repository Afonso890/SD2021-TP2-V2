package tp1.api.storage;

import java.util.Iterator;
import java.util.Map.Entry;

import tp1.api.Spreadsheet;

public class DropBoxStorage implements StorageInterface{

	private String domainName;
	public DropBoxStorage(String domainName) {
		this.domainName=domainName;
	}

	@Override
	public Spreadsheet get(String sheetid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Spreadsheet put(String sheetid, Spreadsheet sheet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Spreadsheet remove(String sheetid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Entry<String, Spreadsheet>> entries() {
		// TODO Auto-generated method stub
		return null;
	}

}
