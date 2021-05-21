package tp1.api.dropbox.args;

public class CreateFileArgs {
/*
 * {
    "path": "/Homework/math/Matrices.txt",
    "mode": "add",
    "autorename": true,
    "mute": false,
    "strict_conflict": false
}
 */
	//private static String MODE1="add"; 
	private static String MODE2="overwrite";
	//private static String MODE3="update";
	String path, mode;
	boolean mute, strict_conflict,autorename;
	public CreateFileArgs(String path) {
		this.path=path;
		mode=MODE2;
		autorename=false;
		mute=false;
		strict_conflict=false;
		// TODO Auto-generated constructor stub
	}

}
