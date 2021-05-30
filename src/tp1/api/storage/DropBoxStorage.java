package tp1.api.storage;
import tp1.api.Spreadsheet;
import tp1.api.SpreadsheetWrapper;
import tp1.api.dropbox.DropboxOperations;

public class DropBoxStorage implements StorageInterface{
	DropboxOperations dp;
	public DropBoxStorage(String domainName, boolean clean) {
		dp=new DropboxOperations("/"+domainName,clean);
	}

	private SpreadsheetWrapper getAux(String sheetid) {
		String[] arrOfStr = sheetid.split("\\$",2);
		if(arrOfStr.length < 2)
			return null;
		String path = arrOfStr[1] + "/" + sheetid;
		return dp.downloadFile(path);
	}
	@Override
	public Spreadsheet get(String sheetid) {
		SpreadsheetWrapper sp = getAux(sheetid);
		try {
			return sp.getSheet();
		}catch(Exception e) {
			return null;
		}
	}

	@Override
	public Spreadsheet put(String sheetid, Spreadsheet sheet) {
		SpreadsheetWrapper sw = new SpreadsheetWrapper(sheet,System.currentTimeMillis());
		boolean added=dp.createFile(sw);
		if(added) {
			return sheet;
		}
		return null;
	}
	
	public Spreadsheet removeUserFolder(String path) {
		try {
			return dp.delete(path).getSheet();
		}catch(Exception e) {
			return null;
		}
	}
	
	@Override
	public Spreadsheet remove(String sheetid) {
		String[] arrOfStr = sheetid.split("\\$", 2);
		String path = arrOfStr[1] + "/" + sheetid;
		try {
			return dp.delete(path).getSheet();
		}catch(Exception e) {
			return null;
		}
	}

	@Override
	public void updateCell(Spreadsheet sp, String cell, String rawValue) {
		sp.setCellRawValue(cell,rawValue);
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

	@Override
	public SpreadsheetWrapper getSpreadsheet(String sheetid) {
		// TODO Auto-generated method stub
		return getAux(sheetid);
	}

}
