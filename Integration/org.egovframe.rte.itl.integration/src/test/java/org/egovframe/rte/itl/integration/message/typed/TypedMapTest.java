package org.egovframe.rte.itl.integration.message.typed;

import org.egovframe.rte.itl.integration.type.*;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.junit.Assert.*;

public class TypedMapTest {

	private static final ListType stringListType = new ListType("string[]", "string[]", PrimitiveType.STRING);

	private static final RecordType recordType = new RecordType("recordA", "RecordA", new HashMap<String, Type>() {
		/**
		 *  serialVersion UID
		 */
		private static final long serialVersionUID = 3943829516089661787L;

		{
			put("intValue", PrimitiveType.INTEGER);
			put("booleanValue", PrimitiveType.BOOLEAN);
			put("stringArray", stringListType);
		}
	});

	@Test
	public void testCreate() throws Exception {
		// empty map
		new TypedMap(recordType);

		// create with Map
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		values.put("stringArray", new String[] { "a", "b", "c" });
		new TypedMap(recordType, values);

		// fails with TypeMismatched
		Map<String, Object> values2 = new HashMap<String, Object>();
		values2.put("intValue", 1);
		values2.put("booleanValue", "false");
		values2.put("stringArray", new String[] { "a", "b", "c" });
		try {
			new TypedMap(recordType, values2);
			fail();
		} catch (UnassignableValueException e) {
		}

		// fail with no field definition
		Map<String, Object> values3 = new HashMap<String, Object>();
		values3.put("intValue", 1);
		values3.put("longValue", 52L);
		values3.put("stringArray", new String[] { "a", "b", "c" });
		try {
			new TypedMap(recordType, values3);
			fail();
		} catch (NoSuchRecordFieldException e) {
		}
	}

	@Test
	public void testClear() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		values.put("stringArray", new String[] { "a", "b", "c" });
		TypedMap map = new TypedMap(recordType, values);

