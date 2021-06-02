package tp1.api.replication;

import jakarta.ws.rs.core.Response.Status;

public class ReplicationSyncReturn {

	Status status;
	String objResponse;
	public ReplicationSyncReturn() {
		// TODO Auto-generated constructor stub
	}
	public ReplicationSyncReturn(Status status, String objResponse) {
		// TODO Auto-generated constructor stub
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getObjResponse() {
		return objResponse;
	}
	public void setObjResponse(String objResponse) {
		this.objResponse = objResponse;
	}

}
