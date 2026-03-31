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
package org.egovframe.rte.fdl.crypto.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * egov-crypto properties 설정 파일을 읽는 유틸리티 클래스
 *
 * <p>Desc.: properties 설정 파일을 읽어 EgovCryptoConfig 객체로 변환하는 클래스</p>
 *
 * @author 유지보수
 * @version 6.0
 * @since 2025.06.01
 */
public class EgovCryptoConfigReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovCryptoConfigReader.class);
    private static final String DEFAULT_CONFIG_PATH = "egovframework/egovProps/conf/egov-crypto-config.properties";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String FILE_PREFIX = "file:";

    private final String configPath;
    private final ResourceLoader resourceLoader;

    public EgovCryptoConfigReader() {
        this(null, null);
    }

    /**
     * globals.properties 등에서 지정한 설정 파일 경로로 리더 생성
     */
    public EgovCryptoConfigReader(String configPath, ResourceLoader resourceLoader) {
        this.configPath = (configPath != null && !configPath.trim().isEmpty()) ? configPath.trim() : null;
        this.resourceLoader = resourceLoader;
    }

    /**
     * 지정된 경로의 설정 파일을 읽어 EgovCryptoConfig 객체를 반환.
     * ResourceLoader가 있으면 먼저 사용(테스트 등 Spring 컨텍스트에서 해당 컨텍스트 classpath 기준으로 로드),
     * 없거나 실패 시 context classloader → 파일 순으로 시도하고, 모두 실패 시 기본 설정을 반환한다.
     */
    public EgovCryptoConfig readConfig() {
        String path = configPath != null ? configPath : DEFAULT_CONFIG_PATH;
        InputStream inputStream = null;
        String triedPath = path;
		// 2026.02.28 KISA 보안취약점 조치
        try {
            inputStream = getInputStream(path);
            if (inputStream == null && resourceLoader != null) {
                String resourcePath = toResourcePath(path);
                Resource resource = resourceLoader.getResource(resourcePath);
                if (resource.exists()) {
                    inputStream = resource.getInputStream();
                    triedPath = resourcePath;
                }
            }
            if (inputStream == null) {
                inputStream = getInputStream(DEFAULT_CONFIG_PATH);
                triedPath = DEFAULT_CONFIG_PATH;
            }
            if (inputStream == null && resourceLoader != null) {
                Resource resource = resourceLoader.getResource(CLASSPATH_PREFIX + DEFAULT_CONFIG_PATH);
                if (resource.exists()) {
                    inputStream = resource.getInputStream();
                    triedPath = CLASSPATH_PREFIX + DEFAULT_CONFIG_PATH;
                }
            }
            if (inputStream != null) {
                Properties props = new Properties();
                try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    props.load(reader);
                }
                return mapPropertiesToBean(props, EgovCryptoConfig.class);
            }
        } catch (IOException e) {
            LOGGER.debug("Failed to read crypto configuration file, use default config. tried: {} - {}", triedPath, e.getMessage());
        }
        LOGGER.debug("Using default crypto configuration (no file found for path: {})", path);
        return createDefaultConfig();
    }

    private static String toResourcePath(String path) {
        if (path.startsWith(CLASSPATH_PREFIX) || path.startsWith(FILE_PREFIX)) {
            return path;
        }
        File f = new File(path);
        if (f.isAbsolute() && f.exists()) {
            return FILE_PREFIX + path;
        }
        return CLASSPATH_PREFIX + path;
    }

    // 2026.02.28 KISA 보안취약점 조치
    // properties 설정 파일을 읽어 EgovCryptoConfig 객체로 변환
    private static EgovCryptoConfig mapPropertiesToBean(Properties props, Class<EgovCryptoConfig> beanClass) {
        EgovCryptoConfig bean;
        // 2026.02.28 KISA 보안취약점 조치
        try {
            bean = beanClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create config instance: " + e.getMessage(), e);
        }
        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            if (value == null) continue;
            String setterName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
            // 2026.02.28 KISA 보안취약점 조치
            try {
                Method setter = findSetter(beanClass, setterName);
                if (setter != null) {
                    Object arg = convertValue(value, setter.getParameterTypes()[0]);
                    setter.invoke(bean, arg);
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number format for config property : " + key + " : " + e.getMessage(), e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid config property value for : " + key + " : " + e.getMessage(), e);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Failed to set config property : " + key + " : " + e.getMessage(), e);
            }
        }
        return bean;
    }

    private static Method findSetter(Class<?> beanClass, String setterName) {
        for (Method m : beanClass.getMethods()) {
            if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                return m;
            }
        }
        return null;
    }

    private static Object convertValue(String value, Class<?> targetType) {
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value.trim());
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value.trim());
        }
        return value;
    }

    /**
     * 경로에 따라 classpath 리소스 또는 파일 시스템에서 InputStream 반환. 없으면 null.
     */
    private static InputStream getInputStream(String path) throws IOException {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith(CLASSPATH_PREFIX)) {
            String resourcePath = path.substring(CLASSPATH_PREFIX.length()).trim();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            // 2026.02.28 KISA 보안취약점 조치
            if (cl != null) {
                InputStream is = null;
                boolean returned = false;
                try {
                    is = cl.getResourceAsStream(resourcePath);
                    if (is != null) {
                        returned = true;
                        return is;
                    }
                } finally {
                    if (!returned && is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            LOGGER.debug("Error closing stream while reading security config: {}", path, e);
                        }
                    }
                }
            }
            // 2026.02.28 KISA 보안취약점 조치
            try {
                return new ClassPathResource(resourcePath).getInputStream();
            } catch (IOException e) {
                LOGGER.debug("Failed to read security configuration file: {}", path, e);
                return null;
            }
        }
        if (path.startsWith(FILE_PREFIX)) {
            String filePath = path.substring(FILE_PREFIX.length()).trim();
            File file = new File(filePath);
            if (file.isFile() && file.canRead()) return new FileInputStream(file);
            return null;
        }
        File file = new File(path);
        if (file.isFile() && file.canRead()) {
            return new FileInputStream(file);
        }
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            return cl.getResourceAsStream(path);
        }
        // 2026.02.28 KISA 보안취약점 조치
        try {
            return new ClassPathResource(path).getInputStream();
        } catch (IOException e) {
            LOGGER.debug("Failed to read security configuration file: {}", path, e);
            return null;
        }
    }

    /**
     * 기본 설정 객체를 생성
     */
    private EgovCryptoConfig createDefaultConfig() {
        EgovCryptoConfig config = new EgovCryptoConfig();
        config.setId("egovCryptoConfig");
        config.setInitial(true);
        config.setCrypto(true);
        config.setAlgorithm("SHA-256");
        config.setAlgorithmKey("egovframe");
        config.setAlgorithmKeyHash("gdyYs/IZqY86VcWhT8emCYfqY1ahw2vtLG+/FzNqtrQ=");
        config.setCryptoBlockSize(1024);
        config.setCryptoPropertyLocation("classpath:/egovframework/egovProps/globals.properties");
        config.setPlainDigest(false);
        LOGGER.debug("Use Basic Crypto Configuration");
        return config;
    }

}
