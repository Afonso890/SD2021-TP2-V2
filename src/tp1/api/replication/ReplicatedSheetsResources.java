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

public class ReplicatedSheetsResources implements RestSpreadsheets {

	private KafkaOperationsHandler operations;
	private SyncPoint sync;
	private SpreadSheetResource resource;
	public ReplicatedSheetsResources(SpreadSheetResource resource, SyncPoint sync) {
		operations=new KafkaOperationsHandler(resource.getDomain(),resource,sync);
		System.out.println("INICIA NOVA REP RESOURCES " + operations);
		this.sync=sync;
		this.resource=resource;
	}

	@Override
	public String createSpreadsheet(Spreadsheet sheet, String password) {
		CreateSpreadSheet create = new CreateSpreadSheet(sheet, password);
		operations.sender(ReceiveOperationArgs.CREATE_SPREADSHEET,Consts.json.toJson(create));
		System.out.println("VERSION NUMBER NO CREATE " + operations.getVersionNumber());
		String result = sync.waitForResult(operations.getVersionNumber());
		
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
	
		if(res.getStatus()==Status.OK) {
			return res.getObjResponse();
		}
		throw new WebApplicationException(res.getStatus());
	}

	@Override
	public void deleteSpreadsheet(String sheetId, String password) {
		DeletSpreadsheet delete = new DeletSpreadsheet(password, sheetId);
		operations.sender(ReceiveOperationArgs.DELETE_SPREADSHEET,Consts.json.toJson(delete));
		String result = sync.waitForResult(operations.getVersionNumber());
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public void deleteSpreadsheet(String userId) {
		DeleteUsersSheets delete = new DeleteUsersSheets(userId);
		operations.sender(ReceiveOperationArgs.DELETE_USERS_SPREADSHEET,Consts.json.toJson(delete));
		String result = sync.waitForResult(operations.getVersionNumber());
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
		System.out.println("VERSION NUMBER NO GET " + operations.getVersionNumber());
		return resource.getSpreadsheet(sheetId, userId, password);
	}

	@Override
	public String[][] getSpreadsheetValues(String sheetId, String userId, String password) {
		return resource.getSpreadsheetValues(sheetId, userId, password);
	}

	@Override
	public SpreadsheetValuesWrapper importRange(String sheetId, String range, String email) {
		return resource.importRange(sheetId, range, email);
	}

	@Override
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		Update update = new Update(sheetId, cell, rawValue, userId, password);
		operations.sender(ReceiveOperationArgs.UPDATE_SPREADSHEET,Consts.json.toJson(update));
		String result = sync.waitForResult(operations.getVersionNumber());
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public void shareSpreadsheet(String sheetId, String userId, String password) {
		Share share = new Share(sheetId, userId, password);
		operations.sender(ReceiveOperationArgs.SHARE_SPREADSHEET,Consts.json.toJson(share));
		String result = sync.waitForResult(operations.getVersionNumber());
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public void unshareSpreadsheet(String sheetId, String userId, String password) {
		Unshare unshare = new Unshare(sheetId, userId, password);
		operations.sender(ReceiveOperationArgs.UNSHARE_SPREADSHEET,Consts.json.toJson(unshare));
		String result = sync.waitForResult(operations.getVersionNumber());
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}
	
}
