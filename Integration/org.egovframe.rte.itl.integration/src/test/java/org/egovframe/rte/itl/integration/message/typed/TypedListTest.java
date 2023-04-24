package org.egovframe.rte.itl.integration.message.typed;

import org.egovframe.rte.itl.integration.type.ListType;
import org.egovframe.rte.itl.integration.type.PrimitiveType;
import org.egovframe.rte.itl.integration.type.UnassignableValueException;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


public class TypedListTest
{

    private static ListType stringListType =
        new ListType("string[]", "string[]", PrimitiveType.STRING);
    
    @Test
    public void testCreation() throws Exception
    {
        // Empty List
        List<Object> emptyList = new TypedList(stringListType);
        assertEquals(0, emptyList.size());
        
        // With Array
        String[] strings = new String[] { "a", "b", "c" };
        List<Object> listWithArray =
            new TypedList(stringListType, strings);
        assertEquals(strings.length, listWithArray.size());
        for (int i = 0; i < strings.length; i++)
        {
            assertEquals(strings[i], listWithArray.get(i));
        }
        
        // With Collection
        Collection<String> c = Arrays.asList(new String[] { "a", "b", "c" });
        List<Object> listWithCollection = new TypedList(stringListType, c);
        assertEquals(c.size(), listWithCollection.size());
        for (Iterator<? extends Object> i = c.iterator(), j = listWithCollection.iterator();
            i.hasNext() && j.hasNext();)
        {
            assertEquals(i.next(), j.next());
        }

        // Fails with Null Type
        try
        {
            new TypedList(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }

        // Fails with Null Argument 
        try
        {
            new TypedList(stringListType, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
        
        // Fails with TypeMismatched
        try
        {
            new TypedList(stringListType,
                    Arrays.asList(new int[] { 1, 2, 3 }));
            fail();
        }
        catch (UnassignableValueException e)
        {
        }
    }
    
    @Test
    public void testAdd() throws Exception
    {
        List<Object> list = new TypedList(stringListType);
        
        // append to empty list
        String a = "a";
        assertTrue(list.add(a));
        assertEquals(1, list.size());
        assertEquals(a, list.get(0));
        
        // append to the end of list
        String b = "b";
        assertTrue(list.add(b));
        assertEquals(2, list.size());
        assertEquals(a, list.get(0));
        assertEquals(b, list.get(1));
        
        // append fails with TypeMismatched
        try
        {
            list.add(1);
            fail();
        }
        catch (UnassignableValueException e)
        {
        }
        
        // insert
        String c = "c";
        list.add(1, c);
        assertEquals(3, list.size());
        assertEquals(a, list.get(0));
        assertEquals(c, list.get(1));
        assertEquals(b, list.get(2));
        
        // insert fails with IndexOutOfBounds
        String d = "d";
        try
        {
            list.add(-1, d);
            fail();
        }
        catch (IndexOutOfBoundsException e)
        {
        }
        try
        {
            list.add(list.size() + 1, d);
            fail();
        }
        catch (IndexOutOfBoundsException e)
        {
        }
    }
    
    @Test
    public void testAddAll() throws Exception
    {
        List<Object> list = new TypedList(stringListType);
        
        // append all to empty list
        String[] strings = new String[] { "a", "b", "c" };
        assertTrue(list.addAll(Arrays.asList(strings)));
        assertEquals(strings.length, list.size());
        for (int i = 0; i < strings.length; i++)
        {
            assertEquals(strings[i], list.get(i));
        }
        
        // append nothing
        assertFalse(list.addAll(Arrays.asList(new String[] {})));
        
        // append all fails with null
        try
        {
            list.addAll(null);
            fail();
        }
        catch (NullPointerException e)
        {
        }
        
        // append all fails with TypeMismatched
        try
        {
            list.addAll(Arrays.asList(new Integer[] { 1, 2, 3 }));
            fail();
        }
        catch (UnassignableValueException e)
        {
        }
        
        // insert all to the list
        assertTrue(list.addAll(2, Arrays.asList(strings)));
        assertEquals(strings.length * 2, list.size());
        assertEquals(strings[0], list.get(0));
        assertEquals(strings[1], list.get(1));
        assertEquals(strings[0], list.get(2));  // inserted
        assertEquals(strings[1], list.get(3));  // inserted
        assertEquals(strings[2], list.get(4));  // inserted
        assertEquals(strings[2], list.get(5));
        
        // insert nothing
        assertFalse(list.addAll(2, Arrays.asList(new String[] {})));
        
        // insert all fails with null
        try
        {
            list.addAll(0, null);
            fail();
        }
        catch (NullPointerException e)
        {
        }
        
        // insert all fails with IndexOutOfBounds
        try
        {
            list.addAll(-1, Arrays.asList(strings));
        }
        catch (IndexOutOfBoundsException e)
        {
        }
        try
        {
            list.addAll(list.size() + 1, Arrays.asList(strings));
        }
        catch (IndexOutOfBoundsException e)
        {
        }
        
        // insert all fails with TypeMismatched
        try
        {
            list.addAll(0, Arrays.asList(new Object[] { "string", 1 }));
        }
        catch (UnassignableValueException e)
        {
        }
    }

    @Test
    public void testClear() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c" };
        List<Object> list = new TypedList(stringListType, strings);
        
        list.clear();
        assertEquals(0, list.size());
    }
    
    @Test
    public void testContains() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "d" };
        List<Object> list = new TypedList(stringListType, strings);

        for (String s : strings)
        {
            assertTrue(list.contains(s));
        }
        assertTrue(list.contains(null));
        assertFalse(list.contains("e"));
    }
    
