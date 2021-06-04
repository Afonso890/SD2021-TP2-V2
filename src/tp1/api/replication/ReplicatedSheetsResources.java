package tp1.api.replication;

import jakarta.ws.rs.WebApplicationException;


import jakarta.ws.rs.core.Response.Status;
import tp1.api.Spreadsheet;
import tp1.api.SpreadsheetValuesWrapper;
import tp1.api.consts.Consts;
import tp1.api.replication.args.CreateSpreadSheet;
import tp1.api.replication.args.DeletSpreadsheet;
import tp1.api.replication.args.DeleteUsersSheets;
import tp1.api.replication.args.Share;
import tp1.api.replication.args.Unshare;
import tp1.api.replication.args.Update;
import tp1.api.replication.sync.SyncPoint;
import tp1.api.servers.resources.SpreadSheetResource;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.api.service.rest.RestSpreadsheetsReplication;

public class ReplicatedSheetsResources implements RestSpreadsheetsReplication {

	private KafkaOperationsHandler operations;
	private SyncPoint sync;
	
	private RestSpreadsheets resource;
	public ReplicatedSheetsResources(RestSpreadsheets resource, SyncPoint sync,KafkaOperationsHandler operations ) {
		this.operations=operations;
		this.sync=sync;
		this.resource=resource;
		System.out.println("==================================SERVER REPLICATED STARTED --------------------------------------->");
	}
	
	
	private long max(Long version) {
		if(version==null) {
			return operations.getVersionNumber();
		}else {
			return Math.max(operations.getVersionNumber(),version);
		}
	}
	
	
	@Override
	public String createSpreadsheet(Long version, Spreadsheet sheet, String password) {
		try {
			CreateSpreadSheet create = new CreateSpreadSheet(sheet, password);
			
			operations.sender(ReceiveOperationArgs.CREATE_SPREADSHEET,Consts.json.toJson(create));
			String result = sync.waitForResult(max(version));
			
			ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		
			if(res.getStatus()==Status.OK) {
				return res.getObjResponse();
			}
			throw new WebApplicationException(res.getStatus());
		}catch(WebApplicationException e) {
			throw e;
		}catch(Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void deleteSpreadsheet(Long version, String sheetId, String password) {
		DeletSpreadsheet delete = new DeletSpreadsheet(password, sheetId);
		operations.sender(ReceiveOperationArgs.DELETE_SPREADSHEET,Consts.json.toJson(delete));
		String result = sync.waitForResult(max(version));
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public void deleteSpreadsheet(Long version, String userId) {
		DeleteUsersSheets delete = new DeleteUsersSheets(userId);
		operations.sender(ReceiveOperationArgs.DELETE_USERS_SPREADSHEET,Consts.json.toJson(delete));
		String result = sync.waitForResult(max(version));
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public Spreadsheet getSpreadsheet(Long version, String sheetId, String userId, String password) {
		// TODO Auto-generated method stub
		
		System.out.println("VVVVVVVVVVV--------------------------------------- "+operations.getVersionNumber()+" ppppppppppppppppppppppppp "+version);
		try {
			if(version>operations.getVersionNumber()) {
				sync.waitForVersion(version);
			}
			return resource.getSpreadsheet(sheetId, userId, password);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String[][] getSpreadsheetValues(Long version, String sheetId, String userId, String password) {
		if(version>operations.getVersionNumber()) {
			sync.waitForVersion(version);
		}		
		return resource.getSpreadsheetValues(sheetId, userId, password);
	}

	@Override
	public SpreadsheetValuesWrapper importRange(Long version, String sheetId, String range, String email) {
		if(version>operations.getVersionNumber()) {
			sync.waitForVersion(version);
		}
		return resource.importRange(sheetId, range, email);
	}

	@Override
	public void updateCell(Long version, String sheetId, String cell, String rawValue, String userId, String password) {
		Update update = new Update(sheetId, cell, rawValue, userId, password);
		operations.sender(ReceiveOperationArgs.UPDATE_SPREADSHEET,Consts.json.toJson(update));
		String result = sync.waitForResult(max(version));
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public void shareSpreadsheet(Long version, String sheetId, String userId, String password) {
		Share share = new Share(sheetId, userId, password);
		operations.sender(ReceiveOperationArgs.SHARE_SPREADSHEET,Consts.json.toJson(share));
		String result = sync.waitForResult(max(version));
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public void unshareSpreadsheet(Long version, String sheetId, String userId, String password) {
		Unshare unshare = new Unshare(sheetId, userId, password);
		operations.sender(ReceiveOperationArgs.UNSHARE_SPREADSHEET,Consts.json.toJson(unshare));
		String result = sync.waitForResult(max(version));
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}
	
}
