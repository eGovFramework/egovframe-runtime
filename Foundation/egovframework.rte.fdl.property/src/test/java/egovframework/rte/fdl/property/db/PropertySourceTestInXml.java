package egovframework.rte.fdl.property.db;

//DB기반의 PropertySource의 경우, XML에서 property적용시 사용
public class PropertySourceTestInXml {

	private String sample1;
	private String sample2;

	public void setSample1(String sample1) {
		this.sample1 = sample1;
	}

	public void setSample2(String sample2) {
		this.sample2 = sample2;
	}

	public String getSample2() {
		return sample2;
	}

	public String getSample1() {
		return sample1;
	}
}