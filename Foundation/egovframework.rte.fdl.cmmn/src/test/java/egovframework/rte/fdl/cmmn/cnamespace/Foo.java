package egovframework.rte.fdl.cmmn.cnamespace;

public class Foo {
	private Bar bar;
	private Baz baz;
	private String email;

	public Foo(Bar bar, Baz baz, String email) {
		this.bar = bar;
		this.baz = baz;
		this.email = email;
	}

	public Bar getBar() {
		return bar;
	}
	public void setBar(Bar bar) {
		this.bar = bar;
	}
	public Baz getBaz() {
		return baz;
	}
	public void setBaz(Baz baz) {
		this.baz = baz;
	}
	public String getEmail() {
		return email;
	}
	public String setEmail(String email) {
		this.email = email;
		return "return_" + this.email;
	}

}
