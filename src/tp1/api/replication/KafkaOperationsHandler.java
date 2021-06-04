package tp1.api.replication;

import java.util.LinkedList;

import java.util.List;

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
import tp1.api.service.rest.RestSpreadsheets;
public class KafkaOperationsHandler {
	

	private String topic;
	private SyncPoint sync;
	private long versionNumber;
	KafkaPublisher publisher;
	private long opsSent;
	public KafkaOperationsHandler(String topic,RestSpreadsheets resource, SyncPoint sync) {
		// TODO Auto-generated constructor stub
		publisher = KafkaPublisher.createPublisher("localhost:9092, kafka:9092");
		opsSent=0;
		this.sync=sync;
		this.topic=topic;
		versionNumber=0;
		receiver(resource);
		System.out.println("+++++++++++++++++++++++ STARTED REPLICA *********************** ");
	}
	
	private void receiver(RestSpreadsheets resource) {
		List<String> topicLst = new LinkedList<String>();
		topicLst.add(topic);
		KafkaSubscriber subscriber = KafkaSubscriber.createSubscriber("localhost:9092, kafka:9092", topicLst);
		
		subscriber.start( new RecordProcessor() {
			@Override
			public void onReceive(ConsumerRecord<String, String> r) {
				//System.out.println( "Sequence Number: " + r.topic() + " , " +  r.offset() + " -> ");
				ReplicationSyncReturn res=saveOperation(r.value(),resource);
				sync.setResult(versionNumber,Consts.json.toJson(res));
			}
		});
		/*
		subscriber.consume( new SubscriberListener() {
			
			public void onReceive(String topic, String key, String value) {
				// TODO Auto-generated method stub
				ReplicationSyncReturn res=saveOperation(value,resource);
				sync.setResult(versionNumber, Consts.json.toJson(res));
			}
		});*/
	}
	public long getVersionNumber() {
		return versionNumber;
	}
	private ReplicationSyncReturn saveOperation(String value,RestSpreadsheets resource){
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
			versionNumber++;
			System.out.println("MESSAGE RECEIVED <-----------------------> "+versionNumber+" --> OPERATIONS SENT "+opsSent);
		}catch(WebApplicationException e) {
			result.setStatus(e.getResponse().getStatusInfo().toEnum());
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
			opsSent++;
		}else {
			System.out.println("Failed to publish message");
		}
	}
}
