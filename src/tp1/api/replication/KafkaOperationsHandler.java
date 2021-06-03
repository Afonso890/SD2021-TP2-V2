package tp1.api.replication;

import java.util.Iterator;

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
import tp1.api.servers.resources.SpreadSheetResource;
public class KafkaOperationsHandler {
	
	private static final String HELP="HELP";

	private String topic;
	private String topic2;
	private SyncPoint sync;
	private long versionNumber;
	KafkaPublisher publisher;
	public KafkaOperationsHandler(String topic,SpreadSheetResource resource, SyncPoint sync) {
		// TODO Auto-generated constructor stub
		publisher = KafkaPublisher.createPublisher("localhost:9092, kafka:9092");

		this.sync=sync;
		this.topic=topic;
		topic2=topic+"_STARTED";
		versionNumber=0;
		receiver(resource);
		letOthersKnowIamRunning();
	}
	
	private long updateReplicaOnStarting(SpreadSheetResource resource, String ops) {
		long version=0;
		synchronized (resource) {
			@SuppressWarnings("unchecked")
			LinkedList<String> operations=Consts.json.fromJson(ops,LinkedList.class);
			sync.setOperations(operations);
			Iterator<String> it = operations.iterator(); 
			while(it.hasNext()) {
				saveOperation(it.next(),resource);
				version++;
			}
		}
		System.out.println("********************************************************* OPERATIONS INSERTED "+version);
		return version;
	}

	private void receiver(SpreadSheetResource resource) {
		List<String> topicLst = new LinkedList<String>();
		topicLst.add(topic);
		topicLst.add(topic2);

		KafkaSubscriber subscriber = KafkaSubscriber.createSubscriber("localhost:9092, kafka:9092", topicLst);
		subscriber.start( new RecordProcessor() {
			@Override
			public void onReceive(ConsumerRecord<String, String> r) {
				//System.out.println( "Sequence Number: " + r.topic() + " , " +  r.offset() + " -> ");
				if(topic.equals(r.topic())) {
					sync.setResult(versionNumber, Consts.json.toJson(saveOperation(r.value(),resource)));
				}else if(topic2.equals(r.topic())) {
					System.out.println();
					System.out.println(" RECEIVED EVENTS : ");
					System.out.println();
					if(HELP.equals(r.value())) {
						System.out.println(sync.totalOperations()+" ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt");
						if(sync.totalOperations()>Consts.ZERO) {
							letOthersKnowIamRunning();
						}
					}
					else {
						System.out.println("RECEIVED SOMETHING ELSE 0000000000000000000000000000000000000000000000000000000000000000000000000 TOTAL OPERATIONS "+sync.totalOperations());
						if(sync.totalOperations()==Consts.ZERO) {
							System.out.println("44444444444444444444444444444444444444444444444444444444444444444444 going to update!");
							versionNumber = updateReplicaOnStarting(resource,r.value());
						}
					}
				}
				
			}
		});
	}
	public long getVersionNumber() {
		return versionNumber;
	}
	private ReplicationSyncReturn saveOperation(String value,SpreadSheetResource resource){
		ReceiveOperationArgs args =	Consts.json.fromJson(value,ReceiveOperationArgs.class);
		ReplicationSyncReturn result=new ReplicationSyncReturn();
		value=args.getArgs();
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
		}catch(WebApplicationException e) {
			result.setStatus(e.getResponse().getStatusInfo().toEnum());
		}
		return result;
	}
	public void sender(String operation, String value) {
		versionNumber++;
		value = Consts.json.toJson(new ReceiveOperationArgs(operation, value));
		long sequenceNumber = publisher.publish(topic,value);
		sync.addOperations(value);
		if(sequenceNumber >= 0) {
			System.out.println("Message published with sequence number: " + sequenceNumber+" TOTAL NUMBER OF OPERATIONS "+sync.totalOperations());
		}else {
			System.out.println("Failed to publish message");
		}
	}
	public void letOthersKnowIamRunning() {
		String value=HELP;
		LinkedList<String> ops=sync.operations();
		if(ops.size()>0) {
			value=Consts.json.toJson(ops);
		}
		long sequenceNumber = publisher.publish(topic2,value);
		if(sequenceNumber >= 0) {
			System.out.println();
			System.out.println("]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]] WARN SENT: " + sequenceNumber);
			System.out.println();

		}else {
			System.out.println("Failed to publish message");
		}
	}
}