    @Test
    public void testContainsAll() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        assertTrue(list.containsAll(Arrays.asList(strings)));
        assertTrue(list.containsAll(Arrays.asList(new String[] { "a", "b", null })));
        assertFalse(list.containsAll(Arrays.asList(new String[] { "a", "e" })));
        assertFalse(list.containsAll(Arrays.asList(new Object[] { "a", 1 })));
        
        // fails with null
        try
        {
            list.containsAll(null);
            fail();
        }
        catch (NullPointerException e)
        {
        }
    }
    
    @Test
    public void testEquals() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "d" };
        List<Object> list1 = new TypedList(stringListType, strings);
        
        List<Object> list2 = new TypedList(stringListType);
        for (String s : strings)
        {
            list2.add(s);
        }
        assertTrue(list1.equals(list2));
        assertTrue(list2.equals(list1));
        
        List<Object> list3 = new TypedList(stringListType);
        for (String s : strings)
        {
            list3.add(0, s);
        }
        assertFalse(list1.equals(list3));
        assertFalse(list3.equals(list1));
    }
    
    @Test
    public void testGet() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        for (int i = 0; i < strings.length; i++)
        {
            assertEquals(strings[i], list.get(i));
        }
        
        // fails with IndexOutOfBounds
        try
        {
            list.get(-1);
            fail();
        }
        catch (IndexOutOfBoundsException e)
        {
        }
        try
        {
            list.get(list.size());
            fail();
        }
        catch (IndexOutOfBoundsException e)
        {
        }
    }
    
    @Test
    public void testIndexOf() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        assertEquals(0, list.indexOf("a"));
        assertEquals(1, list.indexOf("b"));
        assertEquals(2, list.indexOf("c"));
        assertEquals(3, list.indexOf(null));
        assertEquals(7, list.indexOf("d"));
        assertEquals(-1, list.indexOf("e"));
    }

    @Test
    public void testIsEmpty() throws Exception
    {
        assertTrue(new TypedList(stringListType).isEmpty());
        assertFalse(new TypedList(stringListType, new String[] { "a" }).isEmpty());
    }

    @Test
    public void testIterator() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        Iterator<Object> i = list.iterator();
        // [ a b c null a b c d ]
        //  ^
        assertTrue(i.hasNext());
        assertEquals("a", i.next());
        // [ a b c null a b c d ]
        //    ^
        assertEquals("b", i.next());
        // [ a b c null a b c d ]
        //      ^
        i.remove();
        // [ a c null a b c d ]
        //    ^
        assertEquals("c", i.next());
        // [ a c null a b c d ]
        //      ^
        assertEquals(null, i.next());
        // [ a c null a b c d ]
        //           ^
        assertEquals("a", i.next());
        // [ a c null a b c d ]
        //             ^
        assertEquals("b", i.next());
        // [ a c null a b c d ]
        //               ^
        i.remove();
        // [ a b null a c d ]
        //             ^
        assertEquals("c", i.next());
        // [ a b null a c d ]
        //               ^
        assertEquals("d", i.next());
        // [ a b null a c d ]
        //                 ^
        assertFalse(i.hasNext());
        try
        {
            i.next();
            fail();
        }
        catch (NoSuchElementException e)
        {
        }
    }
    
    @Test
    public void testLastIndexOf() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        assertEquals(4, list.lastIndexOf("a"));
        assertEquals(5, list.lastIndexOf("b"));
        assertEquals(6, list.lastIndexOf("c"));
        assertEquals(3, list.lastIndexOf(null));
        assertEquals(7, list.lastIndexOf("d"));
        assertEquals(-1, list.lastIndexOf("e"));
    }

    @Test
    public void testListIterator() throws Exception
    {
        // see TypedListIteratorTest
    }
    
    @Test
    public void testRemoveWithIndex() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        assertEquals(strings[4], list.remove(4));
        assertEquals(strings.length - 1, list.size());
        assertEquals(strings[0], list.get(0));
        assertEquals(strings[1], list.get(1));
        assertEquals(strings[2], list.get(2));
        assertEquals(strings[3], list.get(3));
        assertEquals(strings[5], list.get(4));
        assertEquals(strings[6], list.get(5));
        assertEquals(strings[7], list.get(6));
        
        try
        {
            list.remove(-1);
            fail();
        }
        catch (IndexOutOfBoundsException e)
        {
        }
        try
        {
            list.remove(list.size());
            fail();
        }
        catch (IndexOutOfBoundsException e)
        {
        }
    }
    
    @Test
    public void testRemoveWithObject() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        assertTrue(list.remove("a"));
        assertEquals(strings.length - 1, list.size());
        assertEquals(strings[1], list.get(0));
        assertEquals(strings[2], list.get(1));
        assertEquals(strings[3], list.get(2));
        assertEquals(strings[4], list.get(3));
        assertEquals(strings[5], list.get(4));
        assertEquals(strings[6], list.get(5));
        assertEquals(strings[7], list.get(6));
        
        assertTrue(list.remove(null));
        assertEquals(strings.length - 2, list.size());
        assertEquals(strings[1], list.get(0));
        assertEquals(strings[2], list.get(1));
        assertEquals(strings[4], list.get(2));
        assertEquals(strings[5], list.get(3));
        assertEquals(strings[6], list.get(4));
        assertEquals(strings[7], list.get(5));
        
        assertFalse(list.remove("e"));
    }
    
    @Test
    public void testRemoveAll() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        Collection<String> c = Arrays.asList(new String[] { "a", "b" });
        assertTrue(list.removeAll(c));
        // [ c null c d ]
        assertEquals("c", list.get(0));
        assertEquals(null, list.get(1));
        assertEquals("c", list.get(2));
        assertEquals("d", list.get(3));
        
        // nothing to be removed
        assertFalse(list.removeAll(c));
        
        // fails with null
        try
        {
            list.removeAll(null);
            fail();
        }
        catch (NullPointerException e)
        {
        }
    }
    
    @Test
    public void testRetainAll() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        Collection<String> c = Arrays.asList(new String[] { "a", "b" });
        assertTrue(list.retainAll(c));
        // [ a b a b ]
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("a", list.get(2));
        assertEquals("b", list.get(3));
        
        // nothing to be removed
        assertFalse(list.retainAll(c));
        
        // fails with null
        try
        {
            list.retainAll(null);
            fail();
        }
        catch (NullPointerException e)
        {
        }
    }
    
    @Test
    public void testSet() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        assertEquals(strings[4], list.set(4, "e"));
        // [ a b c null e b c d ]
        assertEquals(strings.length, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        assertEquals(null, list.get(3));
        assertEquals("e", list.get(4));
        assertEquals("b", list.get(5));
        assertEquals("c", list.get(6));
        assertEquals("d", list.get(7));
        
        assertEquals(strings[0], list.set(0, null));
        // [ null b c null e b c d]
        assertEquals(strings.length, list.size());
        assertEquals(null, list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        assertEquals(null, list.get(3));
        assertEquals("e", list.get(4));
        assertEquals("b", list.get(5));
        assertEquals("c", list.get(6));
        assertEquals("d", list.get(7));
        
        // fails with TypeMismatched
        try
        {
            list.set(0, 1);
            fail();
        }
        catch (UnassignableValueException e)
        {
        }
        
        // fails with IndexOutOfBoundsException
        try
        {
            list.set(-1, "a");
            fail();
        }
        catch (IndexOutOfBoundsException e)
        {
        }
        try
        {
            list.set(list.size(), "a");
            fail();
        }
        catch (IndexOutOfBoundsException e)
        {
        }
    }
    
    @Test
    public void testSize() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        assertEquals(strings.length, list.size());
    }
    
    @Test
    public void testSubList() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        // empty sublist
        assertEquals(0, list.subList(4, 4).size());
        
        // sublist
        int fromIndex = 1;
        int toIndex = 3;
        List<Object> subList = list.subList(fromIndex, toIndex);
        // [ a [ b c ] null a b c d ]
        assertEquals(toIndex - fromIndex, subList.size());
        for (int i = fromIndex; i < toIndex; i++)
        {
            assertEquals(strings[i], subList.get(i - fromIndex));
        }

        // add
        subList.add("e");
        // [ a [ b c e ] null a b c d ]
        assertEquals(3, subList.size());
        assertEquals("b", subList.get(0));
        assertEquals("c", subList.get(1));
        assertEquals("e", subList.get(2));
        assertEquals(9, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        assertEquals("e", list.get(3));
        assertEquals(null, list.get(4));
        assertEquals("a", list.get(5));
        assertEquals("b", list.get(6));
        assertEquals("c", list.get(7));
        assertEquals("d", list.get(8));

        // remove
        subList.remove("b");
        // [ a [ c e ] null a b c d ]
        assertEquals(2, subList.size());
        assertEquals("c", subList.get(0));
        assertEquals("e", subList.get(1));
        assertEquals(8, list.size());
        assertEquals("a", list.get(0));
        assertEquals("c", list.get(1));
        assertEquals("e", list.get(2));
        assertEquals(null, list.get(3));
        assertEquals("a", list.get(4));
        assertEquals("b", list.get(5));
        assertEquals("c", list.get(6));
        assertEquals("d", list.get(7));
        
        // clear
        subList.clear();
        // [ a null a b c d ]
        assertEquals(0, subList.size());
        assertEquals(6, list.size());
        assertEquals("a", list.get(0));
        assertEquals(null, list.get(1));
        assertEquals("a", list.get(2));
        assertEquals("b", list.get(3));
        assertEquals("c", list.get(4));
        assertEquals("d", list.get(5));
    }
    
    @Test
    public void testToArray() throws Exception
    {
        String[] strings = new String[] { "a", "b", "c", null, "a", "b", "c", "d" };
        List<Object> list = new TypedList(stringListType, strings);
        
        Object[] array = list.toArray();
        assertNotNull(array);
        assertEquals(strings.length, array.length);
        for (int i = 0; i < strings.length; i++)
        {
            assertEquals(strings[i], array[i]);
        }
        
        String[] stringArray = list.toArray(new String[0]);
        assertNotNull(stringArray);
        assertEquals(strings.length, stringArray.length);
        for (int i = 0; i < strings.length; i++)
        {
            assertEquals(strings[i], stringArray[i]);
        }
        
        // Un Assignable
        try
        {
            list.toArray(new Integer[0]);
            fail();
        }
        catch (ArrayStoreException e)
        {
        }
        
        // null
        try
        {
            list.toArray((Object[])null);
            fail();
        }
        catch (NullPointerException e)
        {
        }
    }
    
    @Test
    public void testRetrieve() throws Exception
    {
        String[] stringArray = new String[] { "a", "b", "c", "d", "a", "b" };
        TypedList stringTypedList = new TypedList(stringListType, stringArray);
        
        // size
        assertEquals(stringArray.length, stringTypedList.size());
        
        // get
        int i = 0;
        for (i = 0; i < stringArray.length; i++)
        {
            assertEquals(stringArray[i], stringTypedList.get(i));
        }
        
        // iterator
        i = 0;
        for (Object o : stringTypedList)
        {
            assertEquals(stringArray[i], o);
            i++;
        }
        
        // iterator
        i = 0;
        for (Iterator<?> it = stringTypedList.iterator(); it.hasNext(); )
        {
            assertEquals(stringArray[i], it.next());
            i++;
        }
        
        // contains
        for (i = 0; i < stringArray.length; i++)
        {
            assertTrue(stringTypedList.contains(stringArray[i]));
        }
        assertFalse(stringTypedList.contains("x"));
        assertFalse(stringTypedList.contains(1));
        
        // containsAll
        assertTrue(stringTypedList.containsAll(Arrays.asList(new String[] { "a", "b" })));
        assertFalse(stringTypedList.containsAll(Arrays.asList(new String[] { "a", "b", "x" })));
        assertFalse(stringTypedList.containsAll(Arrays.asList(new Object[] { "a", 1 })));
        
        // indexOf
        assertEquals(0, stringTypedList.indexOf("a"));
        assertEquals(1, stringTypedList.indexOf("b"));
        assertEquals(2, stringTypedList.indexOf("c"));
        assertEquals(3, stringTypedList.indexOf("d"));
        assertEquals(-1, stringTypedList.indexOf("x"));
        
        // isEmpty
        assertTrue(new TypedList(stringListType).isEmpty());
        assertFalse(stringTypedList.isEmpty());
        
        // lastIndexOf
        assertEquals(4, stringTypedList.lastIndexOf("a"));
        assertEquals(5, stringTypedList.lastIndexOf("b"));
        assertEquals(2, stringTypedList.lastIndexOf("c"));
        assertEquals(3, stringTypedList.lastIndexOf("d"));
        
        // subList
        Object object = stringTypedList.subList(1, 3);
        assertNotNull(object);
        assertTrue(object instanceof TypedList);
        TypedList subList = (TypedList)object;
        assertEquals(stringListType, subList.getType());
        assertEquals(2, subList.size());
        i = 1;
        for (Object o : subList)
        {
            assertEquals(stringArray[i], o);
            i++;
        }
        
        // toArray
        Object[] array = stringTypedList.toArray();
        assertNotNull(array);
        assertEquals(stringArray.length, array.length);
        for (i = 0; i < stringArray.length; i++)
        {
            assertEquals(stringArray[i], array[i]);
        }
        
        // toArray(T[] a)
        String[] stringArray2 = stringTypedList.toArray(new String[] {});
        assertNotNull(stringArray2);
        assertEquals(stringArray.length, stringArray2.length);
        for (i = 0; i < stringArray.length; i++)
        {
            assertEquals(stringArray[i], stringArray2[i]);
        }
    }
    
    @Test
    public void testModify() throws Exception
    {
        TypedList stringTypedList = new TypedList(stringListType);
        assertEquals(0, stringTypedList.size());
        
        // add
        stringTypedList.add("a");
        assertEquals(1, stringTypedList.size());
        assertEquals("a", stringTypedList.get(0));
        
        try
        {
            stringTypedList.add(1);
            fail();
        }
        catch (UnassignableValueException e)
        {
        }
        
        stringTypedList.add("b");
        assertEquals(2, stringTypedList.size());
        assertEquals("a", stringTypedList.get(0));
        assertEquals("b", stringTypedList.get(1));
        
        // add(int index, E element)
        stringTypedList.add(0, "x");
        assertEquals(3, stringTypedList.size());
        assertEquals("x", stringTypedList.get(0));
        assertEquals("a", stringTypedList.get(1));
        assertEquals("b", stringTypedList.get(2));

        // addAll
        stringTypedList.addAll(Arrays.asList(new String[] { "c", "d" }));
        assertEquals(5, stringTypedList.size());
        assertEquals("x", stringTypedList.get(0));
        assertEquals("a", stringTypedList.get(1));
        assertEquals("b", stringTypedList.get(2));
        assertEquals("c", stringTypedList.get(3));
        assertEquals("d", stringTypedList.get(4));
        
        // addAll(int index, Collection<? extends E> c)
        stringTypedList.addAll(1, Arrays.asList(new String[] { "q", "r" }));
        assertEquals(7, stringTypedList.size());
        assertEquals("x", stringTypedList.get(0));
        assertEquals("q", stringTypedList.get(1));
        assertEquals("r", stringTypedList.get(2));
        assertEquals("a", stringTypedList.get(3));
        assertEquals("b", stringTypedList.get(4));
        assertEquals("c", stringTypedList.get(5));
        assertEquals("d", stringTypedList.get(6));
        
        // clear
        stringTypedList.clear();
        assertEquals(0, stringTypedList.size());

        stringTypedList.addAll(Arrays.asList(new String[] { "a", "b", "c", "d", "a", "b", "c", "d" }));
        // remove
        stringTypedList.remove(5);
        assertEquals(7, stringTypedList.size());
        assertEquals("a", stringTypedList.get(0));
        assertEquals("b", stringTypedList.get(1));
        assertEquals("c", stringTypedList.get(2));
        assertEquals("d", stringTypedList.get(3));
        assertEquals("a", stringTypedList.get(4));
        assertEquals("c", stringTypedList.get(5));
        assertEquals("d", stringTypedList.get(6));
        
        // remove(Object o)
        stringTypedList.remove("c");
        assertEquals(6, stringTypedList.size());
        assertEquals("a", stringTypedList.get(0));
        assertEquals("b", stringTypedList.get(1));
        assertEquals("d", stringTypedList.get(2));
        assertEquals("a", stringTypedList.get(3));
        assertEquals("c", stringTypedList.get(4));
        assertEquals("d", stringTypedList.get(5));
        
        // removeAll
        stringTypedList.removeAll(Arrays.asList(new String[] { "a", "b" }));
        assertEquals(3, stringTypedList.size());
        assertEquals("d", stringTypedList.get(0));
        assertEquals("c", stringTypedList.get(1));
        assertEquals("d", stringTypedList.get(2));
        
        // retainAll
        stringTypedList.retainAll(Arrays.asList(new String[] { "a", "b", "c" }));
        assertEquals(1, stringTypedList.size());
        assertEquals("c", stringTypedList.get(0));
        
        // set
        stringTypedList.set(0, "x");
        assertEquals(1, stringTypedList.size());
        assertEquals("x", stringTypedList.get(0));
        
        // listIterator
        ListIterator<Object> listIterator = stringTypedList.listIterator();
        
        // [ x ]
        //  ^
        
        // listIterator.next
        assertEquals("x", listIterator.next());
        
        // [ (x) ]
        //      ^
        
        // listIterator.add
        listIterator.add("y");
        assertEquals(2, stringTypedList.size());
        assertEquals("x", stringTypedList.get(0));
        assertEquals("y", stringTypedList.get(1));
        
        // [ x y ]
        //      ^
        
        // listIterator.hasNext
        assertFalse(listIterator.hasNext());
        
        // listIterator.hasPrevious
        assertTrue(listIterator.hasPrevious());
        
        // listIterator.nextIndex
        assertEquals(2, listIterator.nextIndex());
        
        // listIterator.previousIndex
        assertEquals(1, listIterator.previousIndex());
        
        // listIterator.remove
        try
        {
            listIterator.remove();
            fail();
        }
        catch (IllegalStateException e)
        {
        }
        
        // [ x y ]
        //      ^
        
        // listIterator.previous
        assertEquals("y", listIterator.previous());
        
        // [ x (y) ]
        //    ^
        
        // listIterator.remove (again)
        listIterator.remove();
        assertEquals(1, stringTypedList.size());
        assertEquals("x", stringTypedList.get(0));

        // [ x ]
        //    ^
        
        // listIterator.set
        try
        {
            listIterator.set("z");
            fail();
        }
        catch (IllegalStateException e)
        {
        }
        assertEquals("x", listIterator.previous());
        
        // [ (x) ]
        //  ^
        
        listIterator.set("z");
        assertEquals(1, stringTypedList.size());
        assertEquals("z", stringTypedList.get(0));

        // [ (z) ]
        //  ^
    }
}
