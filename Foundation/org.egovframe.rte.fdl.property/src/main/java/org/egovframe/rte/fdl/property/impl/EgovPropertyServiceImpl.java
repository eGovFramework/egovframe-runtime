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
package org.egovframe.rte.fdl.property.impl;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.configuration2.PropertiesConfiguration.DEFAULT_ENCODING;

/**
 * Property 서비스의 구현 클래스
 * 이 서비스를 통해 어플리케이션에서 유일한 키값으로 키/값쌍을 가지고 오도록 서비스 한다.
 *
 * @author 실행환경 개발팀 김태호
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.02.01	김태호			최초 생성
 * 2014.08.12	Vincent Han		"properties" 속성이 없는 경우 처리
 * 2020.08.31	유지보수			Property 값을 정확히 등록하기 위해 put() 메소드를 addProperty() 메소드로 변경
 * </pre>
 * @since 2009.02.01
 */
public class EgovPropertyServiceImpl implements EgovPropertyService, ApplicationContextAware, InitializingBean, DisposableBean, ResourceLoaderAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovPropertyServiceImpl.class);

    private volatile PropertiesConfiguration egovProperties;
    private ResourceLoader resourceLoader;
    private MessageSource messageSource;
    private Set<?> extFileName;
    private Map<?, ?> properties;

    /**
     * boolean 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @return boolean 타입의 값
     */
    public boolean getBoolean(String name) {
        return getConfiguration().getBoolean(name);
    }

    /**
     * boolean 타입의 프로퍼티 값 얻기(디폴트값을 입력받음)
     *
     * @param name 프로퍼티키
     * @param def  디폴트 값
     * @return boolean 타입의 값
     */
    public boolean getBoolean(String name, boolean def) {
        return getConfiguration().getBoolean(name, def);
    }

    /**
     * double 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @return double 타입의 값
     */
    public double getDouble(String name) {
        return getConfiguration().getDouble(name);
    }

    /**
     * double 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @param def  디폴트 값
     * @return double 타입의 값
     */
    public double getDouble(String name, double def) {
        return getConfiguration().getDouble(name, def);
    }

    /**
     * float 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @return Float 타입의 값
     */
    public float getFloat(String name) {
        return getConfiguration().getFloat(name);
    }

    /**
     * float 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @param def  디폴트 값
     * @return float 타입의 값
     */
    public float getFloat(String name, float def) {
        return getConfiguration().getFloat(name, def);
    }

    /**
     * int 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @return int 타입의 값
     */
    public int getInt(String name) {
        return getConfiguration().getInt(name);
    }

    /**
     * int 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @param def  디폴트 값
     * @return int 타입의 값
     */
    public int getInt(String name, int def) {
        return getConfiguration().getInt(name, def);
    }

    /**
     * 프로퍼티 키 목록 읽기
     *
     * @return Key를 위한 Iterator
     */
    public Iterator<?> getKeys() {
        return getConfiguration().getKeys();
    }

    /**
     * prefix를 이용한 키 목록 읽기
     *
     * @param prefix prefix
     * @return prefix에 매칭되는 키목록
     */
    public Iterator<?> getKeys(String prefix) {
        return getConfiguration().getKeys(prefix);
    }

    /**
     * long 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @return long 타입의 값
     */
    public long getLong(String name) {
        return getConfiguration().getLong(name);
    }

    /**
     * long 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @param def  디폴트 값
     * @return long 타입의 값
     */
    public long getLong(String name, long def) {
        return getConfiguration().getLong(name, def);
    }

    /**
     * String 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @return String 타입의 값
     */
    public String getString(String name) {
        return getConfiguration().getString(name);
    }

    /**
     * String 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @param def  디폴트 값
     * @return String 타입의 값
     */
    public String getString(String name, String def) {
        return getConfiguration().getString(name, def);
    }

    /**
     * String[] 타입의 프로퍼티 값 얻기
     *
     * @param name 프로퍼티키
     * @return String[] 타입의 값
     */
    public String[] getStringArray(String name) {
        return getConfiguration().getStringArray(name);
    }

    /**
     * egovProperties 얻기
     *
     * @return Properties of requested Service.
     */
    private PropertiesConfiguration getConfiguration() {
        return egovProperties;
    }

    /**
     * resource 변경시 refresh (외부 파일 재로드 후 코드로 등록한 properties 재적용)
     */
    public void refreshPropertyFiles() throws IOException, FdlException {
        if (egovProperties == null) {
            return;
        }
        PropertiesConfiguration refreshedProperties = createPropertiesConfiguration();
        loadExternalPropertyFiles(refreshedProperties);
        applyProgrammaticProperties(refreshedProperties);
        egovProperties = refreshedProperties;
    }

    private PropertiesConfiguration createPropertiesConfiguration() {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        return propertiesConfiguration;
    }

    private void loadExternalPropertyFiles(PropertiesConfiguration propertiesConfiguration) throws IOException {
        if (extFileName == null) {
            return;
        }
        Iterator<?> it = extFileName.iterator();
        while (it.hasNext()) {
            Object element = it.next();
            String enc = null;
            String fileName;
            if (element instanceof Map) {
                Map<?, ?> ele = (Map<?, ?>) element;
                enc = (String) ele.get("encoding");
                fileName = (String) ele.get("filename");
            } else {
                fileName = (String) element;
            }
            loadPropertyResources(propertiesConfiguration, fileName, enc);
        }
    }

    @SuppressWarnings("rawtypes")
    private void applyProgrammaticProperties(PropertiesConfiguration propertiesConfiguration) throws FdlException {
        if (properties == null) {
            return;
        }
        Iterator<?> it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key == null || key.isEmpty()) {
                throw new FdlException(messageSource, "error.properties.check.essential", null);
            }
            propertiesConfiguration.addProperty(key, value);
        }
    }

    /**
     * Bean 초기화 함수로 최초 생성시 필요한 Property 세티처리
     */
    public void afterPropertiesSet() throws IOException, FdlException {
        egovProperties = createPropertiesConfiguration();
        refreshPropertyFiles();
    }

    /**
     * extFileName을 지정할 때 Attribute로 정의.
     * <p><b>보안 주의:</b> 각 원소(문자열 또는 {@code filename}/{@code encoding} 키를 가진 Map)의
     * {@code filename} 값은 {@link ResourceLoader}에 그대로 전달되어 {@code classpath:}/{@code file:}/
     * {@code http(s):} 등 스킴에 따라 로컬 파일이나 원격 URL을 읽어올 수 있다. 이 값은 반드시 신뢰할 수
     * 있는 배포 설정(Spring Bean 구성)에서만 지정해야 하며, 사용자 입력 등 신뢰할 수 없는 값을 여기에
     * 바인딩하면 임의 파일 읽기·SSRF로 이어질 수 있다.</p>
     */
    public void setExtFileName(Set<?> extFileName) {
        this.extFileName = extFileName;
    }

    /**
     * properties를 지정할 때 Attribute로 정의
     */
    public void setProperties(Map<?, ?> properties) {
        this.properties = properties;
    }

    /**
     * 서비스 종료처리
     */
    public void destroy() {
        egovProperties = null;
    }

    /**
     * 리소스 로더 세팅
     */
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * ApplicationContext 세팅
     *
     * @param applicationContext to be set by container
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.messageSource = (MessageSource) applicationContext.getBean("messageSource");
    }

    /**
     * 파일위치정보를 가지고 resources 정보 추출
     *
     * @param propertiesConfiguration 프로퍼티를 적재할 대상 설정
     * @param location 파일위치
     * @param encoding Encoding 정보
     */
    private void loadPropertyResources(PropertiesConfiguration propertiesConfiguration, String location, String encoding) throws IOException {
        if (resourceLoader instanceof ResourcePatternResolver) {
            Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
            loadPropertyLoop(propertiesConfiguration, resources, encoding);
        } else {
            Resource resource = resourceLoader.getResource(location);
            loadPropertyRes(propertiesConfiguration, resource, encoding);
        }
    }

    /**
     * 멀티로 지정된 경우 처리를 위해 LOOP 처리
     *
     * @param propertiesConfiguration 프로퍼티를 적재할 대상 설정
     * @param resources 리소스정보
     * @param encoding  인코딩정보
     */
    private void loadPropertyLoop(PropertiesConfiguration propertiesConfiguration, Resource[] resources, String encoding) {
        Assert.notNull(resources, "Resource array must not be null");
        for (int i = 0; i < resources.length; i++) {
            loadPropertyRes(propertiesConfiguration, resources[i], encoding);
        }
    }

    /**
     * 파일 정보를 읽어서 egovProperties에 저장
     *
     * @param propertiesConfiguration 프로퍼티를 적재할 대상 설정
     * @param resource 리소스정보
     * @param encoding 인코딩정보
     */
    private void loadPropertyRes(PropertiesConfiguration propertiesConfiguration, Resource resource, String encoding) {
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        // 2026.02.28 KISA 보안취약점 조치
        try {
            inputStream = resource.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, StringUtils.isEmpty(encoding) ? DEFAULT_ENCODING : encoding);
            propertiesConfiguration.read(inputStreamReader);
        } catch (ConfigurationException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    LOGGER.debug("Failed to close loadPropertyRes : {}", e.getMessage());
                }
            } else if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.debug("Failed to close loadPropertyRes : {}", e.getMessage());
                }
            }
        }
    }

}
