package tp1.util;

public class Pair<V1, V2> {

	private V1 value1;
	private V2 value2;
	public Pair() {
		// TODO Auto-generated constructor stub
	}
	public Pair(V1 v1, V2 v2) {
		setValue1(v1);
		setValue2(v2);
	}
	public V1 getValue1() {
		return value1;
	}
	public void setValue1(V1 value1) {
		this.value1 = value1;
	}
	public V2 getValue2() {
		return value2;
	}
	public void setValue2(V2 value2) {
		this.value2 = value2;
	}
	

}
