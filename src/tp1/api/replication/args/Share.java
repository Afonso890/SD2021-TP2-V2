package tp1.api.replication.args;

public class Share {
	private String sheetId, userId, password;
	public String getSheetId() {
		return sheetId;
	}
	public void setSheetId(String sheetId) {
		this.sheetId = sheetId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Share(String sheetId, String userId, String password) {
		setSheetId(sheetId);
		setUserId(userId);
		setPassword(password);
	}

}
