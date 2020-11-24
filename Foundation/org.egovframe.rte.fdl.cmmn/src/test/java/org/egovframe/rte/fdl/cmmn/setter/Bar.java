package org.egovframe.rte.fdl.cmmn.setter;

public class Bar {
	private String message;

	public String setMessage(String message) {
		this.message = message;
		return "return_" + this.message;
	}
	
	
}
