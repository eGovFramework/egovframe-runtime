package egovframework.rte.bat.support;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;

/**
 * EgovStepVariableListener 클래스
 * <Notice>
 * 	    표준프레임워크 베치에서 Step Listener 환경에서 Step Scope 범위에서 변수 사용 
 *      (stepExecutionContext에 데이터 저장)
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
 *   2018.01.15  장동한           getVariableMap, getVariableString 적용
 * </pre>
 */
public class EgovStepVariableListener implements StepExecutionListener {
	 
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovStepVariableListener.class);
	
	private Map<String, Object> map = new HashMap<String, Object>();
	
	private Properties pros;
	 
	public Properties getPros() {
		return pros;
	}

	public void setPros(Properties pros) {
		this.pros = pros;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		LOGGER.debug("EgovStepVariableListener afterJob run. ");
		Enumeration<Object> keys = this.pros.keys();
		ExecutionContext executionContext = stepExecution.getExecutionContext();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			executionContext.put(key, pros.getProperty(key));
		}
		
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		LOGGER.debug("EgovStepVariableListener afterStep run. ");
		return null;
	}
	
	public String getVariableString(String key) {
		return pros.getProperty(key);
	}

	public Map<String, Object> getVariableMap() {
		map.clear();
		Enumeration<?> propertyNames = pros.propertyNames();
		String key = "";
		while (propertyNames.hasMoreElements()) {
			key = (String)propertyNames.nextElement();
			map.put(key, pros.getProperty(key));
		}
		return map;
	}
}
