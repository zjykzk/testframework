package test.util;

public final class Pair<F, S> {
	public Pair(F f, S s) {
		this.f = f;
		this.s = s;
	}
	
	public F fisrt() {
		return f;
	}
	
	public S second() {
		return s;
	}
	
	private F f;
	private S s;
}
