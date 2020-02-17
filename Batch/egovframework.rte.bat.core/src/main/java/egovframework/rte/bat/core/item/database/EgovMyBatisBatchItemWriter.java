package egovframework.rte.bat.core.item.database;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.mybatis.spring.batch.MyBatisBatchItemWriter;

import egovframework.rte.bat.support.EgovResourceVariable;
import egovframework.rte.bat.support.EgovJobVariableListener;
import egovframework.rte.bat.support.EgovStepVariableListener;

/**
 * EgovMyBatisBatchItemWriter 클래스
 * <Notice>
 * 	    표준프레임워크 베치에서 MyBatisBatchItemWriter 클래스를 확장하여 
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
public class EgovMyBatisBatchItemWriter<T> extends MyBatisBatchItemWriter<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovMyBatisBatchItemWriter.class);
	
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
	}

	public EgovJobVariableListener getJobVariable() {
		return jobVariable;
	}

	public void setJobVariable(EgovJobVariableListener jobVariable) {
		this.jobVariable = jobVariable;
	}

	public EgovStepVariableListener getStepVariable() {
		return stepVariable;
	}

	public void setStepVariable(EgovStepVariableListener stepVariable) {
		this.stepVariable = stepVariable;
	}


	@Override
	@SuppressWarnings("unchecked")
	public void write(final List<? extends T> items) {
		List<Object> list = new ArrayList<Object>();
		Map<String, Object> map = null;
		try{
			for(T item : items) {
				map = new HashMap<String, Object>();
			    BeanInfo beanInfo = Introspector.getBeanInfo(item.getClass());
			    if(resourceVariable != null)
			    	map.putAll(resourceVariable.getVariableMap());
			    if(jobVariable != null)
			    	map.putAll(jobVariable.getVariableMap());
			    if(stepVariable != null)
			    	map.putAll(stepVariable.getVariableMap());
			    for(PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
			        Method reader = pd.getReadMethod();
			        if(reader != null)
			            map.put(pd.getName(),reader.invoke(item));
			    }
			    list.add(map);
			}			
		}catch(IllegalArgumentException e) {
			e.printStackTrace();
			LOGGER.error("[" + e.getClass() +"] executing items convert error : " + e.getMessage());
		}catch(Exception e){
			LOGGER.error("[" + e.getClass() +"] executing items convert error : " + e.getMessage());
		}
		super.write( (List<? extends T>)list );
	}

}
