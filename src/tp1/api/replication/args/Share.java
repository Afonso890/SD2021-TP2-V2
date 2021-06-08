package tp1.api.replication.args;

public class Share {
	private String sheetid;
	private String userId;

	public Share(String sheet, String userId) {
		setSheetid(sheet);
		setUserId(userId);
	}

	public String getSheetid() {
		return sheetid;
	}

	public void setSheetid(String sheetid) {
		this.sheetid = sheetid;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
