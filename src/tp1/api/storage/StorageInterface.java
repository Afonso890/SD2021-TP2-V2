package tp1.api.storage;

import tp1.api.Spreadsheet;
import tp1.api.SpreadsheetWrapper;

public interface StorageInterface {
	
	public Spreadsheet get(String sheetid);
	
	public Spreadsheet put(String sheetid, Spreadsheet sheet);
	
	public Spreadsheet remove(String sheetid);
		
	public void updateCell(Spreadsheet sp, String cell, String rawValue);
	
	/**
	 * 
	 * @param sp
	 * @param userid
	 * @return true if the user was not in the shared list
	 */
	public boolean share(Spreadsheet sp,String userid);
	
	/**
	 * 
	 * @param sp
	 * @param userid
	 * @return true if the user was in the shared list and was successfully removed
	 */
	public boolean unShare(Spreadsheet sp,String userid);
	/**
	 * Removes all spreadsheets of this user
	 * @param userid
	 */
	public void deleteSheetsOfThisUser(String userid);
	
	public SpreadsheetWrapper getSpreadsheet(String sheetid);



}
