/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.bat.core.item.database;

import org.egovframe.rte.bat.support.EgovJobVariableListener;
import org.egovframe.rte.bat.support.EgovResourceVariable;
import org.egovframe.rte.bat.support.EgovStepVariableListener;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EgovMyBatisBatchItemWriter 클래스
 * 표준프레임워크 베치에서 MyBatisBatchItemWriter 클래스를 확장하여
 * EgovResourceVariable,EgovJobVariableListener,EgovStepVariableListener 클래스를 이용한 변수 공유 기능 제공
 *
 * @author 장동한
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2017.09.06	장동한				최초 생성
 * 2018.01.15	장동한				EgovJobVariable, EgovStepVariable 적용
 * 2026.10.25	PR #490 반영			Map item 엔트리가 파라미터에서 누락되는 문제 수정
 * </pre>
 * @since 2017.09.06
 */
public class EgovMyBatisBatchItemWriter<T> extends MyBatisBatchItemWriter<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovMyBatisBatchItemWriter.class);

    /**
     * egovframework EgovResourceVariable
     */
    private EgovResourceVariable resourceVariable = null;

    /**
     * egovframework EgovJobVariableListener
     */
    private EgovJobVariableListener jobVariable = null;

    /**
     * egovframework EgovStepVariableListener
     */
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
        try {
            for (T item : items) {
                list.add(mergeSharedVariables(item));
            }
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            ReflectionUtils.handleReflectionException(e);
        }
        super.write((List<? extends T>) list);
    }

    /**
     * item과 공유 변수(resourceVariable/jobVariable/stepVariable)를 병합한 파라미터 Map으로 변환한다.
     * 공유 변수를 먼저 넣고 item 값을 나중에 넣으므로 키가 겹치면 item 값이 우선한다.
     */
    Map<String, Object> mergeSharedVariables(T item)
            throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        Map<String, Object> map = new HashMap<String, Object>();
        if (resourceVariable != null)
            map.putAll(resourceVariable.getVariableMap());
        if (jobVariable != null)
            map.putAll(jobVariable.getVariableMap());
        if (stepVariable != null)
            map.putAll(stepVariable.getVariableMap());

        if (item instanceof Map) {
            // Map item(예: resultType="hashmap")은 BeanInfo 로 class, empty 만 나오므로 엔트리를 그대로 덮어쓴다
            // MyBatis 는 Map 파라미터를 String 이름으로 조회하므로 String 키 엔트리만 병합한다
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet()) {
                if (entry.getKey() instanceof String)
                    map.put((String) entry.getKey(), entry.getValue());
            }
            return map;
        }

        BeanInfo beanInfo = Introspector.getBeanInfo(item.getClass());
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            Method reader = pd.getReadMethod();
            if (reader != null)
                map.put(pd.getName(), reader.invoke(item));
        }
        return map;
    }

}
