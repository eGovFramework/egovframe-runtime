package org.egovframe.rte.itl.integration.type;

import org.egovframe.rte.itl.integration.message.typed.TypedList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ListTypeTest
{

    @Test
    public void testIsAssignableFrom() throws Exception
    {
        ListType stringListType =
            new ListType("string[]", "string[]", PrimitiveType.STRING);
        assertTrue(stringListType.isAssignableFrom(TypedList.class));
        assertTrue(stringListType.isAssignableFrom(List.class));
        assertTrue(stringListType.isAssignableFrom(String[].class));
        assertFalse(stringListType.isAssignableFrom(Map.class));

        ListType stringListType2 =
            new ListType("string[][]", "string[][]", stringListType);
        assertTrue(stringListType2.isAssignableFrom(TypedList.class));
        assertTrue(stringListType2.isAssignableFrom(List.class));
        assertTrue(stringListType2.isAssignableFrom(String[][].class));
        assertFalse(stringListType2.isAssignableFrom(Map.class));
        assertFalse(stringListType2.isAssignableFrom(String[].class));

        ListType integerListType =
            new ListType("integer[]", "integer[]", PrimitiveType.INTEGER);
        assertTrue(integerListType.isAssignableFrom(int[].class));
        assertTrue(integerListType.isAssignableFrom(Integer[].class));
        assertTrue(integerListType.isAssignableFrom(byte[].class));
        assertTrue(integerListType.isAssignableFrom(short[].class));
        assertFalse(integerListType.isAssignableFrom(long[].class));
        assertFalse(integerListType.isAssignableFrom(float[].class));
    }

    @Test
    public void testIsAssigableValue() throws Exception
    {
        ListType stringListType =
            new ListType("string[]", "string[]", PrimitiveType.STRING);
//        TypedList typedStringList = new TypedList(stringListType)
//        {{
//            add("a");
//            add("b");
//            add("c");
//        }};
//      assertTrue(stringListType.isAssignableValue(typedStringList));
        List<String> stringList = new ArrayList<String>()
        {/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = 9045815271522604267L;

		{
            add("a");
            add("b");
            add("c");
        }};
        assertTrue(stringListType.isAssignableValue(stringList));
        assertTrue(stringListType.isAssignableValue(new String[] { "a", "b", "c" }));

        ListType integerListType =
            new ListType("integer[]", "integer[]", PrimitiveType.INTEGER);
        assertTrue(integerListType.isAssignableValue(new int[] { 1, 2, 3 }));
        assertTrue(integerListType.isAssignableValue(new byte[] { 1, 2, 3 }));
        assertFalse(integerListType.isAssignableValue(new long[] { 1L, 2L, 3L }));
    }

    private static boolean isUnassignableValue(Type type, Object value) throws Exception
    {
        try
        {
            type.convertToTypedObject(value);
        }
        catch (UnassignableValueException e)
        {
            return true;
        }
        return false;
    }

    @Test
    public void testConvertToTypedObject() throws Exception
    {
        ListType stringListType =
            new ListType("string[]", "string[]", PrimitiveType.STRING);

        String[] stringArray = new String[] { "a", "b", "c" };
        Object object = stringListType.convertToTypedObject(stringArray);
        assertNotNull(object);
        assertTrue(object instanceof TypedList);
        TypedList typedStringList = (TypedList)object;
        assertEquals(stringArray.length, typedStringList.size());
        for (int i = 0; i < stringArray.length; i++)
        {
            assertEquals(stringArray[i], typedStringList.get(i));
        }

        int[] intArray = new int[] { 1, 2, 3 };
        assertTrue(isUnassignableValue(stringListType, intArray));

        ListType integerListType =
            new ListType("integer[]", "integer[]", PrimitiveType.INTEGER);
        object = integerListType.convertToTypedObject(intArray);
        assertNotNull(object);
        assertTrue(object instanceof TypedList);
        TypedList typedIntegerList = (TypedList)object;
        assertEquals(intArray.length, typedIntegerList.size());
        for (int i = 0; i < intArray.length; i++)
        {
            assertEquals(intArray[i], typedIntegerList.get(i));
        }
    }
}
