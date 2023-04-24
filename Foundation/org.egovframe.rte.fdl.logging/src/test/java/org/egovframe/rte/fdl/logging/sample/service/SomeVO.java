package org.egovframe.rte.fdl.logging.sample.service;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class SomeVO implements Serializable {

	private static final long serialVersionUID = -1717767349866238571L;

	private String someAttr;

	public String getSomeAttr() {
		return someAttr;
	}

	public void setSomeAttr(String someAttr) {
		this.someAttr = someAttr;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
