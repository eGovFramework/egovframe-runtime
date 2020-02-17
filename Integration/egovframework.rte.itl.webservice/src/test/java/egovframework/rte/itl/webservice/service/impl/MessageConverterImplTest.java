package egovframework.rte.itl.webservice.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import egovframework.rte.itl.integration.type.ListType;
import egovframework.rte.itl.integration.type.PrimitiveType;
import egovframework.rte.itl.integration.type.RecordType;
import egovframework.rte.itl.integration.type.Type;
import egovframework.rte.itl.webservice.service.EgovWebServiceClassLoader;
import egovframework.rte.itl.webservice.service.MessageConverter;
import egovframework.rte.itl.webservice.service.ServiceEndpointInfo;
import egovframework.rte.itl.webservice.service.ServiceEndpointInterfaceInfo;

import org.junit.Test;

public class MessageConverterImplTest {
	private final RecordType personRecordType = new RecordType("personRecordType", "PersonRecordType", new HashMap<String, Type>() {
		/**
		 *  serialVersion UID
		 */
		private static final long serialVersionUID = -4732914927397819381L;

		{
			put("no", PrimitiveType.INTEGER);
			put("name", PrimitiveType.STRING);
		}
	});

	private final ListType personListType = new ListType("person[]", "person array", personRecordType);

	private final RecordType recordType = new RecordType("recordType", "RecordType", new HashMap<String, Type>() {
		/**
		 *  serialVersion UID
		 */
		private static final long serialVersionUID = 1552380011450990901L;

		{
			put("booleanValue", PrimitiveType.BOOLEAN);
			put("byteValue", PrimitiveType.BYTE);
			put("shortValue", PrimitiveType.SHORT);
			put("intValue", PrimitiveType.INTEGER);
			put("longValue", PrimitiveType.LONG);
			put("bigIntegerValue", PrimitiveType.BIGINTEGER);
			put("floatValue", PrimitiveType.FLOAT);
			put("doubleValue", PrimitiveType.DOUBLE);
			put("bigDecimalValue", PrimitiveType.BIGDECIMAL);
			put("stringValue", PrimitiveType.STRING);
			put("calendarValue", PrimitiveType.CALENDAR);
			put("personList", personListType);
		}
	});

	private final EgovWebServiceClassLoader classLoader = new EgovWebServiceClassLoader() {
		private final Map<Type, Class<?>> map = new HashMap<Type, Class<?>>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = 4357638556504895586L;

			{
				put(personRecordType, Person.class);
				put(personListType, (new Person[0]).getClass());
				put(recordType, ValueObject.class);
			}
		};

		public String getFieldNameOfServiceBridge() {
			return null;
		}

		public Class<?> loadClass(ServiceEndpointInfo serviceEndpointInfo) throws ClassNotFoundException {
			return null;
		}

		public Class<?> loadClass(ServiceEndpointInterfaceInfo serviceEndpointInterfaceInfo) throws ClassNotFoundException {
			return null;
		}

