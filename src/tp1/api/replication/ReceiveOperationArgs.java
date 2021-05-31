package tp1.api.replication;

public class ReceiveOperationArgs {

	public static final String CREATE_SPREADSHEET="CREATE";
	public static final String DELETE_SPREADSHEET="DELETE";
	public static final String SHARE_SPREADSHEET="SHARE";
	public static final String UNSHARE_SPREADSHEET="UNSHARE";
	public static final String UPDATE_SPREADSHEET="UPDATE";
	public static final String DELETE_USERS_SPREADSHEET="DELETE_SHEETS";
	
	private String operation;
	private String args;


	public ReceiveOperationArgs(String operation, String args) {
		setOperation(operation);
		setArgs(args);
	}


	public String getOperation() {
		return operation;
	}


	public void setOperation(String operation) {
		this.operation = operation;
	}


	public String getArgs() {
		return args;
	}


	public void setArgs(String args) {
		this.args = args;
	}

}
