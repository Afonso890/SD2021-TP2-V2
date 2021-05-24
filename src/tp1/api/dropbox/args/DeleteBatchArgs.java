package tp1.api.dropbox.args;

import java.util.List;
import java.util.Map.Entry;

public class DeleteBatchArgs {
	
	final List<Entry<String,String>>  entries;
	
	public DeleteBatchArgs(List<Entry<String,String>>  entries) {
		this.entries = entries;
	}

}
