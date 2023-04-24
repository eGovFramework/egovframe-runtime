/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package org.egovframe.rte.itl.integration.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * 연계 서비스의 표준 메시지를 정의 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지를 정의하기 위한 Class 이다. Primitive Type을 정의하기 위한
 * Class로 Java의 Primitive Type 및 기본 Type을 제공한다.
 * </p>
 * 
 * <ul>
 * <li><code>BOOLEAN</code> : <code>boolean</code>,
 * <code>java.lang.Boolean</code>
 * <li><code>STRING</code> : <code>java.lang.String</code>
 * <li><code>BYTE</code> : <code>byte</code>, <code>java.lang.Byte</code>
 * <li><code>SHORT</code> : <code>short</code>, <code>java.lang.Short</code>
 * <li><code>INTEGER</code> : <code>int</code>, <code>java.lang.Integer</code>
 * <li><code>LONG</code> : <code>long</code>, <code>java.lang.Long</code>
 * <li><code>BIGINTEGER</code> : <code>java.math.BigInteger</code>
 * <li><code>FLOAT</code> : <code>float</code>, <code>java.lang.Float</code>
 * <li><code>DOUBLE</code> : <code>double</code>, <code>java.lang.Double</code>
 * <li><code>BIGDECIMAL</code> : <code>java.math.BigDecimal</code>
 * <li><code>CALENDAR</code> : <code>java.util.Calendar</code>,
 * <code>java.util.Date</code> (일자 + 시각)
 * </ul>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	심상호				최초 생성
 * </pre>
 */
public abstract class PrimitiveType extends AbstractType {

    /**
     * Default Constructor
     */
    protected PrimitiveType() {
        super();
    }

    /**
     * Argument <code>id</code>를 id로 가진 PrimitiveType을
     * 생성한다.
     * @param id
     *        id
     * @param name
     *        name
     * @param assignableClasses
     *        Assign 할 수 있는 Java 객체의 Class들
     * @throws IllegalArgumentException
     *         1. Argument <code>id</code> 값이
     *         <code>null</code>이거나, 공백 문자로만 이루어진 경우 2.
     *         Argument <code>name</code> 값이
     *         <code>null</code>이거나, 공백 문자로만 이루어진 경우 3.
     *         Argument <code>assignableClasses</code>가
     *         <code>null</code>이거나 빈 Array인 경우
     */
    protected PrimitiveType(final String id, final String name,
            final Class<?>[] assignableClasses) {
        super(id, name, assignableClasses);
    }

    /**
     * Argument <code>id</code>를 id로 가진 PrimitiveType을
     * 생성한다.
     * @param id
     *        id
     * @param name
     *        name
     * @param assignableClasses
     *        Assign 할 수 있는 Java 객체의 Class들
     * @throws IllegalArgumentException
     *         1. Argument <code>id</code> 값이
     *         <code>null</code>이거나, 공백 문자로만 이루어진 경우 2.
     *         Argument <code>name</code> 값이
     *         <code>null</code>이거나, 공백 문자로만 이루어진 경우 3.
     *         Argument <code>assignableClasses</code>가
     *         <code>null</code>이거나 빈 List인 경우
     */
    protected PrimitiveType(final String id, final String name,
            final List<Class<?>> assignableClasses) {
        super(id, name, assignableClasses);
    }

    public boolean isAssignableValue(Object source) {
        if (source == null) {
            return true;
        }
        return isAssignableFrom(source.getClass());
    }

    /**
     * <code>BOOLEAN</code> Type
     * <p>
     * Assignable Java Classes :
     * <ul>
     * <li><code>boolean</code>
     * <li><code>java.lang.Boolean</code>
     * </ul>
     */
    public static final PrimitiveType BOOLEAN =
        new PrimitiveType("boolean", "boolean", new Class<?>[] {boolean.class,
            Boolean.class }) {
            public Object convertToTypedObject(final Object source) {
                if (source == null) {
                    return null;
                }
                if (isAssignableFrom(source.getClass()) == false) {
                    throw new UnassignableValueException();
                }
                return (Boolean) source;
            }
        };

    /**
     * <code>STRING</code> Type
     * <p>
     * Assignable Java Class :
     * <ul>
     * <li><code>java.lang.String</code>
     * </ul>
     */
    public static final PrimitiveType STRING =
        new PrimitiveType("string", "string", new Class<?>[] {String.class }) {
            public Object convertToTypedObject(final Object source) {
                if (source == null) {
                    return null;
                }
                if (isAssignableFrom(source.getClass()) == false) {
                    throw new UnassignableValueException();
                }
                return (String) source;
            }
        };

