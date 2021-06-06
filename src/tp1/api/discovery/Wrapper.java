package tp1.api.discovery;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Wrapper {
	private long lastTime;
	private Set<String> uris;
	public Wrapper(String uriString) {
		uris = new HashSet<String>();
		addUriString(uriString);
		lastTime=System.currentTimeMillis();
	}
	
	public synchronized long getLastTime() {
		return lastTime;
	}
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	
	public synchronized void addUriString(String uri) {
		uris.add(uri);
		lastTime=System.currentTimeMillis();
	}
	public synchronized String [] getUris() {
		String [] rs = null;
		
		if(!uris.isEmpty()) {
			rs = new String[uris.size()];
			Iterator<String> it = uris.iterator();
			int index=0;
			while(it.hasNext()) {
				rs[index]=it.next();
				index++;
			}
		}
		return rs;
	}
	public int numberOfUris() {
		return uris.size();
	}
}
