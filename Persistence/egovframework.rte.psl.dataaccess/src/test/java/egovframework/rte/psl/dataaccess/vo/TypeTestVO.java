package egovframework.rte.psl.dataaccess.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class TypeTestVO implements Serializable {

	private static final long serialVersionUID = -3653247402772333834L;

	private int id;

	private BigDecimal bigdecimalType;

	private boolean booleanType;

	private byte byteType;

	private String charType;

	private double doubleType;

	private float floatType;

	private int intType;

	private long longType;

	private short shortType;

	private String stringType;

	private Date dateType;

	private java.sql.Date sqlDateType;

	private Time sqlTimeType;

	private Timestamp sqlTimestampType;

	private Calendar calendarType;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public BigDecimal getBigdecimalType() {
		return bigdecimalType;
	}

	public void setBigdecimalType(BigDecimal bigdecimalType) {
		this.bigdecimalType = bigdecimalType;
	}

	public boolean isBooleanType() {
		return booleanType;
	}

	public void setBooleanType(boolean booleanType) {
		this.booleanType = booleanType;
	}

	public byte getByteType() {
		return byteType;
	}

	public void setByteType(byte byteType) {
		this.byteType = byteType;
	}

	public String getCharType() {
		return charType;
	}

	public void setCharType(String charType) {
		this.charType = charType;
	}

	public double getDoubleType() {
		return doubleType;
	}

	public void setDoubleType(double doubleType) {
		this.doubleType = doubleType;
	}

	public float getFloatType() {
		return floatType;
	}

	public void setFloatType(float floatType) {
		this.floatType = floatType;
	}

	public int getIntType() {
		return intType;
	}

	public void setIntType(int intType) {
		this.intType = intType;
	}

	public long getLongType() {
		return longType;
	}

	public void setLongType(long longType) {
		this.longType = longType;
	}

	public short getShortType() {
		return shortType;
	}

	public void setShortType(short shortType) {
		this.shortType = shortType;
	}

	public String getStringType() {
		return stringType;
	}

	public void setStringType(String stringType) {
		this.stringType = stringType;
	}

	public Date getDateType() {
		return dateType;
	}

	public void setDateType(Date dateType) {
		this.dateType = dateType;
	}

	public java.sql.Date getSqlDateType() {
		return sqlDateType;
	}

	public void setSqlDateType(java.sql.Date sqlDateType) {
		this.sqlDateType = sqlDateType;
	}

	public Time getSqlTimeType() {
		return sqlTimeType;
	}

	public void setSqlTimeType(Time sqlTimeType) {
		this.sqlTimeType = sqlTimeType;
	}

	public Timestamp getSqlTimestampType() {
		return sqlTimestampType;
	}

	public void setSqlTimestampType(Timestamp sqlTimestampType) {
		this.sqlTimestampType = sqlTimestampType;
	}

	public Calendar getCalendarType() {
		return calendarType;
	}

	public void setCalendarType(Calendar calendarType) {
		this.calendarType = calendarType;
	}

}
