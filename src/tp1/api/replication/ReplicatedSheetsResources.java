package tp1.api.replication;

import java.util.LinkedList;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.Spreadsheet;
import tp1.api.SpreadsheetValuesWrapper;
import tp1.api.consts.Consts;
import tp1.api.discovery.Discovery;
import tp1.api.replication.args.CreateSpreadSheet;
import tp1.api.replication.args.DeletSpreadsheet;
import tp1.api.replication.args.DeleteUsersSheets;
import tp1.api.replication.args.Share;
import tp1.api.replication.args.Unshare;
import tp1.api.replication.args.Update;
import tp1.api.replication.sync.SyncPoint;
import tp1.api.servers.resources.SpreadSheetsSharedMethods;
import tp1.api.service.rest.RestSpreadsheets;
import tp1.api.storage.StorageInterface;

public class ReplicatedSheetsResources extends SpreadSheetsSharedMethods implements RestSpreadsheets {
	private static final long SLEEP_BEFORE_READING_OLD_OPERATIONS=1000;
	private static final long MINUS_ONE=-1L;

	private static final String KAFKA_HOSTS="localhost:9092, kafka:9092";
	private SyncPoint sync;
	//private KafkaOperationsHandler repManager;
	private KafkaPublisher publisher;
	private long versionNumber;
	private long operationSentOffset;
	
	public ReplicatedSheetsResources(String domainName, Discovery martian, String uri, StorageInterface spreadSheets, SyncPoint sync) {
		super(domainName, martian, uri, spreadSheets);
		//resource = new SpreadSheetsSharedMethods(domainName, martian, uri, spreadSheets);
		publisher = KafkaPublisher.createPublisher(KAFKA_HOSTS);
		//repManager=new KafkaOperationsHandler(domainName,resource,sync);
		this.sync=sync;
		operationSentOffset=MINUS_ONE;
		versionNumber=MINUS_ONE;
		receiver();
		//System.out.println("****************************** REPLICA STARTED ++++++++++++++++++++++++++++++ "+martian.getId());
	}
	
	@Override
	public String createSpreadsheet(Spreadsheet sheet, String password) {
		CreateSpreadSheet create = new CreateSpreadSheet(sheet, password);
		sender(ReceiveOperationArgs.CREATE_SPREADSHEET,Consts.json.toJson(create));
		String result = sync.waitForResult(sentOperationsCounter());
		
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
	
		if(res.getStatus()==Status.OK) {
			return res.getObjResponse();
		}
		throw new WebApplicationException(res.getStatus());
	}

