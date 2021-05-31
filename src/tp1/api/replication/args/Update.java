package tp1.api.replication.args;

public class Update {

	private String sheetId;
	private String cell;
	private String rawValue;
	private String userId;
	private String password;
	public Update(String sheetId, String cell, String rawValue, String userId, String password) {
		setSheetId(sheetId);
		setCell(cell);
		setRawValue(rawValue);
		setUserId(userId);
		setPassword(password);
	}
	public String getSheetId() {
		return sheetId;
	}
	public void setSheetId(String sheetId) {
		this.sheetId = sheetId;
	}
	public String getCell() {
		return cell;
	}
	public void setCell(String cell) {
		this.cell = cell;
	}
	public String getRawValue() {
		return rawValue;
	}
	public void setRawValue(String rawValue) {
		this.rawValue = rawValue;
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

}
