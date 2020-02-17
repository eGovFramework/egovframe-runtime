package egovframework.rte.bat.core.item.database;

import static org.springframework.util.ClassUtils.getShortName;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.batch.MyBatisPagingItemReader;

import egovframework.rte.bat.support.EgovJobVariableListener;
import egovframework.rte.bat.support.EgovResourceVariable;
import egovframework.rte.bat.support.EgovStepVariableListener;

/**
 * EgovMyBatisPagingItemReader 클래스
 * <Notice>
 * 	    표준프레임워크 베치에서 MyBatisPagingItemReader 클래스를 확장하여 
 *      EgovResourceVariable,EgovJobVariableListener,EgovStepVariableListener 클래스를 이용한 변수 공유 기능 제공  
 *      
 * <Disclaimer>
 *		N/A
 *
 * @author 장동한
 * @since 2017.09.06
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2017.09.06  장동한           최초 생성
 *   2018.01.15  장동한           EgovJobVariable, EgovStepVariable 적용
 * </pre>
 */

public class EgovMyBatisPagingItemReader<T> extends MyBatisPagingItemReader<T>{
	
	private Map<String, Object> map = new HashMap<String, Object>();
	
	/** egovframework EgovResourceVariable */
	private EgovResourceVariable resourceVariable = null;
	
	/** egovframework EgovJobVariableListener */
	private EgovJobVariableListener jobVariable = null;
	
	/** egovframework EgovStepVariableListener */
	private EgovStepVariableListener stepVariable = null;

	public EgovResourceVariable getResourceVariable() {
		return resourceVariable;
	}

	public void setResourceVariable(EgovResourceVariable resourceVariable) {
		this.resourceVariable = resourceVariable;
		processVariable();
	}

	public EgovJobVariableListener getJobVariable() {
		return jobVariable;
	}

	public void setJobVariable(EgovJobVariableListener jobVariable) {
		this.jobVariable = jobVariable;
		processVariable();
	}

	public EgovStepVariableListener getStepVariable() {
		return stepVariable;
	}

	public void setStepVariable(EgovStepVariableListener stepVariable) {
		this.stepVariable = stepVariable;
		processVariable();
	}
	
	public void processVariable() {
		map.clear();
		if(resourceVariable != null)
	    	map.putAll(resourceVariable.getVariableMap());
	    if(jobVariable != null)
	    	map.putAll(jobVariable.getVariableMap());
	    if(stepVariable != null)
	    	map.putAll(stepVariable.getVariableMap());
	    setParameterValues(this.map);
	}

	public EgovMyBatisPagingItemReader() {
		setName(getShortName(EgovMyBatisPagingItemReader.class));
	}
	 
}
