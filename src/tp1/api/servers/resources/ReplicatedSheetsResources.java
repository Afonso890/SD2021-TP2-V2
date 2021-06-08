package tp1.api.servers.resources;

import java.util.HashSet;
import java.util.LinkedList;


import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.Spreadsheet;
import tp1.api.SpreadsheetValuesWrapper;
import tp1.api.consts.Consts;
import tp1.api.discovery.Discovery;
import tp1.api.replication.KafkaPublisher;
import tp1.api.replication.KafkaSubscriber;
import tp1.api.replication.ReceiveOperationArgs;
import tp1.api.replication.RecordProcessor;
import tp1.api.replication.args.CreateSpreadSheet;
import tp1.api.replication.args.DeletSpreadsheet;
import tp1.api.replication.args.DeleteUsersSheets;
import tp1.api.replication.args.Share;
import tp1.api.replication.args.Unshare;
import tp1.api.replication.args.Update;
import tp1.api.replication.sync.SyncPoint;
import tp1.api.service.rest.RestSpreadsheetsReplication;
import tp1.api.storage.StorageInterface;

public class ReplicatedSheetsResources extends SpreadSheetsSharedMethods implements RestSpreadsheetsReplication {
	private static final long SLEEP_BEFORE_READING_OLD_OPERATIONS=1000;
	private static final long MINUS_ONE=-1L;
	private static final int TRYOUT_TIMES=6;

	private static final String KAFKA_HOSTS="localhost:9092, kafka:9092";
	private SyncPoint sync;
	//private KafkaOperationsHandler repManager;
	private KafkaPublisher publisher;
	private long operationSentOffset;
	private int ids;
	private StorageInterface spreadSheets;

	public ReplicatedSheetsResources(String domainName, Discovery martian, String uri, StorageInterface spreadSheets, SyncPoint sync,String secrete) {
		super(domainName, martian, uri, spreadSheets,secrete);
		this.spreadSheets=spreadSheets;
		//resource = new SpreadSheetsSharedMethods(domainName, martian, uri, spreadSheets);
		publisher = KafkaPublisher.createPublisher(KAFKA_HOSTS);
		//repManager=new KafkaOperationsHandler(domainName,resource,sync);
		this.sync=sync;
		operationSentOffset=MINUS_ONE;
		this.ids=0;
		receiver();
		//System.out.println("****************************** REPLICA STARTED ++++++++++++++++++++++++++++++ "+martian.getId());
	}
	
	@Override
	public String createSpreadsheet(Long version, Spreadsheet sheet, String password) {
		try {
			passwordIsCorrect(sheet.getOwner(),password,Status.BAD_REQUEST);
			validSheet(sheet);
		}catch(Exception e) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		CreateSpreadSheet create = new CreateSpreadSheet(sheet, password);
		sender(ReceiveOperationArgs.CREATE_SPREADSHEET,Consts.json.toJson(create));
		String res = sync.waitForResult(versionToWaitFor(version));
		return res;
	}

	@Override
	public void deleteSpreadsheet(Long version, String sheetId, String password) {
		synchronized (spreadSheets) {
			Spreadsheet sp = hasSpreadSheet(sheetId);
			passwordIsCorrect(sp.getOwner(),password,Status.FORBIDDEN);
		}
		
		DeletSpreadsheet delete = new DeletSpreadsheet(sheetId);
		sender(ReceiveOperationArgs.DELETE_SPREADSHEET,Consts.json.toJson(delete));
		sync.waitForVersion(versionToWaitFor(version));
	}

	@Override
	public void deleteSpreadsheetOfThisUser(Long version, String userId,String secrete) {
		DeleteUsersSheets delete = new DeleteUsersSheets(userId,secrete);
		sender(ReceiveOperationArgs.DELETE_USERS_SPREADSHEET,Consts.json.toJson(delete));
		sync.waitForVersion(versionToWaitFor(version));
	}

