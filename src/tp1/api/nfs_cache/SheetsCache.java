package tp1.api.nfs_cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tp1.util.CellRange;


public class SheetsCache {

	//20 seconds
	private static final long interval =20*1000;
	
	private Map<String,SheetWrapperClient> cache;

	public SheetsCache() {
		cache=new ConcurrentHashMap<String, SheetWrapperClient>();
	}
	
	public String [][] getValues(String sheetId,String range) {
		SheetWrapperClient sp = cache.get(sheetId);
		if(sp!=null&&sp.validCacheEntry(interval)) {
			return extractValues(sp.getValues(),range);
		}
		return null;
	}
	public String [][] getValuesInFailure(String sheetId,String range) {
		SheetWrapperClient sp = cache.get(sheetId);
		if(sp!=null) {
			return extractValues(sp.getValues(),range);
		}
		return null;
	}
	
	public String [][] extractValues(String [][] values,String range){
		CellRange r = new CellRange( range );
		return r.extractRangeValuesFrom(values);
	}
	public void addValues(String [][] values, String sheetid, long server_tw) {
		SheetWrapperClient sp = cache.get(sheetid);
		long time = System.currentTimeMillis();
		if(sp!=null) {
			if(sp.getServer_tw()!=server_tw) {
				sp.setValues(values);
				sp.setServer_tw(server_tw);
			}else {
				sp.setTc(time);
			}
		}else {
			sp=new SheetWrapperClient(values,server_tw,time);
			cache.put(sheetid,sp);
		}
	}

}
