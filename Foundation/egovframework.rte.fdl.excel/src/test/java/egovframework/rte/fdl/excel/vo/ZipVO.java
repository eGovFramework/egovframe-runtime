package egovframework.rte.fdl.excel.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class ZipVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private BigDecimal zipNo;

    private BigDecimal serNo;

    private String sidoNm;

    private String cggNm;

    private String umdNm;

    private String bdNm;

    private String jibun;

    private String regId;

	public BigDecimal getZipNo() {
		return zipNo;
	}

	public void setZipNo(BigDecimal zipNo) {
		this.zipNo = zipNo;
	}

	public BigDecimal getSerNo() {
		return serNo;
	}

	public void setSerNo(BigDecimal serNo) {
		this.serNo = serNo;
	}

	public String getSidoNm() {
		return sidoNm;
	}

	public void setSidoNm(String sidoNm) {
		this.sidoNm = sidoNm;
	}

	public String getCggNm() {
		return cggNm;
	}

	public void setCggNm(String cggNm) {
		this.cggNm = cggNm;
	}

	public String getUmdNm() {
		return umdNm;
	}

	public void setUmdNm(String umdNm) {
		this.umdNm = umdNm;
	}

	public String getBdNm() {
		return bdNm;
	}

	public void setBdNm(String bdNm) {
		this.bdNm = bdNm;
	}

	public String getJibun() {
		return jibun;
	}

	public void setJibun(String jibun) {
		this.jibun = jibun;
	}

	public String getRegId() {
		return regId;
	}

	public void setRegId(String regId) {
		this.regId = regId;
	}


}