    /**
     * <code>BYTE</code> Type
     * <p>
     * Assignable Java Classes :
     * <ul>
     * <li><code>byte</code>
     * <li><code>java.lang.Byte</code>
     * </ul>
     */
    public static final PrimitiveType BYTE =
        new PrimitiveType("byte", "byte", new Class<?>[] {byte.class,
            Byte.class }) {
            public Object convertToTypedObject(final Object source) {
                if (source == null) {
                    return null;
                }
                if (isAssignableFrom(source.getClass()) == false) {
                    throw new UnassignableValueException();
                }
                return ((Number) source).byteValue();
            }
        };

    /**
     * <code>SHORT</code> Type
     * <p>
     * Assignable Java Classes :
     * <ul>
     * <li><code>byte</code>
     * <li><code>java.lang.Byte</code>
     * <li><code>short</code>
     * <li><code>java.lang.Short</code>
     * </ul>
     */
    public static final PrimitiveType SHORT =
        new PrimitiveType("short", "short", new Class<?>[] {byte.class,
            Byte.class, short.class, Short.class }) {
            public Object convertToTypedObject(final Object source) {
                if (source == null) {
                    return null;
                }
                if (isAssignableFrom(source.getClass()) == false) {
                    throw new UnassignableValueException();
                }
                return ((Number) source).shortValue();
            }
        };

    /**
     * <code>INTEGER</code> Type
     * <p>
     * Assignable Java Classes :
     * <ul>
     * <li><code>byte</code>
     * <li><code>java.lang.Byte</code>
     * <li><code>short</code>
     * <li><code>java.lang.Short</code>
     * <li><code>int</code>
     * <li><code>java.lang.Integer</code>
     * </ul>
     */
    public static final PrimitiveType INTEGER =
        new PrimitiveType("integer", "integer", new Class<?>[] {byte.class,
            Byte.class, short.class, Short.class, int.class, Integer.class }) {
            public Object convertToTypedObject(final Object source) {
                if (source == null) {
                    return null;
                }
                if (isAssignableFrom(source.getClass()) == false) {
                    throw new UnassignableValueException();
                }
                return ((Number) source).intValue();
            }
        };

    /**
     * <code>LONG</code> Type
     * <p>
     * Assignable Java Classes :
     * <ul>
     * <li><code>byte</code>
     * <li><code>java.lang.Byte</code>
     * <li><code>short</code>
     * <li><code>java.lang.Short</code>
     * <li><code>int</code>
     * <li><code>java.lang.Integer</code>
     * <li><code>long</code>
     * <li><code>java.lang.Long</code>
     * </ul>
     */
    public static final PrimitiveType LONG =
        new PrimitiveType("long", "long", new Class<?>[] {byte.class,
            Byte.class, short.class, Short.class, int.class, Integer.class,
            long.class, Long.class }) {
            public Object convertToTypedObject(final Object source) {
                if (source == null) {
                    return null;
                }
                if (isAssignableFrom(source.getClass()) == false) {
                    throw new UnassignableValueException();
                }
                return ((Number) source).longValue();
            }
        };

    /**
     * <code>BIGINTEGER</code> Type
     * <p>
     * Assignable Java Classes :
     * <ul>
     * <li><code>byte</code>
     * <li><code>java.lang.Byte</code>
     * <li><code>short</code>
     * <li><code>java.lang.Short</code>
     * <li><code>int</code>
     * <li><code>java.lang.Integer</code>
     * <li><code>long</code>
     * <li><code>java.lang.Long</code>
     * <li><code>java.math.BigInteger</code>
     * </ul>
     */
    public static final PrimitiveType BIGINTEGER =
        new PrimitiveType("biginteger", "biginteger", new Class<?>[] {
            byte.class, Byte.class, short.class, Short.class, int.class,
            Integer.class, long.class, Long.class, BigInteger.class }) {
            public Object convertToTypedObject(final Object source) {
                if (source == null) {
                    return null;
                }
                if (isAssignableFrom(source.getClass()) == false) {
                    throw new UnassignableValueException();
                }
                if (source instanceof BigInteger) {
                    return source;
                }
                return BigInteger.valueOf(((Number) source).longValue());
            }
        };

    /**
     * <code>FLOAT</code> Type
     * <p>
     * Assignable Java Classes :
     * <ul>
     * <li><code>byte</code>
     * <li><code>java.lang.Byte</code>
     * <li><code>short</code>
     * <li><code>java.lang.Short</code>
     * <li><code>float</code>
     * <li><code>java.lang.Float</code>
     * </ul>
     */
    public static final PrimitiveType FLOAT =
        new PrimitiveType("float", "float", new Class<?>[] {byte.class,
            Byte.class, short.class, Short.class, float.class, Float.class }) {
            public Object convertToTypedObject(final Object source) {
                if (source == null) {
                    return null;
                }
                if (isAssignableFrom(source.getClass()) == false) {
                    throw new UnassignableValueException();
                }
                return ((Number) source).floatValue();
            }
        };