		public Class<?> loadClass(Type type) throws ClassNotFoundException {
			return map.get(type);
		}
	};

	private final MessageConverter messageConverter = new MessageConverterImpl(classLoader);

	private final List<Object> personList = new ArrayList<Object>() {
		/**
		 *  serialVersion UID
		 */
		private static final long serialVersionUID = 1521021649881780719L;

		{
			add(new HashMap<String, Object>() {
				/**
				 *  serialVersion UID
				 */
				private static final long serialVersionUID = 2024858600074988182L;

				{
					put("no", 1);
					put("name", "ȫ�浿");
				}
			});
			add(new HashMap<String, Object>() {
				/**
				 *  serialVersion UID
				 */
				private static final long serialVersionUID = 2111273953420556406L;

				{
					put("no", 2);
					put("name", "��ö��");
				}
			});
			add(new HashMap<String, Object>() {
				/**
				 *  serialVersion UID
				 */
				private static final long serialVersionUID = 8184523077965140910L;

				{
					put("no", 3);
					put("name", "�ֿ���");
				}
			});
		}
	};

	private final Map<String, Object> typedObject = new HashMap<String, Object>() {
		/**
		 *  serialVersion UID
		 */
		private static final long serialVersionUID = 8103299603788545932L;

		{
			put("booleanValue", true);
			put("byteValue", Byte.MAX_VALUE);
			put("shortValue", Short.MAX_VALUE);
			put("intValue", Integer.MAX_VALUE);
			put("longValue", Long.MAX_VALUE);
			put("bigIntegerValue", BigInteger.valueOf(Long.MAX_VALUE));
			put("floatValue", Float.MAX_VALUE);
			put("doubleValue", Double.MAX_VALUE);
			put("bigDecimalValue", BigDecimal.valueOf(Double.MAX_VALUE));
			put("stringValue", "String");
			put("calendarValue", Calendar.getInstance());
			put("personList", personList);
		}
	};

	private final ValueObject valueObject = new ValueObject() {
		{
			booleanValue = true;
			byteValue = Byte.MAX_VALUE;
			shortValue = Short.MAX_VALUE;
			intValue = Integer.MAX_VALUE;
			longValue = Long.MAX_VALUE;
			bigIntegerValue = BigInteger.valueOf(Long.MAX_VALUE);
			floatValue = Float.MAX_VALUE;
			doubleValue = Double.MAX_VALUE;
			bigDecimalValue = BigDecimal.valueOf(Double.MAX_VALUE);
			stringValue = "String";
			calendarValue = Calendar.getInstance();
			personList = new Person[] { new Person() {
				{
					no = 1;
					name = "ȫ�浿";
				}
			}, new Person() {
				{
					no = 2;
					name = "��ö��";
				}
			}, new Person() {
				{
					no = 3;
					name = "�ֿ���";
				}
			} };
		}
	};

	@SuppressWarnings("unchecked")
	@Test
	public void testConvertToValueObject() throws Exception {
		Object object = messageConverter.convertToValueObject(typedObject, recordType);

		assertNotNull(object);
		assertTrue(object instanceof ValueObject);
		ValueObject valueObject = (ValueObject) object;
		assertEquals(typedObject.get("booleanValue"), valueObject.booleanValue);
		assertEquals(typedObject.get("byteValue"), valueObject.byteValue);
		assertEquals(typedObject.get("shortValue"), valueObject.shortValue);
		assertEquals(typedObject.get("intValue"), valueObject.intValue);
		assertEquals(typedObject.get("longValue"), valueObject.longValue);
		assertEquals(typedObject.get("bigIntegerValue"), valueObject.bigIntegerValue);
		assertEquals(typedObject.get("floatValue"), valueObject.floatValue);
		assertEquals(typedObject.get("doubleValue"), valueObject.doubleValue);
		assertEquals(typedObject.get("bigDecimalValue"), valueObject.bigDecimalValue);
		assertEquals(typedObject.get("stringValue"), valueObject.stringValue);
		assertEquals(typedObject.get("calendarValue"), valueObject.calendarValue);
		Person[] personArray = valueObject.personList;
		assertNotNull(personArray);
		assertEquals(personList.size(), personArray.length);
		for (int i = 0; i < personList.size(); i++) {
			Map<String, Object> personMap = (Map<String, Object>) personList.get(i);
			Person person = personArray[i];

			assertEquals(personMap.get("no"), person.no);
			assertEquals(personMap.get("name"), person.name);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConvertToTypedObject() throws Exception {
		Object object = messageConverter.convertToTypedObject(valueObject, recordType);

		assertNotNull(object);
		assertTrue(object instanceof Map);
		Map<String, Object> typedObject = (Map<String, Object>) object;
		assertEquals(valueObject.booleanValue, typedObject.get("booleanValue"));
		assertEquals(valueObject.byteValue, typedObject.get("byteValue"));
		assertEquals(valueObject.shortValue, typedObject.get("shortValue"));
		assertEquals(valueObject.intValue, typedObject.get("intValue"));
		assertEquals(valueObject.longValue, typedObject.get("longValue"));
		assertEquals(valueObject.bigIntegerValue, typedObject.get("bigIntegerValue"));
		assertEquals(valueObject.floatValue, typedObject.get("floatValue"));
		assertEquals(valueObject.doubleValue, typedObject.get("doubleValue"));
		assertEquals(valueObject.bigDecimalValue, typedObject.get("bigDecimalValue"));
		assertEquals(valueObject.stringValue, typedObject.get("stringValue"));
		assertEquals(valueObject.calendarValue, typedObject.get("calendarValue"));
		Person[] personArray = valueObject.personList;
		object = typedObject.get("personList");
		assertNotNull(object);
		assertTrue(object instanceof List);
		List<Object> personList = (List<Object>) object;
		assertEquals(personArray.length, personList.size());
		for (int i = 0; i < personArray.length; i++) {
			Person person = personArray[i];
			object = personList.get(i);
			assertNotNull(object);
			assertTrue(object instanceof Map);
			Map<String, Object> personMap = (Map<String, Object>) object;
			assertEquals(person.no, personMap.get("no"));
			assertEquals(person.name, personMap.get("name"));
		}
	}
}

class ValueObject {
	public boolean booleanValue;

	public byte byteValue;

	public short shortValue;

	public int intValue;

	public long longValue;

	public BigInteger bigIntegerValue;

	public float floatValue;

	public double doubleValue;

	public BigDecimal bigDecimalValue;

	public String stringValue;

	public Calendar calendarValue;

	public Person[] personList;
}

class Person {
	public int no;

	public String name;
}