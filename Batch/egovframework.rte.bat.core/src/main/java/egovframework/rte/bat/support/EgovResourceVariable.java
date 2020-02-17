package egovframework.rte.bat.support;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EgovResourceVariable 클래스
 * <Notice>
 * 	    표준프레임워크 베치에서 singleton 방식으로 배치 Resource 변수 사용
 *      (Map 형태로 Object를 담을 수 있음) 
 * <Disclaimer>
 *		N/A
 *
 * @author 장동한
 * @since 2017.12.01
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2017.12.01  장동한           최초 생성
 * </pre>
 */
public class EgovResourceVariable  {
	 
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovResourceVariable.class);
	
	private Properties pros;
	
	private Map<String, Object> map = null;
	
	public EgovResourceVariable(){
		LOGGER.debug("EgovResourceVariable constructor run. ");
		this.map = new HashMap<String, Object>();
	}
	
	public Properties getPros() {
		synchronized(this.map){
			return pros;
		}
	}

	public void setPros(Properties pros) {
		LOGGER.debug("EgovResourceVariable setPros run. ");
		String key = "";
		synchronized(this.map){
			map.clear();
			this.pros = pros;
			Enumeration<Object> keys = this.pros.keys();
			while (keys.hasMoreElements()) {
				key = (String) keys.nextElement();
				this.map.put(key, pros.getProperty(key));
			}
		}
	}

	public Map<String, Object> getVariableMap() {
		synchronized(this.map){
			return this.map;
		}
	}

	public Object getVariable(String key) {
		synchronized(this.map){
			return this.map.get(key);
		}
	}
	
	public void setVariable(String key, Object value) {
		synchronized(this.map){
			this.map.put(key, value);
		}
	}

	public String getVariableString(String key) {
		synchronized(this.map){
			return (String)this.map.get(key);
		}
	}

	public void setVariableString(String key, String value) {
		synchronized(this.map){
			this.map.put(key, value);
		}
	}
	
	public void setClear() {
		synchronized(this.map){
			this.map = new HashMap<String, Object>();
		}
	}
	
}
