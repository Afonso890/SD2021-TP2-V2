package tp1.util;

import jakarta.ws.rs.client.Client;
import tp1.api.Spreadsheet;
import tp1.api.engine.AbstractSpreadsheet;
import tp1.api.spreadsheet.clients.ImportRange;

public class GetAbstractSpreadSheet {

	
	public static AbstractSpreadsheet getTheOne(Spreadsheet sp, String domain, Client client) {
		return  new AbstractSpreadsheet() {
			
			@Override
			public String sheetId() {
				// TODO Auto-generated method stub
				return sp.getSheetId();
			}
			
			@Override
			public int rows() {
				// TODO Auto-generated method stub
				return sp.getRows();
			}
			
			@Override
			public String[][] getRangeValues(String sheetURL, String range) {
				// TODO Auto-generated method stub
				//getValues from the sheetURL server
				//CellRange r = new CellRange( range );
				//String [] uris = martian.knownUrisOf(sheetURL);
				return ImportRange.importRange(sheetURL,range,sp.getOwner()+"@"+domain,client);
			}
			
			@Override
			public int columns() {
				// TODO Auto-generated method stub
				return sp.getColumns();
			}
			
			@Override
			public String cellRawValue(int row, int col) {
				// TODO Auto-generated method stub
				return sp.getCellRawValue(row, col);
			}
		};
	}
}
