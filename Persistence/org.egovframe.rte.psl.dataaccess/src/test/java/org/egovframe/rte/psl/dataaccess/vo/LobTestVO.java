package org.egovframe.rte.psl.dataaccess.vo;

import java.io.Serializable;

public class LobTestVO implements Serializable {

	private static final long serialVersionUID = -5563510918316251092L;

	private int id;

	private byte[] blobType;

	private String clobType;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte[] getBlobType() {
		return blobType;
	}

	public void setBlobType(byte[] blobType) {
		this.blobType = blobType;
	}

	public String getClobType() {
		return clobType;
	}

	public void setClobType(String clobType) {
		this.clobType = clobType;
	}

}