    /**
     * <code>DOUBLE</code> Type
     * <p>
     * Assignable Java Classes :
     * <ul>
     * <li><code>byte</code>
     * <li><code>java.lang.Byte</code>
     * <li><code>short</code>
     * <li><code>java.lang.Short</code>
     * <li><code>int</code>
     * <li><code>java.lang.Integer</code>
     * <li><code>float</code>
     * <li><code>java.lang.Float</code>
     * <li><code>double</code>
     * <li><code>java.lang.Double</code>
     * </ul>
     */
    public static final PrimitiveType DOUBLE =
        new PrimitiveType("double", "double", new Class<?>[] {byte.class,
            Byte.class, short.class, Short.class, int.class, Integer.class,
            float.class, Float.class, double.class, Double.class }) {
            public Object convertToTypedObject(final Object source) {
                if (source == null) {
                    return null;
                }
                if (isAssignableFrom(source.getClass()) == false) {
                    throw new UnassignableValueException();
                }
                return ((Number) source).doubleValue();
            }
        };

    /**
     * <code>BIGDECIMAL</code> Type
     * <p>
     * Assignable Java Classes :
     * <ul>
     * <li><code>byte</code>
     * <li><code>java.lang.Byte</code>
     * <li><code>short</code>
     * <li><code>java.lang.Short</code>
     * <li><code>int</code>
     * <li><code>java.lang.Integer</code>
     * <li><code>long</code>
     * <li><code>java.lang.Long</code>
     * <li><code>float</code>
     * <li><code>java.math.BigInteger</code>
     * <li><code>java.lang.Float</code>
     * <li><code>double</code>
     * <li><code>java.lang.Double</code>
     * <li><code>java.math.BigDecimal</code>
     * </ul>
     */
    public static final PrimitiveType BIGDECIMAL =
        new PrimitiveType("bigdecimal", "bigdecimal", new Class<?>[] {
            byte.class, Byte.class, short.class, Short.class, int.class,
            Integer.class, long.class, Long.class, BigInteger.class,
            float.class, Float.class, double.class, Double.class,
            BigDecimal.class }) {
            public Object convertToTypedObject(final Object source) {
                if (source == null) {
                    return null;
                }
                if (isAssignableFrom(source.getClass()) == false) {
                    throw new UnassignableValueException();
                }
                if (source instanceof BigDecimal) {
                    return source;
                } else if (source instanceof BigInteger) {
                    return new BigDecimal((BigInteger) source);
                } else if (BIGINTEGER.isAssignableValue(source)) {
                    return new BigDecimal((BigInteger) BIGINTEGER
                        .convertToTypedObject(source));
                }
                return BigDecimal.valueOf(((Number) source).doubleValue());
            }
        };

    /**
     * <code>CALENDAR</code> Type
     * <p>
     * Assignable Java Classes :
     * <ul>
     * <li><code>java.util.Date</code>
     * <li><code>java.util.Calendar</code>
     * </ul>
     */
    public static final PrimitiveType CALENDAR =
        new PrimitiveType("calendar", "calendar", new Class<?>[] {Date.class,
            Calendar.class }) {
            public Object convertToTypedObject(final Object source) {
                if (source == null) {
                    return null;
                }
                if (isAssignableFrom(source.getClass()) == false) {
                    throw new UnassignableValueException();
                }
                if (source instanceof Date) {
                    Calendar value = Calendar.getInstance();
                    value.setTime((Date) source);
                    return value;
                } else {
                    // Calendar
                    return source;
                }
            }
        };

    protected static final Map<String, PrimitiveType> primitiveTypes =
        new HashMap<String, PrimitiveType>() {
            /**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = -5126785475742494349L;

			private void putType(final PrimitiveType type) {
                put(type.getId(), type);
            }

            {
                putType(BOOLEAN);
                putType(STRING);
                putType(BYTE);
                putType(SHORT);
                putType(INTEGER);
                putType(LONG);
                putType(BIGINTEGER);
                putType(FLOAT);
                putType(DOUBLE);
                putType(BIGDECIMAL);
                putType(CALENDAR);
            }
        };

    /**
     * PrimitiveType instance를 얻어온다.
     * @param id
     *        type id
     * @return PrimitiveType이 있을 경우 PrimitiveType
     *         instance, 없으면 <code>null</code>
     */
    public static PrimitiveType getPrimitiveType(final String id) {
        return primitiveTypes.get(id);
    }

}
