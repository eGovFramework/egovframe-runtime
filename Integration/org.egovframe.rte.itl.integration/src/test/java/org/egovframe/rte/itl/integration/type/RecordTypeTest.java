package org.egovframe.rte.itl.integration.type;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.egovframe.rte.itl.integration.message.typed.TypedMap;

import org.junit.Test;

public class RecordTypeTest {

	private static final ListType stringListType = new ListType("string[]", "string[]", PrimitiveType.STRING);

	private static final RecordType recordTypeA = new RecordType("A", "A", new HashMap<String, Type>() {
		/**
		 *  serialVersion UID
		 */
		private static final long serialVersionUID = 1003291813408378852L;

		{
			put("a", PrimitiveType.STRING);
			put("b", PrimitiveType.INTEGER);
		}
	});

	private static final RecordType recordTypeB = new RecordType("B", "B", new HashMap<String, Type>() {
		/**
		 *  serialVersion UID
		 */
		private static final long serialVersionUID = -2204589011002849636L;

		{
			put("c", PrimitiveType.BOOLEAN);
			put("d", stringListType);
		}
	});

	private static final RecordType recordTypeC = new RecordType("C", "C", new HashMap<String, Type>() {
		/**
		 *  serialVersion UID
		 */
		private static final long serialVersionUID = 198137443349401515L;

		{
			put("e", recordTypeA);
			put("f", PrimitiveType.CALENDAR);
		}
	});

	@Test
	public void testIsAssignableFrom() throws Exception {
		assertTrue(recordTypeA.isAssignableFrom(TypedMap.class));
		assertTrue(recordTypeA.isAssignableFrom(Map.class));
		assertFalse(recordTypeA.isAssignableFrom(Iterable.class));
	}

	@Test
	public void testIsAssignableValue() throws Exception {
		final Map<String, Object> valueA1 = new HashMap<String, Object>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = 3284655428518660243L;

			{
				put("a", "valueA");
				put("b", 1);
			}
		};
		final Map<String, Object> valueA2 = new HashMap<String, Object>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = 6776775395656328695L;

			{
				put("a", "valueA");
			}
		};
		final Map<String, Object> valueA3 = new HashMap<String, Object>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = -5765762438505921584L;

			{
				put("a", "valueA");
				put("c", true);
			}
		};
		assertTrue(recordTypeA.isAssignableValue(valueA1));
		assertTrue(recordTypeA.isAssignableValue(valueA2));
		try {
			recordTypeA.isAssignableValue(valueA3);
			fail();
		} catch (NoSuchRecordFieldException e) {
		}

		final Map<String, Object> valueB1 = new HashMap<String, Object>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = -3644426118423305930L;

			{
				put("c", true);
				put("d", new String[] { "x", "y", "z" });
			}
		};
		final Map<String, Object> valueB2 = new HashMap<String, Object>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = -185879951518993662L;

			{
				put("c", true);
				put("d", new String[] {});
			}
		};
		final Map<String, Object> valueB3 = new HashMap<String, Object>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = -6515925286263685839L;

			{
				put("c", true);
				put("d", null);
			}
		};
		assertTrue(recordTypeB.isAssignableValue(valueB1));
		assertTrue(recordTypeB.isAssignableValue(valueB2));
		assertTrue(recordTypeB.isAssignableValue(valueB3));

		final Map<String, Object> valueC1 = new HashMap<String, Object>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = -1730034652757831321L;

			{
				put("e", valueA1);
				put("f", Calendar.getInstance());
			}
		};
		final Map<String, Object> valueC2 = new HashMap<String, Object>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = -811765504132330071L;

			{
				put("e", valueB1);
				put("f", Calendar.getInstance());
			}
		};
		assertTrue(recordTypeC.isAssignableValue(valueC1));
		try {
			recordTypeC.isAssignableValue(valueC2);
			fail();
		} catch (NoSuchRecordFieldException e) {
		}
	}
}
