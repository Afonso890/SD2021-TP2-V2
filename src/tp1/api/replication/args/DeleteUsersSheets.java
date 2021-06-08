package tp1.api.replication.args;

public class DeleteUsersSheets {

	private String userId;
	private String secrete;
	public DeleteUsersSheets(String userId,String secret) {
		setUserId(userId);
		setSecrete(secret);
	}
	public String getSecrete() {
		return secrete;
	}
	public void setSecrete(String secrete) {
		this.secrete = secrete;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

}
