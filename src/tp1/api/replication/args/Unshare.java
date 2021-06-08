package tp1.api.replication.args;

public class Unshare {

	public String getSheetId() {
		return sheetId;
	}

	public void setSheetId(String sheetId) {
		this.sheetId = sheetId;
	}

	private String sheetId;
	private String userId;
	
	public Unshare(String sheetId, String userId) {
		setUserId(userId);
		setSheetId(sheetId);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
