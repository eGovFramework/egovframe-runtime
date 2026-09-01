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
package org.egovframe.rte.fdl.string;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 객체의 로딩을 지원하는 유틸 클래스
 * Class 로딩 등의 객체관련 기능을 제공하는 유틸이다.
 * <p><b>보안 주의:</b> {@link #loadClass(String)}/{@link #instantiate(String)}는 className을 검증 없이
 * 그대로 클래스로더에 전달해 로드·인스턴스화한다(CWE-470 Unsafe Reflection). 신뢰할 수 없는 입력
 * (사용자 요청 파라미터 등)을 className으로 직접 전달하지 말 것. 값의 출처를 신뢰할 수 없다면
 * {@link #setAllowedClassNames(Set)}으로 허용 클래스 목록을 설정하라(기본값은 null=제한 없음,
 * 기존 동작과 동일).</p>
 * 개정이력(Modification Information)
 * <p>
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	윤성종			최초 생성
 * 2023.08.31   유지보수			기능 추가(isEmpty(), Contribution 반영)
 * 2024.08.17	이백행			시큐어코딩 Exception 제거
 */
public final class EgovObjectUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovObjectUtil.class);

    /**
     * 로딩을 허용할 클래스명(FQCN) 화이트리스트. null이면 제한 없음(기본값, 기존 동작과 동일 — 하위 호환).
     */
    private static volatile Set<String> allowedClassNames = null;

    private EgovObjectUtil() {
    }

    /**
     * className이 신뢰할 수 없는 입력으로 흘러들 가능성이 있는 경우, 로딩 가능한 클래스를 제한하려면
     * 이 메서드로 허용 목록을 설정한다. 설정 시 {@link #loadClass(String)}(및 이를 호출하는
     * {@link #instantiate(String)} 등)는 목록에 없는 클래스명에 대해 {@link SecurityException}을 던진다.
     * null 또는 빈 집합을 넘기면 제한이 해제된다(기본값).
     *
     * @param classNames 허용할 클래스명(FQCN) 집합
     */
    public static void setAllowedClassNames(Set<String> classNames) {
        allowedClassNames = (classNames == null || classNames.isEmpty())
                ? null
                : Collections.unmodifiableSet(new HashSet<>(classNames));
    }

    /**
     * 클래스명으로 객체를 로딩한다.
     */
    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        Set<String> allowed = allowedClassNames;
        if (allowed != null && !allowed.contains(className)) {
            throw new SecurityException("class '" + className + "' is not in the allowed class list");
        }
        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
        if (clazz == null) {
            clazz = Class.forName(className);
        }
        return clazz;
    }

    /**
     * 클래스명으로 객체를 로드한 후 인스턴스화 한다.
     */
    public static Object instantiate(String className) {
        Class<?> clazz;
        try {
            clazz = loadClass(className);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Class is can not instantialized", e);
        }
    }

    /**
     * 클래스명으로 파라매터가 있는 클래스의 생성자를 인스턴스화 한다.
     */
    public static Object instantiate(String className, String[] types, Object[] values) {
        Class<?> clazz;
        Class<?>[] classParams = new Class[values.length];
        Object[] objectParams = new Object[values.length];
        try {
            clazz = loadClass(className);
            for (int i = 0; i < values.length; i++) {
                classParams[i] = loadClass(types[i]);
                objectParams[i] = values[i];
            }
            Constructor<?> constructor = clazz.getConstructor(classParams);
            return constructor.newInstance(values);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Class is can not instantialized", e);
        }
    }

    /**
     * 객체가 Null 인지 확인한다.
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     * 객체가 비어있는 값인지 확인한다.
     */
    public static boolean isEmpty(@Nullable Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof Optional) {
            return !((Optional<?>) obj).isPresent();
        } else if (obj instanceof CharSequence) {
            return ((CharSequence) obj).isEmpty();
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        } else {
            return obj instanceof Map && ((Map<?, ?>) obj).isEmpty();
        }
    }

}
