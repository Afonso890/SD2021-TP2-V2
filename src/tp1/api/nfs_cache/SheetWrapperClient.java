package tp1.api.nfs_cache;

public class SheetWrapperClient extends SheetWrapperServer {

	private long tc;//the last time the cache entry was checked as valid
	public SheetWrapperClient(String[][] sp, long server_tw,long tc) {
		super(sp,server_tw);
		this.tc=tc;
		// TODO Auto-generated constructor stub
	}
	public long getTc() {
		return tc;
	}
	public void setTc(long tc) {
		this.tc = tc;
	}
	
	public boolean validCacheEntry(long timeInterval) {
		long currentTime = System.currentTimeMillis();
		boolean valid = currentTime-tc<timeInterval;
		//if(valid) {
			//tc=System.currentTimeMillis();
		//}
		return valid;
	}

}
