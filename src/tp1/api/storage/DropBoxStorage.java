package tp1.api.storage;
import java.util.Iterator;
import java.util.List;
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
		boolean added=dp.createFile(sheet);
		if(added) {
			return sheet;
		}
		return null;
	}

	@Override
	public Spreadsheet remove(String sheetid) {
		return dp.delete(sheetid);
	}

	@Override
	public Iterator<Entry<String, Spreadsheet>> entries() {
		List<Entry<String,Spreadsheet>> sheets = dp.listDirectory();
		if(sheets!=null) {
			return sheets.iterator();
		}
		return null;
	}

	@Override
	public void updateCell(Spreadsheet sp, String cell, String rawValue) {
		sp.setCellRawValue(cell, rawValue);
		put(sp.getSheetId(),sp);
	}

	@Override
	public boolean share(Spreadsheet sp, String userid) {
		boolean added=sp.getSharedWith().add(userid);
		if(added) {
			put(sp.getSheetId(),sp);
		}
		return added;
	}

	@Override
	public boolean unShare(Spreadsheet sp, String userid) {
		boolean removed = sp.getSharedWith().remove(userid);
		if(removed) {
			put(sp.getSheetId(),sp);
		}
		return removed;
	}

	@Override
	public void deleteSheetsOfThisUser(String userid) {
		Iterator<Entry<String,Spreadsheet>> it = entries();
		Entry<String,Spreadsheet> sp;
		while(it.hasNext()) {
			sp=it.next();
			if(sp.getValue().getOwner().equals(userid)) {
				remove(sp.getKey());
			}
		}
	}

}
