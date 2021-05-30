package tp1.api;

public class SpreadsheetValuesWrapper {

	private String [][] values;
	private long server_tw;
	public String[][] getValues() {
		return values;
	}
	public void setValues(String[][] values) {
		this.values = values;
	}
	public long getServer_tw() {
		return server_tw;
	}
	public void setServer_tw(long server_tw) {
		this.server_tw = server_tw;
	}
	public SpreadsheetValuesWrapper() {
		// TODO Auto-generated constructor stub
	}
	public SpreadsheetValuesWrapper(String [][] values, long server_tw) {
		setValues(values);
		setServer_tw(server_tw);
	}

}
