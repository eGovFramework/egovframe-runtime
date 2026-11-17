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
 * 2026.11.14	유지보수			refreshPropertyFiles()가 새 설정 객체에 다시 읽은 뒤 바꿔 끼우도록 수정(바뀐 값 반영, 값 누적 방지), 읽은 파일 스트림 닫기
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
     * resource 변경시 refresh
     * <p>새 설정 객체에 외부파일과 properties를 모두 읽은 뒤 바꿔 끼운다.
     * 읽는 중에 실패하면 예외를 던지고 기존 값을 그대로 둔다.</p>
     */
    public void refreshPropertyFiles() throws IOException {
        if (egovProperties == null) {
            return;
        }
        PropertiesConfiguration refreshedProperties;
        try {
            refreshedProperties = createConfiguration();
        } catch (FdlException e) {
            // properties의 키는 afterPropertiesSet()에서 이미 검사했다
            throw new IllegalStateException(e);
        }
        egovProperties = refreshedProperties;
    }

    /**
     * Bean 초기화 함수로 최초 생성시 필요한 Property 세티처리
     *
     * @throws Exception fail to initialize
     */
    public void afterPropertiesSet() throws IOException, FdlException {
        egovProperties = createConfiguration();
    }

    /**
     * 외부파일과 properties를 읽은 새 설정 객체 생성
     *
     * @return 새 설정 객체
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private PropertiesConfiguration createConfiguration() throws IOException, FdlException {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();

        // 외부파일이 정의되었을때
        if (extFileName != null) {
            String fileName = null;
            Iterator<?> it = extFileName.iterator();
            while (it.hasNext()) {
                Object element = it.next();
                String enc = null;
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

        if (properties != null) {
            Iterator<?> it = properties.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (key == null || key.equals("")) {
                    throw new FdlException(messageSource, "error.properties.check.essential", null);
                }
                // 2020.08.31 ESFC Property 값을 정확히 등록하기 위해 put() 메소드를 addProperty() 메소드로 변경
                propertiesConfiguration.addProperty(key, value);
            }
        }
        return propertiesConfiguration;
    }

    /**
     * extFileName을 지정할 때 Attribute로 정의
     *
     * @param extFileName
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
     * @param propertiesConfiguration 읽은 값을 담을 설정 객체
     * @param location 파일위치
     * @param encoding Encoding 정보
     * @throws IOException
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
     * @param propertiesConfiguration 읽은 값을 담을 설정 객체
     * @param resources 리소스정보
     * @param encoding  인코딩정보
     * @throws IOException
     */
    private void loadPropertyLoop(PropertiesConfiguration propertiesConfiguration, Resource[] resources, String encoding) {
        Assert.notNull(resources, "Resource array must not be null");
        for (int i = 0; i < resources.length; i++) {
            loadPropertyRes(propertiesConfiguration, resources[i], encoding);
        }
    }

    /**
     * 파일 정보를 읽어서 설정 객체에 저장
     *
     * @param propertiesConfiguration 읽은 값을 담을 설정 객체
     * @param resource 리소스정보
     * @param encoding 인코딩정보
     * @throws IOException
     */
    private void loadPropertyRes(PropertiesConfiguration propertiesConfiguration, Resource resource, String encoding) {

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        try {
            inputStream = resource.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, StringUtils.isEmpty(encoding) ? DEFAULT_ENCODING : encoding);
            propertiesConfiguration.read(inputStreamReader);
        } catch (ConfigurationException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            // refreshPropertyFiles()를 부를 때마다 파일을 다시 열므로 닫는다
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
