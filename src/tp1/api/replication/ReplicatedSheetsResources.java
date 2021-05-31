package tp1.api.replication;

import jakarta.ws.rs.WebApplicationException;
import tp1.api.Spreadsheet;
import tp1.api.SpreadsheetValuesWrapper;
import tp1.api.consts.Consts;
import tp1.api.replication.args.CreateSpreadSheet;
import tp1.api.replication.sync.SyncPoint;
import tp1.api.servers.resources.SpreadSheetResource;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.util.Pair;

public class ReplicatedSheetsResources implements RestSpreadsheets {

	private KafkaOperationsHandler operations;
	private SyncPoint sync;
	public ReplicatedSheetsResources(SpreadSheetResource resource, SyncPoint sync) {
		operations=new KafkaOperationsHandler(resource.getDomain(),resource,sync);
		this.sync=sync;
	}

	@Override
	public String createSpreadsheet(Spreadsheet sheet, String password) {
		CreateSpreadSheet create = new CreateSpreadSheet(sheet, password);
		operations.sender(ReceiveOperationArgs.CREATE_SPREADSHEET,Consts.json.toJson(create));
		String result = sync.waitForResult(operations.getVersionNumber());
		
		Pair<jakarta.ws.rs.core.Response.Status,String> res=Consts.json.fromJson(result,Pair.class);
		result= res.getValue2();
		if(result==null) {
			throw new WebApplicationException(res.getValue1());
		}
		return result;
	}

	@Override
	public void deleteSpreadsheet(String sheetId, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteSpreadsheet(String userId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[][] getSpreadsheetValues(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpreadsheetValuesWrapper importRange(String sheetId, String range, String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shareSpreadsheet(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unshareSpreadsheet(String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		
	}
	
}
