package tp1.api.storage;
import java.util.Iterator;

import java.util.List;
import java.util.Map.Entry;

import tp1.api.Spreadsheet;
import tp1.api.dropbox.DropboxOperations;

public class DropBoxStorage implements StorageInterface{
	DropboxOperations dp;
	public DropBoxStorage(String domainName, boolean clean) {
		dp=new DropboxOperations("/"+domainName,clean);
	}

	@Override
	public Spreadsheet get(String sheetid) {
		String[] arrOfStr = sheetid.split("\\$",2);
		if(arrOfStr.length < 2)
			return null;
		String path = arrOfStr[1] + "/" + sheetid;
		return dp.downloadFile(path);
	}

	@Override
	public Spreadsheet put(String sheetid, Spreadsheet sheet) {
		boolean added=dp.createFile(sheet);
		if(added) {
			return sheet;
		}
		return null;
	}
	
	public Spreadsheet removeUserFolder(String path) {
		return dp.delete(path);
	}
	
	@Override
	public Spreadsheet remove(String sheetid) {
		String[] arrOfStr = sheetid.split("\\$", 2);
		String path = arrOfStr[1] + "/" + sheetid;
		return dp.delete(path);
	}

	@Override
	public Iterator<Entry<String, Spreadsheet>> entries() {
		List<Entry<String,Spreadsheet>> sheets = dp.listDirectory();
		return sheets.iterator();
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
		this.removeUserFolder(userid);
	}

}
