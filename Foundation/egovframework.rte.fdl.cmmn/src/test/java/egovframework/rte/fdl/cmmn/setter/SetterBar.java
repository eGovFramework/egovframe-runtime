package egovframework.rte.fdl.cmmn.setter;

public class SetterBar {
	private String message;

	public String setMessage(String message) {
		this.message = message;
		return "return_" + this.message;
	}
	
	
}