	@Override
	public void deleteSpreadsheet(String sheetId, String password) {
		DeletSpreadsheet delete = new DeletSpreadsheet(password, sheetId);
		sender(ReceiveOperationArgs.DELETE_SPREADSHEET,Consts.json.toJson(delete));
		String result = sync.waitForResult(sentOperationsCounter());
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public void deleteSpreadsheet(String userId) {
		DeleteUsersSheets delete = new DeleteUsersSheets(userId);
		sender(ReceiveOperationArgs.DELETE_USERS_SPREADSHEET,Consts.json.toJson(delete));
		String result = sync.waitForResult(sentOperationsCounter());
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {
		Update update = new Update(sheetId, cell, rawValue, userId, password);
		sender(ReceiveOperationArgs.UPDATE_SPREADSHEET,Consts.json.toJson(update));
		String result = sync.waitForResult(sentOperationsCounter());
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public void shareSpreadsheet(String sheetId, String userId, String password) {
		Share share = new Share(sheetId, userId, password);
		sender(ReceiveOperationArgs.SHARE_SPREADSHEET,Consts.json.toJson(share));
		String result = sync.waitForResult(sentOperationsCounter());
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}

	@Override
	public void unshareSpreadsheet(String sheetId, String userId, String password) {
		Unshare unshare = new Unshare(sheetId, userId, password);
		sender(ReceiveOperationArgs.UNSHARE_SPREADSHEET,Consts.json.toJson(unshare));
		String result = sync.waitForResult(sentOperationsCounter());
		ReplicationSyncReturn res=Consts.json.fromJson(result,ReplicationSyncReturn.class);
		if(res.getStatus()!=Status.OK) {
			throw new WebApplicationException(res.getStatus());
		}
	}
	
	@Override
	public Spreadsheet getSpreadsheet(String sheetId ,String userId,String password) {
		return super.getSpreadsheet(sheetId, userId, password);
	}
		
	@Override
	public String[][] getSpreadsheetValues(String sheetId, String userId,String password){
		return super.getSpreadsheetValues(sheetId, userId, password);
	}
	
	@Override
	public SpreadsheetValuesWrapper importRange(String sheetId,String range,String email) {
		return super.importRange(sheetId, range, email);
	}
	
	private void receiver() {
		try {
			Thread.sleep(SLEEP_BEFORE_READING_OLD_OPERATIONS);
		}catch(Exception e) {
			e.printStackTrace();
		}
		List<String> topicLst = new LinkedList<String>();
		topicLst.add(getDomain());
		KafkaSubscriber subscriber = KafkaSubscriber.createSubscriber("localhost:9092, kafka:9092", topicLst);
		
		subscriber.start( new RecordProcessor() {
			@Override
			public void onReceive(ConsumerRecord<String, String> r) {
				//System.out.println("Sequence Number: " + r.topic() + " , " +  r.offset() + " -> ");
				versionNumber=r.offset();
				ReplicationSyncReturn res=saveOperation(r.value());
				if(versionNumber==operationSentOffset) {
					sync.setResult(versionNumber,Consts.json.toJson(res));	
				}else {
					sync.setVersionNumber(versionNumber);
				}
			}
		});
	}

	private long sentOperationsCounter() {
		return operationSentOffset;
	}
	private ReplicationSyncReturn saveOperation(String value){
		ReceiveOperationArgs args;
		args =	Consts.json.fromJson(value,ReceiveOperationArgs.class);
		value=args.getArgs();
		ReplicationSyncReturn result=new ReplicationSyncReturn();
		try {
			if(ReceiveOperationArgs.CREATE_SPREADSHEET.equals(args.getOperation())) {
				CreateSpreadSheet cs = Consts.json.fromJson(value,CreateSpreadSheet.class);
				result.setObjResponse(super.createSpreadsheet(cs.getSheet(),cs.getPassword()));
			}else if(ReceiveOperationArgs.DELETE_SPREADSHEET.equals(args.getOperation())) {
				DeletSpreadsheet cs = Consts.json.fromJson(value,DeletSpreadsheet.class);
				super.deleteSpreadsheet(cs.getSheetid(),cs.getPassword());
			}else if(ReceiveOperationArgs.DELETE_USERS_SPREADSHEET.equals(args.getOperation())) {
				DeleteUsersSheets cs = Consts.json.fromJson(value,DeleteUsersSheets.class);
				super.deleteSpreadsheet(cs.getUserId());
			}else if(ReceiveOperationArgs.SHARE_SPREADSHEET.equals(args.getOperation())) {
				Share cs = Consts.json.fromJson(value,Share.class);
				super.shareSpreadsheet(cs.getSheetId(), cs.getUserId(), cs.getPassword());
			}else if(ReceiveOperationArgs.UNSHARE_SPREADSHEET.equals(args.getOperation())) {
				Unshare cs = Consts.json.fromJson(value,Unshare.class);
				super.unshareSpreadsheet(cs.getSheetId(), cs.getUserId(), cs.getPassword());
			}else if(ReceiveOperationArgs.UPDATE_SPREADSHEET.equals(args.getOperation())) {
				Update cs = Consts.json.fromJson(value,Update.class);
				super.updateCell(cs.getSheetId(), cs.getCell(),cs.getRawValue(), cs.getUserId(), cs.getPassword());
			}
			result.setStatus(Status.OK);
		}catch(WebApplicationException e) {
			result.setStatus(e.getResponse().getStatusInfo().toEnum());
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	private void sender(String operation, String value) {
		value = Consts.json.toJson(new ReceiveOperationArgs(operation,value));
		long sequenceNumber = publisher.publish(getDomain(),value);
		if(sequenceNumber >= 0) {
			System.out.println("Message published with sequence number: " + sequenceNumber);
			operationSentOffset=sequenceNumber;
		}else {
			System.out.println("Failed to publish message");
		}
	}
	
}