	@Override
	public void updateCell(Long version, String sheetId, String cell, String rawValue, String userId, String password) {
		try{
			System.out.println("GOING TO SEND AN UPDATE TO KAFKA");
			Spreadsheet sp;
			synchronized (spreadSheets) {
				sp=hasSpreadSheet(sheetId);
				if(!hasAccess(sp,userId)) {
					throw new WebApplicationException(Status.FORBIDDEN);
				}	
			}
			passwordIsCorrect(userId,password,Status.FORBIDDEN);
			Update update = new Update(sp.getSheetId(),cell, rawValue);
			sender(ReceiveOperationArgs.UPDATE_SPREADSHEET,Consts.json.toJson(update));
			System.out.println("MESSAGE SENT TO KAFKA");
			sync.waitForVersion(versionToWaitFor(version));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void shareSpreadsheet(Long version, String sheetId, String userId, String password) {
		userExists(userId);
		Spreadsheet sp=null;
		synchronized (spreadSheets) {
			sp=hasSpreadSheet(sheetId);
			passwordIsCorrect(sp.getOwner(),password,Status.FORBIDDEN);			
			if(sp.getSharedWith()==null) {
				sp.setSharedWith(new HashSet<String>());
			}
			if(sp.getSharedWith().contains(userId)) {
				throw new WebApplicationException( Status.CONFLICT );
			}
		}
		
		Share share = new Share(sp.getSheetId(),userId);
		sender(ReceiveOperationArgs.SHARE_SPREADSHEET,Consts.json.toJson(share));
		sync.waitForVersion(versionToWaitFor(version));
	}

	@Override
	public void unshareSpreadsheet(Long version, String sheetId, String userId, String password) {
		userId = userExists(userId);
		Spreadsheet sp;
		synchronized (spreadSheets) {
			sp=hasSpreadSheet(sheetId);
			passwordIsCorrect(sp.getOwner(),password,Status.FORBIDDEN);
			if(!sp.getSharedWith().contains(userId+"@"+getDomain())) {
				throw new WebApplicationException( Status.NOT_FOUND );
			}
		}
		Unshare unshare = new Unshare(sheetId,userId+"@"+getDomain());
		sender(ReceiveOperationArgs.UNSHARE_SPREADSHEET,Consts.json.toJson(unshare));
		sync.waitForVersion(versionToWaitFor(version));
	}
	
	@Override
	public Spreadsheet getSpreadsheet(Long version, String sheetId,String userId,String password) {
		if(version==null) {
			version=sync.getVersionNumber();
		}
		sync.waitForVersion(version);
		return super.getSpreadsheet(sheetId,userId, password);
	}
		
	@Override
	public String[][] getSpreadsheetValues(Long version, String sheetId, String userId,String password){
		if(version==null) {
			version=sync.getVersionNumber();
		}
		sync.waitForVersion(version);
		return super.getSpreadsheetValues(sheetId, userId, password);
	}
	
	@Override
	public SpreadsheetValuesWrapper importRange(Long version, String sheetId,String range,String email,String secret) {
		if(version==null) {
			version=sync.getVersionNumber();
		}
		sync.waitForVersion(version);
		return super.importRange(sheetId, range, email,secret);
	}
	
	private void receiver() {
		try {
			Discovery d = getDiscovery();
			int tries=0;
			while(!d.hasThisDomain(getUsersDomain())&&tries<TRYOUT_TIMES) {
				Thread.sleep(SLEEP_BEFORE_READING_OLD_OPERATIONS);
				tries++;
			}
			if(tries==TRYOUT_TIMES) {
				System.err.println("ERROR: COULD NOT CONTACT THE USERS' DOMAIN! \n");
				System.exit(1);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		List<String> topicLst = new LinkedList<String>();
		topicLst.add(getDomain());
		//"localhost:9092, kafka:9092"
		KafkaSubscriber subscriber = KafkaSubscriber.createSubscriber(KAFKA_HOSTS,topicLst);
		
		subscriber.start( new RecordProcessor() {
			@Override
			public void onReceive(ConsumerRecord<String, String> r) {
				//System.out.println("Sequence Number: " + r.topic() + " , " +  r.offset() + " -> ");
				String res=saveOperation(r.value());
				sync.setResult(r.offset(),res);	
			}
		});
	}
	private long versionToWaitFor(Long clientVersion) {
		if(clientVersion==null) {
			return operationSentOffset;
		}else {
			return Math.max(operationSentOffset, clientVersion);
		}
	}
	private String saveOperation(String value){
		ReceiveOperationArgs args;
		args =	Consts.json.fromJson(value,ReceiveOperationArgs.class);
		value=args.getArgs();
		String result = null;
		try {
			if(ReceiveOperationArgs.CREATE_SPREADSHEET.equals(args.getOperation())) {
				CreateSpreadSheet cs = Consts.json.fromJson(value,CreateSpreadSheet.class);
				synchronized (spreadSheets) {
					ids++;
					Spreadsheet sheet = cs.getSheet();
					//UUID.randomUUID().toString();
					String sheetId = ids+"$"+sheet.getOwner();
					sheet.setSheetId(sheetId);
					sheet.setSheetURL(getUri()+"_"+sheet.getSheetId());
					spreadSheets.put(sheet.getSheetId(),sheet);
					result=sheetId;
				}
			}else if(ReceiveOperationArgs.DELETE_SPREADSHEET.equals(args.getOperation())) {
				DeletSpreadsheet cs = Consts.json.fromJson(value,DeletSpreadsheet.class);
				synchronized (spreadSheets) {
					spreadSheets.remove(cs.getSheetid());
				}
			}else if(ReceiveOperationArgs.DELETE_USERS_SPREADSHEET.equals(args.getOperation())) {
				DeleteUsersSheets cs = Consts.json.fromJson(value,DeleteUsersSheets.class);
				super.deleteSpreadsheetOfThisUSer(cs.getUserId(),cs.getSecrete());
			}else if(ReceiveOperationArgs.SHARE_SPREADSHEET.equals(args.getOperation())) {
				Share cs = Consts.json.fromJson(value,Share.class);
				synchronized (spreadSheets) {
					spreadSheets.share(spreadSheets.get(cs.getSheetid()),cs.getUserId());
				}
			}else if(ReceiveOperationArgs.UNSHARE_SPREADSHEET.equals(args.getOperation())) {
				Unshare cs = Consts.json.fromJson(value,Unshare.class);
				synchronized (spreadSheets) {
					spreadSheets.unShare(spreadSheets.get(cs.getSheetId()),cs.getUserId());
				}
			}else if(ReceiveOperationArgs.UPDATE_SPREADSHEET.equals(args.getOperation())) {
				System.out.println("GOING TO ADD UPDATE");
				Update cs = Consts.json.fromJson(value,Update.class);
				synchronized (spreadSheets) {
					Spreadsheet sp = spreadSheets.get(cs.getSheetId());
					spreadSheets.updateCell(sp,cs.getCell(),cs.getRawValue());
					//spreadSheets.put(cs.getSheet().getSheetId(),cs.getSheet());
				}			
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	private void sender(String operation, String value) {
		ReceiveOperationArgs args = new ReceiveOperationArgs(operation, value);
		long sequenceNumber = publisher.publish(getDomain(),Consts.json.toJson(args));
		if(sequenceNumber >= 0) {
			System.out.println("Message published with sequence number: " + sequenceNumber);
			operationSentOffset=sequenceNumber;
		}else {
			System.out.println("Failed to publish message");
		}
	}	
}
