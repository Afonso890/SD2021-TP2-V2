package tp1.api.storage;

import java.util.Iterator;
import java.util.Map.Entry;

import tp1.api.Spreadsheet;

public interface StorageInterface {
	
	public Spreadsheet get(String sheetid);
	
	public Spreadsheet put(String sheetid, Spreadsheet sheet);
	
	public Spreadsheet remove(String sheetid);
	
	public Iterator<Entry<String,Spreadsheet>> entries();


}
