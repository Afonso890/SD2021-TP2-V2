package tp1.api.replication.args;

import tp1.api.Spreadsheet;

public class CreateSpreadSheet {

	private Spreadsheet sheet;
	private String password;
	public Spreadsheet getSheet() {
		return sheet;
	}
	public void setSheet(Spreadsheet sheet) {
		this.sheet = sheet;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public CreateSpreadSheet(Spreadsheet sheet, String password) {
		setPassword(password);
		setSheet(sheet);
	}

}
