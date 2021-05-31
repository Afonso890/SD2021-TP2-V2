package tp1.api.replication.args;

public class DeleteUsersSheets {

	private String userId;
	public DeleteUsersSheets(String userId) {
		setUserId(userId);
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

}
