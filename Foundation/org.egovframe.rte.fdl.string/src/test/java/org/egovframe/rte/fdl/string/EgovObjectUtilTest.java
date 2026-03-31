package org.egovframe.rte.fdl.string;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class EgovObjectUtilTest {

    @Test
    public void testIsNull() {
        assertFalse(EgovObjectUtil.isNull(new Object()));

        assertTrue(EgovObjectUtil.isNull(null));
    }

    @Test
    public void testIsEmpty() {
        ArrayList<String> list = new ArrayList<>();
        list.add("12124");

        assertFalse(EgovObjectUtil.isEmpty(new Object()));
        assertTrue(EgovObjectUtil.isEmpty(null));
        assertFalse(EgovObjectUtil.isEmpty(list));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadClass() throws ClassNotFoundException {
        String className = "org.egovframe.rte.fdl.string.EgovStringUtil";
        String wrongClassName = "org.egovframe.rte.fdl.string.EgovStringUtil1";

        Class<?> clazz = EgovObjectUtil.loadClass(className);
        assertNotNull(clazz);

        clazz = null;
        assertNull(clazz);

        Class<Exception> exceptionClass = null;

        try {
            clazz = EgovObjectUtil.loadClass(wrongClassName);
        } catch (Exception e) {
            exceptionClass = (Class<Exception>) e.getClass();
        } finally {
            assertEquals(ClassNotFoundException.class, exceptionClass);
            assertNotSame(InstantiationException.class, exceptionClass);
            assertNotSame(IllegalAccessException.class, exceptionClass);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInstantiate() {
        String className = "java.lang.String";
        String wrongClassName = "java.lang.String1";

        Object object = EgovObjectUtil.instantiate(className);
        assertNotNull(object);

        String string = "eGovFramework";
        assertEquals("Framework", string.substring(4));

        object = null;
        assertNull(object);

        Class<Exception> exceptionClass = null;

        try {
            object = EgovObjectUtil.instantiate(wrongClassName);
        } catch (Exception e) {
            exceptionClass = (Class<Exception>) e.getClass();
        } finally {
            assertEquals(RuntimeException.class, exceptionClass);
            assertNotSame(InstantiationException.class, exceptionClass);
            assertNotSame(IllegalAccessException.class, exceptionClass);
        }
    }

    @Test
    public void testInstantiateParamConstructor() {
        String className = "java.lang.StringBuffer";
        String[] types = new String[]{"java.lang.String"};
        Object[] values = new Object[]{"전자정부 공통서비스"};

        StringBuffer sb = (StringBuffer) EgovObjectUtil.instantiate(className, types, values);
        sb.append(" 및 개발프레임워크 구축 사업");

        assertEquals("전자정부 공통서비스 및 개발프레임워크 구축 사업", sb.toString());
    }
}
