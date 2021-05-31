package tp1.api.replication;

import java.util.LinkedList;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import jakarta.ws.rs.WebApplicationException;
import tp1.api.consts.Consts;
import tp1.api.replication.args.CreateSpreadSheet;
import tp1.api.replication.args.DeletSpreadsheet;
import tp1.api.replication.args.DeleteUsersSheets;
import tp1.api.replication.args.Share;
import tp1.api.replication.args.Unshare;
import tp1.api.replication.args.Update;
import tp1.api.replication.sync.SyncPoint;
import tp1.api.servers.resources.SpreadSheetResource;
import tp1.util.Pair;

public class KafkaOperationsHandler {

	private String topic;
	private SyncPoint sync;
	private long versionNumber;
	public KafkaOperationsHandler(String topic,SpreadSheetResource resource, SyncPoint sync) {
		// TODO Auto-generated constructor stub
		this.sync=sync;
		this.topic=topic;
		this.versionNumber=0;
		receiver(resource);
	}

	private void receiver(SpreadSheetResource resource) {
		List<String> topicLst = new LinkedList<String>();
		topicLst.add(topic);
		KafkaSubscriber subscriber = KafkaSubscriber.createSubscriber("localhost:9092, kafka:9092", topicLst);
		subscriber.start( new RecordProcessor() {
			@Override
			public void onReceive(ConsumerRecord<String, String> r) {
				System.out.println( "Sequence Number: " + r.topic() + 
						" , " +  r.offset() + " -> " + r.value());
				saveOperation(r.value(),resource);
			}
		});
	}
	public long getVersionNumber() {
		return versionNumber;
	}
	private void saveOperation(String value,SpreadSheetResource resource){
		ReceiveOperationArgs args =	Consts.json.fromJson(value,ReceiveOperationArgs.class);
		String result="";
		Pair<jakarta.ws.rs.core.Response.Status,String> res=new Pair<jakarta.ws.rs.core.Response.Status,String>();

		if(ReceiveOperationArgs.CREATE_SPREADSHEET.equals(args.getOperation())) {
			CreateSpreadSheet cs = Consts.json.fromJson(value,CreateSpreadSheet.class);
			try {
				result = resource.createSpreadsheet(cs.getSheet(),cs.getPassword());
				res.setValue1(jakarta.ws.rs.core.Response.Status.OK);
				res.setValue2(result);
			}catch(WebApplicationException e) {
				result=e.getResponse().getStatus()+"";
			}			
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
			resource.updateCell(cs.getSheetId(), cs.getCell(), cs.getRawValue(), cs.getUserId(), cs.getPassword());
		}
		sync.setResult(versionNumber, Consts.json.toJson(res));
	}
	public void sender(String operation, String value) {
		versionNumber++;
		KafkaPublisher publisher = KafkaPublisher.createPublisher("localhost:9092, kafka:9092");
		value = Consts.json.toJson(new ReceiveOperationArgs(operation, value));
		long sequenceNumber = publisher.publish(topic,value);
		if(sequenceNumber >= 0)
			System.out.println("Message published with sequence number: " + sequenceNumber);
		else
			System.out.println("Failed to publish message");
	}
}
