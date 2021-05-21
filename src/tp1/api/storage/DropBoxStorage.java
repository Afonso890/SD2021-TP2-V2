package tp1.api.storage;

import java.util.Iterator;
import java.util.Map.Entry;

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
