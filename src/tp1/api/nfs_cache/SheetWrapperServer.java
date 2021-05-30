package tp1.api.nfs_cache;

public class SheetWrapperServer {

	private String [][] values;
	private long server_tw;
	public SheetWrapperServer(String [][] sp, long server_tw) {
		this.values=sp;
		setServer_tw(server_tw);
	}
	public void setValues(String[][] values) {
		this.values = values;
	}
	public String [][] getValues() {
		return values;
	}
	public long getServer_tw() {
		return server_tw;
	}
	public void setServer_tw(long server_tw) {
		this.server_tw = server_tw;
	}

}
