/*
 * Copyright 2012-2014 MOSPA(Ministry of Security and Public Administration).
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
package org.egovframe.rte.bat.core.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * Reflection 관련 method
 * @author 실행환경 개발팀
 * @since 2012.07.20
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.07.20	실행환경개발팀		최초 생성
 * 2017.02.15	장동한				시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
 * </pre>
*/
public class EgovReflectionSupport<T> {

	 // slf4J logger 로 변경 : 2014.04.30
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovReflectionSupport.class);

	// 실제 VO가 담길 Object
	private Object object = null;

	// VO에 들어있는 Method 목록
	private Method[] methods;

	// VO의 Field명과 Method 목록의 Index를 매핑
	private HashMap<String, Method> methodMap;

	// VO의 Field 타입
	private Type[] fieldType;

	public EgovReflectionSupport() {
		super();
	}

	/**
	 * VO에 들어있는 Method 목록 get 
	 */
	public HashMap<String, Method> getMethodMap() {
		return methodMap;
	}

	/**
	 * 주어진 Method 이름(methodName)으로  Method를 검색한다. 
	 * @param methods
	 * @param methodName
	 * @return Method
	 */
	private Method retrieveMethod(Method[] methods, String methodName) {
		Method method = null;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName)) {
				method = methods[i];
				break;
			}
		}
		return method;
	}

	/**
	 * VO 타입의 instance 생성
	 * @param type VO 타입
	 */
	private void createObject(Class<?> type) {
		try {
			object = type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			ReflectionUtils.handleReflectionException(e);
		}
	}

	/**
	 * VO의 Field명과 Setter Method들을 비교하여 Map에 Put 한다.
	 * Bean 생성시 한 번만 실행 된다.
	 */
	public void generateSetterMethodMap(Class<?> type, String[] names) {
		try {
			methods = type.newInstance().getClass().getMethods();
		} catch (InstantiationException | IllegalAccessException e) {
			ReflectionUtils.handleReflectionException(e);
		}
		methodMap = new HashMap<String, Method>();
		try {
			if (ArrayUtils.isNotEmpty(names) && names.length > 0) {
				for (int i = 0; i < names.length; i++) {
					String strMethod;
					if (names[i].length() > 0) {
						strMethod = "set" + (names[i].substring(0, 1)).toUpperCase() + names[i].substring(1);
						methodMap.put(names[i], retrieveMethod(methods, strMethod));
					}
				}
			}
		//2017.02.15 장동한 시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
		} catch (StringIndexOutOfBoundsException | NullPointerException e) {
			ReflectionUtils.handleReflectionException(e);
		}
	}

	/**
	 * VO의 Setter method 실행
	 * @param tokens VO에 set 될 value
	 * @param names VO의 field 명
	 */
	private void invokeSetterMethod(List<String> tokens, String[] names) {
		Method method;
		for (int i = 0; i < names.length; i++) {
			method = methodMap.get(names[i]);	
			try {
				method.invoke(object, parsingFromString(tokens.get(i).trim(), fieldType[i]));
			} catch (IllegalAccessException | InvocationTargetException e) {
				ReflectionUtils.handleReflectionException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public T generateObject(Class<?> type, List<String> tokens, String[] names) {
		createObject(type);
		invokeSetterMethod(tokens, names);
		return (T) object;
	}

	/**
	 * VO의 Field명과 Getter Method들을 비교하여 Map에 Put 한다.
	 * Bean 생성시 한 번만 실행 된다.
	 */
	public void generateGetterMethodMap(String[] names, T item) {
		if (methods == null) {
			methods = item.getClass().getMethods();
			methodMap = new HashMap<String, Method>();
			try {
				if (ArrayUtils.isNotEmpty(names) && names.length > 0) {
					for (int i = 0; i < names.length; i++) {
						String strMethod;
						if (names[i].length() > 0) {
							strMethod = "get" + (names[i].substring(0, 1)).toUpperCase() + names[i].substring(1);
							methodMap.put(names[i], retrieveMethod(methods, strMethod));
						}
					}
				}
			//2017.02.15 장동한 시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
			} catch (StringIndexOutOfBoundsException | NullPointerException e) {
				ReflectionUtils.handleReflectionException(e);
			}
		}
	}

	/**
	 * item의 field 정보를 가져오기 위해 getter를 invoke
	 * @param item 정보를 담고 있는 VO
	 * @param names VO의 field name
	 * @return getValue field 정보를 get 하여 return
	 */
	public Object invokeGettterMethod(T item, String names) {
		Object value = null;
		try {
			value = methodMap.get(names).invoke(item);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			ReflectionUtils.handleReflectionException(e);
		}
		return value;
	}

	/**
	 * item의 field 정보를 가져오기 위해 getter를 invoke
	 * @param item item 정보를 담고 있는 VO
	 * @param param
	 * @param mapMethod VO의 getter 메소드를 갖고 있는 Map
	 * @return field 정보를 get 하여 return
	 */
	public Object invokeGettterMethod(T item, String param, Map<String, Method> mapMethod) {
		Object value = null;
		try {	
			value = mapMethod.get(param).invoke(item);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			ReflectionUtils.handleReflectionException(e);
		}
		return value;
	}

	/**
	 * VO의 field의 타입 get
	 * @param type VO의 타입
	 * @param names VO field names
	 */
	public void getFieldType(Class<?> type, String[] names) {
		fieldType = new Type[names.length];
		for (int i = 0; i < names.length; i++) {
			try {
				fieldType[i] = type.newInstance().getClass().getDeclaredField(names[i]).getType();
			} catch (SecurityException | NoSuchFieldException | InstantiationException | IllegalAccessException e) {
				ReflectionUtils.handleReflectionException(e);
			}
		}
	}

	/**
	 * sqlType을 배열 형식으로 받는 메소드 
	 */
	public String[] getSqlTypeArray(String[] params, Object item) {
		String[] sqlTypes = new String[params.length];
		for (int i = 0; i < params.length; i++) {
			try {
				sqlTypes[i] = item.getClass().getDeclaredField(params[i]).getType().getSimpleName().toString();
			} catch (SecurityException | NoSuchFieldException e) {
				ReflectionUtils.handleReflectionException(e);
			}
		}
		return sqlTypes;
	}

	/**
	 * String to Type parsing
	 * @param tokenValue String 형태의 token value
	 * @param type 실제 VO에서 token value의 type
	 * @return parsingValue 해당 type으로 paring한 value return
	 */
	private Object parsingFromString(String tokenValue, Type type) {
		Object parsingValue = null;
		if (type == String.class) {
			parsingValue = tokenValue;
		} else if (type == int.class) {
			parsingValue = Integer.parseInt(tokenValue);
		} else if (type == double.class) {
			parsingValue = Double.parseDouble(tokenValue);
		} else if (type == float.class) {
			parsingValue = Float.parseFloat(tokenValue);
		} else if (type == long.class) {
			parsingValue = Long.parseLong(tokenValue);
		} else if (type == char.class) {
			parsingValue = tokenValue.charAt(0);
		} else if (type == byte[].class) {
			parsingValue = tokenValue.getBytes();
		} else if (type == boolean.class) {
			parsingValue = Boolean.valueOf(tokenValue);
		} else if (type == BigDecimal.class) {
			parsingValue = new BigDecimal(tokenValue);
		} else {
			parsingValue = tokenValue;
		}
		return parsingValue;
	}
	
}
