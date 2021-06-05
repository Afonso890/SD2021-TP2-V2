package tp1.api.replication;

import java.util.Iterator;
import java.util.LinkedList;

import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
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
import tp1.api.servers.resources.SpreadSheetsSharedMethods;
public class KafkaOperationsHandler {
	

	private String topic;
	private SyncPoint sync;
	private long versionNumber;
	KafkaPublisher publisher;

	private long opsSent;
	private Queue<String> missedOperations;
	public KafkaOperationsHandler(String topic,SpreadSheetsSharedMethods resource, SyncPoint sync) {
		// TODO Auto-generated constructor stub
		publisher = KafkaPublisher.createPublisher("localhost:9092, kafka:9092");
		missedOperations=new ConcurrentLinkedQueue<String>(); //nk LinkedList<String>();
		opsSent=-1L;
		this.sync=sync;
		this.topic=topic;
		versionNumber=-1L;
		receiver(resource);
		updateReplica(resource);
		System.out.println("+++++++++++++++++++++++ STARTED REPLICA *********************** ");
	}
	

	private long updateReplicaOnStarting(SpreadSheetsSharedMethods resource) {
		Iterator<Entry<Long,String>> operations = sync.operations();
		long version=0;
		while(operations.hasNext()) {
			System.out.println("CADA CICLO DAS OPERATIONS NO SYNC "+version);
			saveOperation(operations.next().getValue(),resource);
			version++;
		}
		System.out.println("CHEGOU AO FIM DO UPDATE ");
		return version;
	}


	private void receiver(SpreadSheetsSharedMethods resource) {
		List<String> topicLst = new LinkedList<String>();
		topicLst.add(topic);
		KafkaSubscriber subscriber = KafkaSubscriber.createSubscriber("localhost:9092, kafka:9092", topicLst);
		
		subscriber.start( new RecordProcessor() {
			@Override
			public void onReceive(ConsumerRecord<String, String> r) {
				System.out.println("Sequence Number: " + r.topic() + " , " +  r.offset() + " -> ");
				versionNumber=r.offset();
				if(missedOperations!=null) {
					missedOperations.add(r.value());
					sync.setVersionNumber(versionNumber);
				}else {
					ReplicationSyncReturn res=saveOperation(r.value(),resource);
					sync.setResult(versionNumber,Consts.json.toJson(res));
				}
			}
		});
	}
	
	private synchronized void  updateReplica(SpreadSheetsSharedMethods resource) {
		try {
			//System.out.println("GOING TO SLEEP BEFORE UPDATING HAHHAHAHAHAHAA");
			Thread.sleep(1000);
			System.out.println("GOING TO UPDATE OLD OPERATIONS -----> LENGTH "+missedOperations.size());
			while(missedOperations.size()>0) {
				try {
					saveOperation(missedOperations.poll(),resource);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		missedOperations=null;
	}
	public long getVersionNumber() {
		return versionNumber;
	}
	public long operationNumberSentByThisReplica() {
		return opsSent;
	}
	private ReplicationSyncReturn saveOperation(String value,SpreadSheetsSharedMethods resource){
		ReceiveOperationArgs args;
		args =	Consts.json.fromJson(value,ReceiveOperationArgs.class);
		value=args.getArgs();
		ReplicationSyncReturn result=new ReplicationSyncReturn();
		try {
			if(ReceiveOperationArgs.CREATE_SPREADSHEET.equals(args.getOperation())) {
				CreateSpreadSheet cs = Consts.json.fromJson(value,CreateSpreadSheet.class);
				result.setObjResponse(resource.createSpreadsheet(cs.getSheet(),cs.getPassword()));
			}else if(ReceiveOperationArgs.DELETE_SPREADSHEET.equals(args.getOperation())) {
				DeletSpreadsheet cs = Consts.json.fromJson(value,DeletSpreadsheet.class);
				resource.deleteSpreadsheet(cs.getSheetid(),cs.getPassword());
			}else if(ReceiveOperationArgs.DELETE_USERS_SPREADSHEET.equals(args.getOperation())) {
				DeleteUsersSheets cs = Consts.json.fromJson(value,DeleteUsersSheets.class);
				resource.deleteSpreadsheet(cs.getUserId());
			}else if(ReceiveOperationArgs.SHARE_SPREADSHEET.equals(args.getOperation())) {
				Share cs = Consts.json.fromJson(value,Share.class);
				resource.shareSpreadsheet(cs.getSheetId(), cs.getUserId(), cs.getPassword());
			}else if(ReceiveOperationArgs.UNSHARE_SPREADSHEET.equals(args.getOperation())) {
				Unshare cs = Consts.json.fromJson(value,Unshare.class);
				resource.unshareSpreadsheet(cs.getSheetId(), cs.getUserId(), cs.getPassword());
			}else if(ReceiveOperationArgs.UPDATE_SPREADSHEET.equals(args.getOperation())) {
				Update cs = Consts.json.fromJson(value,Update.class);
				resource.updateCell(cs.getSheetId(), cs.getCell(),cs.getRawValue(), cs.getUserId(), cs.getPassword());
			}
			result.setStatus(Status.OK);
			System.out.println("MESSAGE RECEIVED <-----------------------> "+versionNumber+" --> OPERATIONS SENT "+opsSent);
		}catch(WebApplicationException e) {
			result.setStatus(e.getResponse().getStatusInfo().toEnum());
			System.out.println("FAILED FAILED STATUS "+result.getStatus()+" OPERATION ------------------> "+args.getOperation());
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public void sender(String operation, String value) {
		value = Consts.json.toJson(new ReceiveOperationArgs(operation,value));
		long sequenceNumber = publisher.publish(topic,value);
		if(sequenceNumber >= 0) {
			System.out.println("Message published with sequence number: " + sequenceNumber);
			sync.addOperations(versionNumber,value);
			opsSent++;
			opsSent=sequenceNumber;
		}else {
			System.out.println("Failed to publish message");
		}
	}
}