		map.clear();
		assertEquals(0, map.size());
	}

	@Test
	public void testContainsKey() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		values.put("stringArray", new String[] { "a", "b", "c" });
		TypedMap map = new TypedMap(recordType, values);

		assertTrue(map.containsKey("intValue"));
		assertTrue(map.containsKey("booleanValue"));
		assertFalse(map.containsKey("longValue"));
		assertFalse(map.containsKey(null));
	}

	@Test
	public void testContainsValue() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		String[] stringArray = new String[] { "a", "b", "c" };
		values.put("stringArray", stringArray);
		TypedMap map = new TypedMap(recordType, values);

		assertTrue(map.containsValue(1));
		assertTrue(map.containsValue(true));
		assertTrue(map.containsValue(new TypedList(stringListType, stringArray)));
		assertFalse(map.containsValue(1L)); // Long
		assertFalse(map.containsValue(null));
	}

	@Test
	public void testEntrySet() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		String[] stringArray = new String[] { "a", "b", "c" };
		values.put("stringArray", stringArray);
		TypedMap map = new TypedMap(recordType, values);

		Set<Entry<String, Object>> entrySet = map.entrySet();
		assertEquals(3, entrySet.size());
		// intValue
		for (Entry<String, Object> entry : entrySet) {
			if (entry.getKey().equals("intValue")) {
				assertEquals(1, entry.getValue());
			}
		}
		// booleanValue
		for (Entry<String, Object> entry : entrySet) {
			if (entry.getKey().equals("booleanValue")) {
				assertEquals(true, entry.getValue());
			}
		}
		// stringArray
		for (Entry<String, Object> entry : entrySet) {
			if (entry.getKey().equals("stringArray")) {
				assertEquals(new TypedList(stringListType, stringArray), entry.getValue());
			}
		}
	}

	@Test
	public void testEquals() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		String[] stringArray = new String[] { "a", "b", "c" };
		values.put("stringArray", stringArray);
		TypedMap map = new TypedMap(recordType, values);

		TypedMap map2 = new TypedMap(recordType);
		map2.put("intValue", 1);
		map2.put("booleanValue", true);
		map2.put("stringArray", stringArray);

		assertTrue(map.equals(map2));
		assertTrue(map2.equals(map));
	}

	@Test
	public void testGet() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		String[] stringArray = new String[] { "a", "b", "c" };
		values.put("stringArray", stringArray);
		TypedMap map = new TypedMap(recordType, values);

		assertEquals(1, map.get("intValue"));
		assertEquals(true, map.get("booleanValue"));
		try {
			map.get("longValue");
			fail();
		} catch (NoSuchRecordFieldException e) {
		}
	}

	@Test
	public void testIsEmpty() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		String[] stringArray = new String[] { "a", "b", "c" };
		values.put("stringArray", stringArray);
		TypedMap map = new TypedMap(recordType, values);

		assertFalse(map.isEmpty());
		assertTrue(new TypedMap(recordType).isEmpty());
	}

	@Test
	public void testKeySet() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		String[] stringArray = new String[] { "a", "b", "c" };
		values.put("stringArray", stringArray);
		TypedMap map = new TypedMap(recordType, values);

		Set<String> keySet = map.keySet();
		assertEquals(3, keySet.size());
		assertTrue(keySet.contains("intValue"));
		assertTrue(keySet.contains("booleanValue"));
		assertTrue(keySet.contains("stringArray"));
	}

	@Test
	public void testPut() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		String[] stringArray = new String[] { "a", "b", "c" };
		values.put("stringArray", stringArray);
		TypedMap map = new TypedMap(recordType, values);

		assertEquals(1, map.put("intValue", 2));
		assertEquals(2, map.get("intValue"));
		try {
			map.put("longValue", 5L);
			fail();
		} catch (NoSuchRecordFieldException e) {
		}

		try {
			map.put("booleanValue", "true");
			fail();
		} catch (UnassignableValueException e) {
		}
	}

	@Test
	public void testPutAll() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		String[] stringArray = new String[] { "a", "b", "c" };
		values.put("stringArray", stringArray);
		TypedMap map = new TypedMap(recordType, values);

		map.putAll(new HashMap<String, Object>() {
			/**
			 *  serialVersionUID
			 */
			private static final long serialVersionUID = -8115611546753861314L;

			{
				put("intValue", 2);
				put("booleanValue", false);
			}
		});
		assertEquals(3, map.size());
		assertEquals(2, map.get("intValue"));
		assertEquals(false, map.get("booleanValue"));

		try {
			map.putAll(new HashMap<String, Object>() {
				/**
				 *  serialVersion UID
				 */
				private static final long serialVersionUID = 736865797008997634L;

				{
					put("intValue", 3);
					put("longValue", 5L);
				}
			});
			fail();
		} catch (NoSuchRecordFieldException e) {
		}

		try {
			map.putAll(new HashMap<String, Object>() {
				/**
				 *  serialVersion UID
				 */
				private static final long serialVersionUID = 4403233078265485312L;

				{
					put("intValue", 6L);
				}
			});
			fail();
		} catch (UnassignableValueException e) {
		}
	}

	@Test
	public void testRemove() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		String[] stringArray = new String[] { "a", "b", "c" };
		values.put("stringArray", stringArray);
		TypedMap map = new TypedMap(recordType, values);

		assertEquals(1, map.remove("intValue"));
		assertEquals(2, map.size());

		try {
			map.remove("longValue");
			fail();
		} catch (NoSuchRecordFieldException e) {
		}
	}

	@Test
	public void testSize() throws Exception {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("intValue", 1);
		values.put("booleanValue", true);
		String[] stringArray = new String[] { "a", "b", "c" };
		values.put("stringArray", stringArray);
		TypedMap map = new TypedMap(recordType, values);

		assertEquals(3, map.size());
	}

	@Test
	public void testValues() throws Exception {
		Map<String, Object> initialValues = new HashMap<String, Object>();
		initialValues.put("intValue", 1);
		initialValues.put("booleanValue", true);
		String[] stringArray = new String[] { "a", "b", "c" };
		initialValues.put("stringArray", stringArray);
		TypedMap map = new TypedMap(recordType, initialValues);

		Collection<Object> values = map.values();
		assertEquals(3, values.size());
		assertTrue(values.contains(1));
		assertTrue(values.contains(true));
		assertTrue(values.contains(new TypedList(stringListType, stringArray)));
	}
}
