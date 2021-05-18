package tp1.api.discovery;

import java.util.HashSet;
import java.util.Set;

public class Wrapper {
	private long lastTime;
	private Set<String> uris;
	//private String uri;

	public Wrapper(String uriString) {
		uris = new HashSet<String>();
		lastTime=System.currentTimeMillis();
		//this.uri=uriString;
	}
	
	public long getLastTime() {
		return lastTime;
	}
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	
	public void addUriString(String uri) {
		uris.add(uri);
		//this.uri=uri;
		lastTime=System.currentTimeMillis();
	}
	
	public String [] getUris() {
		String [] rs = null;
		
		if(!uris.isEmpty()) {
			rs = new String[uris.size()];
			int index=0;
			for (String uri : uris) {
				try {
					rs[index]=uri;
				} catch (Exception e) {
					e.printStackTrace();
				}
				index++;
			}
		}
		return rs;
	}
//	public String [] getUris() {
//		return (String[]) uris.toArray();
//	}
}
