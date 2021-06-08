package tp1.api.replication.args;

public class DeletSpreadsheet {
	private String sheetid;
	public String getSheetid() {
		return sheetid;
	}
	public void setSheetid(String sheetid) {
		this.sheetid = sheetid;
	}
	public DeletSpreadsheet(String sheetid) {
		setSheetid(sheetid);
		// TODO Auto-generated constructor stub
	}

}
