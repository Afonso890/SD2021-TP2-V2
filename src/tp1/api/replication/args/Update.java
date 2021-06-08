package tp1.api.replication.args;

public class Update {

	private String sheetId;
	private String cel;
	private String rawValue;
	public Update(String sheetid, String cell, String rawValue) {
		setSheetId(sheetid);
		setCell(cell);
		setRawValue(rawValue);
	}

	public String getSheetId() {
		return sheetId;
	}

	public void setSheetId(String sheetId) {
		this.sheetId = sheetId;
	}

	public String getCel() {
		return cel;
	}

	public void setCel(String cel) {
		this.cel = cel;
	}

	public String getCell() {
		return cel;
	}
	public void setCell(String cell) {
		this.cel = cell;
	}
	public String getRawValue() {
		return rawValue;
	}
	public void setRawValue(String rawValue) {
		this.rawValue = rawValue;
	}
}
