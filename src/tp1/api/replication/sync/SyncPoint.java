package tp1.api.replication.sync;

import java.util.HashMap;


import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import tp1.api.replication.VersionNumber;

public class SyncPoint implements VersionNumber
{
	private static SyncPoint instance;
	private static long CLEAN_PERIOD=8000;
	public static synchronized SyncPoint getInstance() {
		if( instance == null) {
			instance = new SyncPoint();
		}
		
		return instance;
	}

	private Map<Long,String> result;
	private long version;
	
	
	private SyncPoint() {
		result = new HashMap<Long,String>();
		version=-1L;
		removeDoCleabUp();
	}
	
	
	/**
	 * Waits for version to be at least equals to n
	 */
	public synchronized void waitForVersion( long n) {
		while( version<n) {
			try {
				wait();
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		result.remove(n);
	}
	/**
	 * Assuming that results are added sequentially, returns null if the result is not available.
	 */
	public synchronized String waitForResult( long n) {
		while( version < n) {
			try {
				wait();
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		return result.remove(n);
	}

	/**
	 * Updates the version and stores the associated result
	 */
	public synchronized void setResult( long n, String res) {
		if( res != null)
			result.put(n,res);
		version = n;
		notifyAll();
	}
	
	public synchronized void setVersionNumber(long number) {
		version=number;
	}

	public synchronized long getVersionNumber() {
		return version;
	}
	/**
	 * Cleans up results that will not be consumed
	 */
	public synchronized void cleanupUntil( long n) {
		Iterator<Entry<Long,String>> it = result.entrySet().iterator();
		while( it.hasNext()) {
			if( it.next().getKey() < n)
				it.remove();
		}
	}

	private void removeDoCleabUp() {
		new Thread(()->{
			while(true) {
				try {
					Thread.sleep(CLEAN_PERIOD);
					cleanupUntil(version);
				}catch(Exception e) {
					
				}
			}
		}).start();
	}
}

