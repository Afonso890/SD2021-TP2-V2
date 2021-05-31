package tp1.api.replication.args;

public class DeletSpreadsheet {
	private String sheetid, password;
	public String getSheetid() {
		return sheetid;
	}
	public void setSheetid(String sheetid) {
		this.sheetid = sheetid;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public DeletSpreadsheet(String password, String sheetid) {
		setSheetid(sheetid);
		setPassword(password);
		// TODO Auto-generated constructor stub
	}

}
