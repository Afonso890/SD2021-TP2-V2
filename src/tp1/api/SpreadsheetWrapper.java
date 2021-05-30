package tp1.api;

public class SpreadsheetWrapper {

	private Spreadsheet sheet;
	//date of the last modification in the server
	private long tw_server;
	public SpreadsheetWrapper(Spreadsheet sp, long tw_server) {
		this.sheet=sp;
		this.tw_server=tw_server;
	}
	public Spreadsheet getSheet() {
		return sheet;
	}
	
	public long getTw_server() {
		return tw_server;
	}
	public void setTw_server(long tw_server) {
		this.tw_server = tw_server;
	}	
}
