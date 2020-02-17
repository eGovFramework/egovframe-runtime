package egovframework.rte.itl.integration.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class PrimitiveTypeTest
{

    @Test
    public void testIsAssignableFrom() throws Exception
    {
        // BOOLEAN
        assertTrue(PrimitiveType.BOOLEAN.isAssignableFrom(boolean.class));
        assertTrue(PrimitiveType.BOOLEAN.isAssignableFrom(Boolean.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(String.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(byte.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(Byte.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(short.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(Short.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(int.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(Integer.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(long.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(Long.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(BigInteger.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(float.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(Float.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(double.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(Double.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(BigDecimal.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(Date.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(Calendar.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(Object.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(char.class));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableFrom(Character.class));

        // STRING
        assertFalse(PrimitiveType.STRING.isAssignableFrom(boolean.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(Boolean.class));
        assertTrue(PrimitiveType.STRING.isAssignableFrom(String.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(byte.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(Byte.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(short.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(Short.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(int.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(Integer.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(long.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(Long.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(BigInteger.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(float.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(Float.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(double.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(Double.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(BigDecimal.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(Date.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(Calendar.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(Object.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(char.class));
        assertFalse(PrimitiveType.STRING.isAssignableFrom(Character.class));

        // BYTE
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(boolean.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(Boolean.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(String.class));
        assertTrue(PrimitiveType.BYTE.isAssignableFrom(byte.class));
        assertTrue(PrimitiveType.BYTE.isAssignableFrom(Byte.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(short.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(Short.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(int.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(Integer.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(long.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(Long.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(BigInteger.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(float.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(Float.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(double.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(Double.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(BigDecimal.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(Date.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(Calendar.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(Object.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(char.class));
        assertFalse(PrimitiveType.BYTE.isAssignableFrom(Character.class));

        // SHORT
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(boolean.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(Boolean.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(String.class));
        assertTrue(PrimitiveType.SHORT.isAssignableFrom(byte.class));
        assertTrue(PrimitiveType.SHORT.isAssignableFrom(Byte.class));
        assertTrue(PrimitiveType.SHORT.isAssignableFrom(short.class));
        assertTrue(PrimitiveType.SHORT.isAssignableFrom(Short.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(int.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(Integer.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(long.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(Long.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(BigInteger.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(float.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(Float.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(double.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(Double.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(BigDecimal.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(Date.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(Calendar.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(Object.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(char.class));
        assertFalse(PrimitiveType.SHORT.isAssignableFrom(Character.class));

        // INTEGER
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(boolean.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(Boolean.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(String.class));
        assertTrue(PrimitiveType.INTEGER.isAssignableFrom(byte.class));
        assertTrue(PrimitiveType.INTEGER.isAssignableFrom(Byte.class));
        assertTrue(PrimitiveType.INTEGER.isAssignableFrom(short.class));
        assertTrue(PrimitiveType.INTEGER.isAssignableFrom(Short.class));
        assertTrue(PrimitiveType.INTEGER.isAssignableFrom(int.class));
        assertTrue(PrimitiveType.INTEGER.isAssignableFrom(Integer.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(long.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(Long.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(BigInteger.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(float.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(Float.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(double.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(Double.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(BigDecimal.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(Date.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(Calendar.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(Object.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(char.class));
        assertFalse(PrimitiveType.INTEGER.isAssignableFrom(Character.class));

        // LONG
        assertFalse(PrimitiveType.LONG.isAssignableFrom(boolean.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(Boolean.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(String.class));
        assertTrue(PrimitiveType.LONG.isAssignableFrom(byte.class));
        assertTrue(PrimitiveType.LONG.isAssignableFrom(Byte.class));
        assertTrue(PrimitiveType.LONG.isAssignableFrom(short.class));
        assertTrue(PrimitiveType.LONG.isAssignableFrom(Short.class));
        assertTrue(PrimitiveType.LONG.isAssignableFrom(int.class));
        assertTrue(PrimitiveType.LONG.isAssignableFrom(Integer.class));
        assertTrue(PrimitiveType.LONG.isAssignableFrom(long.class));
        assertTrue(PrimitiveType.LONG.isAssignableFrom(Long.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(BigInteger.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(float.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(Float.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(double.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(Double.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(BigDecimal.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(Date.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(Calendar.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(Object.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(char.class));
        assertFalse(PrimitiveType.LONG.isAssignableFrom(Character.class));

        // BIGINTEGER
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(boolean.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(Boolean.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(String.class));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableFrom(byte.class));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableFrom(Byte.class));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableFrom(short.class));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableFrom(Short.class));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableFrom(int.class));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableFrom(Integer.class));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableFrom(long.class));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableFrom(Long.class));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableFrom(BigInteger.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(float.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(Float.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(double.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(Double.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(BigDecimal.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(Date.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(Calendar.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(Object.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(char.class));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableFrom(Character.class));

        // FLOAT
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(boolean.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(Boolean.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(String.class));
        assertTrue(PrimitiveType.FLOAT.isAssignableFrom(byte.class));
        assertTrue(PrimitiveType.FLOAT.isAssignableFrom(Byte.class));
        assertTrue(PrimitiveType.FLOAT.isAssignableFrom(short.class));
        assertTrue(PrimitiveType.FLOAT.isAssignableFrom(Short.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(int.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(Integer.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(long.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(Long.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(BigInteger.class));
        assertTrue(PrimitiveType.FLOAT.isAssignableFrom(float.class));
        assertTrue(PrimitiveType.FLOAT.isAssignableFrom(Float.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(double.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(Double.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(BigDecimal.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(Date.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(Calendar.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(Object.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(char.class));
        assertFalse(PrimitiveType.FLOAT.isAssignableFrom(Character.class));

        // DOUBLE
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(boolean.class));
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(Boolean.class));
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(String.class));
        assertTrue(PrimitiveType.DOUBLE.isAssignableFrom(byte.class));
        assertTrue(PrimitiveType.DOUBLE.isAssignableFrom(Byte.class));
        assertTrue(PrimitiveType.DOUBLE.isAssignableFrom(short.class));
        assertTrue(PrimitiveType.DOUBLE.isAssignableFrom(Short.class));
        assertTrue(PrimitiveType.DOUBLE.isAssignableFrom(int.class));
        assertTrue(PrimitiveType.DOUBLE.isAssignableFrom(Integer.class));
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(long.class));
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(Long.class));
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(BigInteger.class));
        assertTrue(PrimitiveType.DOUBLE.isAssignableFrom(float.class));
        assertTrue(PrimitiveType.DOUBLE.isAssignableFrom(Float.class));
        assertTrue(PrimitiveType.DOUBLE.isAssignableFrom(double.class));
        assertTrue(PrimitiveType.DOUBLE.isAssignableFrom(Double.class));
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(BigDecimal.class));
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(Date.class));
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(Calendar.class));
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(Object.class));
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(char.class));
        assertFalse(PrimitiveType.DOUBLE.isAssignableFrom(Character.class));

        // BIGDECIMAL
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableFrom(boolean.class));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableFrom(Boolean.class));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableFrom(String.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(byte.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(Byte.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(short.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(Short.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(int.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(Integer.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(long.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(Long.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(BigInteger.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(float.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(Float.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(double.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(Double.class));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableFrom(BigDecimal.class));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableFrom(Date.class));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableFrom(Calendar.class));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableFrom(Object.class));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableFrom(char.class));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableFrom(Character.class));

        // CALENDAR
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(boolean.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(Boolean.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(String.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(byte.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(Byte.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(short.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(Short.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(int.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(Integer.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(long.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(Long.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(BigInteger.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(float.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(Float.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(double.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(Double.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(BigDecimal.class));
        assertTrue(PrimitiveType.CALENDAR.isAssignableFrom(Date.class));
        assertTrue(PrimitiveType.CALENDAR.isAssignableFrom(Calendar.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(Object.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(char.class));
        assertFalse(PrimitiveType.CALENDAR.isAssignableFrom(Character.class));
    }
    
    @Test
    public void testIsAssignableValue() throws Exception
    {
        // BOOLEAN
        assertTrue(PrimitiveType.BOOLEAN.isAssignableValue(null));
        assertTrue(PrimitiveType.BOOLEAN.isAssignableValue(true));
        assertTrue(PrimitiveType.BOOLEAN.isAssignableValue(new Boolean(false)));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue("string"));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(Byte.MIN_VALUE));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(new Byte(Byte.MAX_VALUE)));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(Short.MIN_VALUE));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(new Short(Short.MAX_VALUE)));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(Integer.MIN_VALUE));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(new Integer(Integer.MAX_VALUE)));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(Long.MIN_VALUE));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(new Long(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(BigInteger.valueOf(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(Float.MIN_VALUE));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(new Float(Float.MAX_VALUE)));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(Double.MIN_VALUE));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(new Double(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(BigDecimal.valueOf(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(new Date()));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(Calendar.getInstance()));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(new Object()));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(Character.MIN_VALUE));
        assertFalse(PrimitiveType.BOOLEAN.isAssignableValue(new Character(Character.MAX_VALUE)));

        // STRING
        assertTrue(PrimitiveType.STRING.isAssignableValue(null));
        assertFalse(PrimitiveType.STRING.isAssignableValue(true));
        assertFalse(PrimitiveType.STRING.isAssignableValue(new Boolean(false)));
        assertTrue(PrimitiveType.STRING.isAssignableValue("string"));
        assertFalse(PrimitiveType.STRING.isAssignableValue(Byte.MIN_VALUE));
        assertFalse(PrimitiveType.STRING.isAssignableValue(new Byte(Byte.MAX_VALUE)));
        assertFalse(PrimitiveType.STRING.isAssignableValue(Short.MIN_VALUE));
        assertFalse(PrimitiveType.STRING.isAssignableValue(new Short(Short.MAX_VALUE)));
        assertFalse(PrimitiveType.STRING.isAssignableValue(Integer.MIN_VALUE));
        assertFalse(PrimitiveType.STRING.isAssignableValue(new Integer(Integer.MAX_VALUE)));
        assertFalse(PrimitiveType.STRING.isAssignableValue(Long.MIN_VALUE));
        assertFalse(PrimitiveType.STRING.isAssignableValue(new Long(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.STRING.isAssignableValue(BigInteger.valueOf(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.STRING.isAssignableValue(Float.MIN_VALUE));
        assertFalse(PrimitiveType.STRING.isAssignableValue(new Float(Float.MAX_VALUE)));
        assertFalse(PrimitiveType.STRING.isAssignableValue(Double.MIN_VALUE));
        assertFalse(PrimitiveType.STRING.isAssignableValue(new Double(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.STRING.isAssignableValue(BigDecimal.valueOf(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.STRING.isAssignableValue(new Date()));
        assertFalse(PrimitiveType.STRING.isAssignableValue(Calendar.getInstance()));
        assertFalse(PrimitiveType.STRING.isAssignableValue(new Object()));
        assertFalse(PrimitiveType.STRING.isAssignableValue(Character.MIN_VALUE));
        assertFalse(PrimitiveType.STRING.isAssignableValue(new Character(Character.MAX_VALUE)));

        // BYTE
        assertTrue(PrimitiveType.BYTE.isAssignableValue(null));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(true));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(new Boolean(false)));
        assertFalse(PrimitiveType.BYTE.isAssignableValue("string"));
        assertTrue(PrimitiveType.BYTE.isAssignableValue(Byte.MIN_VALUE));
        assertTrue(PrimitiveType.BYTE.isAssignableValue(new Byte(Byte.MAX_VALUE)));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(Short.MIN_VALUE));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(new Short(Short.MAX_VALUE)));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(Integer.MIN_VALUE));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(new Integer(Integer.MAX_VALUE)));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(Long.MIN_VALUE));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(new Long(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(BigInteger.valueOf(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(Float.MIN_VALUE));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(new Float(Float.MAX_VALUE)));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(Double.MIN_VALUE));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(new Double(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(BigDecimal.valueOf(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(new Date()));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(Calendar.getInstance()));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(new Object()));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(Character.MIN_VALUE));
        assertFalse(PrimitiveType.BYTE.isAssignableValue(new Character(Character.MAX_VALUE)));

        // SHORT
        assertTrue(PrimitiveType.SHORT.isAssignableValue(null));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(true));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(new Boolean(false)));
        assertFalse(PrimitiveType.SHORT.isAssignableValue("string"));
        assertTrue(PrimitiveType.SHORT.isAssignableValue(Byte.MIN_VALUE));
        assertTrue(PrimitiveType.SHORT.isAssignableValue(new Byte(Byte.MAX_VALUE)));
        assertTrue(PrimitiveType.SHORT.isAssignableValue(Short.MIN_VALUE));
        assertTrue(PrimitiveType.SHORT.isAssignableValue(new Short(Short.MAX_VALUE)));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(Integer.MIN_VALUE));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(new Integer(Integer.MAX_VALUE)));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(Long.MIN_VALUE));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(new Long(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(BigInteger.valueOf(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(Float.MIN_VALUE));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(new Float(Float.MAX_VALUE)));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(Double.MIN_VALUE));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(new Double(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(BigDecimal.valueOf(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(new Date()));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(Calendar.getInstance()));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(new Object()));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(Character.MIN_VALUE));
        assertFalse(PrimitiveType.SHORT.isAssignableValue(new Character(Character.MAX_VALUE)));

        // INTEGER
        assertTrue(PrimitiveType.INTEGER.isAssignableValue(null));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(true));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(new Boolean(false)));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue("string"));
        assertTrue(PrimitiveType.INTEGER.isAssignableValue(Byte.MIN_VALUE));
        assertTrue(PrimitiveType.INTEGER.isAssignableValue(new Byte(Byte.MAX_VALUE)));
        assertTrue(PrimitiveType.INTEGER.isAssignableValue(Short.MIN_VALUE));
        assertTrue(PrimitiveType.INTEGER.isAssignableValue(new Short(Short.MAX_VALUE)));
        assertTrue(PrimitiveType.INTEGER.isAssignableValue(Integer.MIN_VALUE));
        assertTrue(PrimitiveType.INTEGER.isAssignableValue(new Integer(Integer.MAX_VALUE)));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(Long.MIN_VALUE));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(new Long(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(BigInteger.valueOf(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(Float.MIN_VALUE));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(new Float(Float.MAX_VALUE)));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(Double.MIN_VALUE));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(new Double(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(BigDecimal.valueOf(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(new Date()));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(Calendar.getInstance()));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(new Object()));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(Character.MIN_VALUE));
        assertFalse(PrimitiveType.INTEGER.isAssignableValue(new Character(Character.MAX_VALUE)));

        // LONG
        assertTrue(PrimitiveType.LONG.isAssignableValue(null));
        assertFalse(PrimitiveType.LONG.isAssignableValue(true));
        assertFalse(PrimitiveType.LONG.isAssignableValue(new Boolean(false)));
        assertFalse(PrimitiveType.LONG.isAssignableValue("string"));
        assertTrue(PrimitiveType.LONG.isAssignableValue(Byte.MIN_VALUE));
        assertTrue(PrimitiveType.LONG.isAssignableValue(new Byte(Byte.MAX_VALUE)));
        assertTrue(PrimitiveType.LONG.isAssignableValue(Short.MIN_VALUE));
        assertTrue(PrimitiveType.LONG.isAssignableValue(new Short(Short.MAX_VALUE)));
        assertTrue(PrimitiveType.LONG.isAssignableValue(Integer.MIN_VALUE));
        assertTrue(PrimitiveType.LONG.isAssignableValue(new Integer(Integer.MAX_VALUE)));
        assertTrue(PrimitiveType.LONG.isAssignableValue(Long.MIN_VALUE));
        assertTrue(PrimitiveType.LONG.isAssignableValue(new Long(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.LONG.isAssignableValue(BigInteger.valueOf(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.LONG.isAssignableValue(Float.MIN_VALUE));
        assertFalse(PrimitiveType.LONG.isAssignableValue(new Float(Float.MAX_VALUE)));
        assertFalse(PrimitiveType.LONG.isAssignableValue(Double.MIN_VALUE));
        assertFalse(PrimitiveType.LONG.isAssignableValue(new Double(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.LONG.isAssignableValue(BigDecimal.valueOf(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.LONG.isAssignableValue(new Date()));
        assertFalse(PrimitiveType.LONG.isAssignableValue(Calendar.getInstance()));
        assertFalse(PrimitiveType.LONG.isAssignableValue(new Object()));
        assertFalse(PrimitiveType.LONG.isAssignableValue(Character.MIN_VALUE));
        assertFalse(PrimitiveType.LONG.isAssignableValue(new Character(Character.MAX_VALUE)));

        // BIGINTEGER
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableValue(null));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(true));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(new Boolean(false)));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue("string"));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableValue(Byte.MIN_VALUE));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableValue(new Byte(Byte.MAX_VALUE)));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableValue(Short.MIN_VALUE));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableValue(new Short(Short.MAX_VALUE)));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableValue(Integer.MIN_VALUE));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableValue(new Integer(Integer.MAX_VALUE)));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableValue(Long.MIN_VALUE));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableValue(new Long(Long.MAX_VALUE)));
        assertTrue(PrimitiveType.BIGINTEGER.isAssignableValue(BigInteger.valueOf(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(Float.MIN_VALUE));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(new Float(Float.MAX_VALUE)));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(Double.MIN_VALUE));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(new Double(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(BigDecimal.valueOf(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(new Date()));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(Calendar.getInstance()));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(new Object()));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(Character.MIN_VALUE));
        assertFalse(PrimitiveType.BIGINTEGER.isAssignableValue(new Character(Character.MAX_VALUE)));

        // FLOAT
        assertTrue(PrimitiveType.FLOAT.isAssignableValue(null));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(true));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(new Boolean(false)));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue("string"));
        assertTrue(PrimitiveType.FLOAT.isAssignableValue(Byte.MIN_VALUE));
        assertTrue(PrimitiveType.FLOAT.isAssignableValue(new Byte(Byte.MAX_VALUE)));
        assertTrue(PrimitiveType.FLOAT.isAssignableValue(Short.MIN_VALUE));
        assertTrue(PrimitiveType.FLOAT.isAssignableValue(new Short(Short.MAX_VALUE)));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(Integer.MIN_VALUE));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(new Integer(Integer.MAX_VALUE)));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(Long.MIN_VALUE));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(new Long(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(BigInteger.valueOf(Long.MAX_VALUE)));
        assertTrue(PrimitiveType.FLOAT.isAssignableValue(Float.MIN_VALUE));
        assertTrue(PrimitiveType.FLOAT.isAssignableValue(new Float(Float.MAX_VALUE)));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(Double.MIN_VALUE));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(new Double(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(BigDecimal.valueOf(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(new Date()));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(Calendar.getInstance()));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(new Object()));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(Character.MIN_VALUE));
        assertFalse(PrimitiveType.FLOAT.isAssignableValue(new Character(Character.MAX_VALUE)));

        // DOUBLE
        assertTrue(PrimitiveType.DOUBLE.isAssignableValue(null));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue(true));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue(new Boolean(false)));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue("string"));
        assertTrue(PrimitiveType.DOUBLE.isAssignableValue(Byte.MIN_VALUE));
        assertTrue(PrimitiveType.DOUBLE.isAssignableValue(new Byte(Byte.MAX_VALUE)));
        assertTrue(PrimitiveType.DOUBLE.isAssignableValue(Short.MIN_VALUE));
        assertTrue(PrimitiveType.DOUBLE.isAssignableValue(new Short(Short.MAX_VALUE)));
        assertTrue(PrimitiveType.DOUBLE.isAssignableValue(Integer.MIN_VALUE));
        assertTrue(PrimitiveType.DOUBLE.isAssignableValue(new Integer(Integer.MAX_VALUE)));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue(Long.MIN_VALUE));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue(new Long(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue(BigInteger.valueOf(Long.MAX_VALUE)));
        assertTrue(PrimitiveType.DOUBLE.isAssignableValue(Float.MIN_VALUE));
        assertTrue(PrimitiveType.DOUBLE.isAssignableValue(new Float(Float.MAX_VALUE)));
        assertTrue(PrimitiveType.DOUBLE.isAssignableValue(Double.MIN_VALUE));
        assertTrue(PrimitiveType.DOUBLE.isAssignableValue(new Double(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue(BigDecimal.valueOf(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue(new Date()));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue(Calendar.getInstance()));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue(new Object()));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue(Character.MIN_VALUE));
        assertFalse(PrimitiveType.DOUBLE.isAssignableValue(new Character(Character.MAX_VALUE)));

        // BIGDECIMAL
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(null));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableValue(true));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableValue(new Boolean(false)));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableValue("string"));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(Byte.MIN_VALUE));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(new Byte(Byte.MAX_VALUE)));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(Short.MIN_VALUE));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(new Short(Short.MAX_VALUE)));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(Integer.MIN_VALUE));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(new Integer(Integer.MAX_VALUE)));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(Long.MIN_VALUE));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(new Long(Long.MAX_VALUE)));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(BigInteger.valueOf(Long.MAX_VALUE)));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(Float.MIN_VALUE));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(new Float(Float.MAX_VALUE)));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(Double.MIN_VALUE));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(new Double(Double.MAX_VALUE)));
        assertTrue(PrimitiveType.BIGDECIMAL.isAssignableValue(BigDecimal.valueOf(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableValue(new Date()));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableValue(Calendar.getInstance()));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableValue(new Object()));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableValue(Character.MIN_VALUE));
        assertFalse(PrimitiveType.BIGDECIMAL.isAssignableValue(new Character(Character.MAX_VALUE)));

        // CALENDAR
        assertTrue(PrimitiveType.CALENDAR.isAssignableValue(null));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(true));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(new Boolean(false)));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue("string"));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(Byte.MIN_VALUE));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(new Byte(Byte.MAX_VALUE)));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(Short.MIN_VALUE));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(new Short(Short.MAX_VALUE)));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(Integer.MIN_VALUE));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(new Integer(Integer.MAX_VALUE)));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(Long.MIN_VALUE));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(new Long(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(BigInteger.valueOf(Long.MAX_VALUE)));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(Float.MIN_VALUE));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(new Float(Float.MAX_VALUE)));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(Double.MIN_VALUE));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(new Double(Double.MAX_VALUE)));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(BigDecimal.valueOf(Double.MAX_VALUE)));
        assertTrue(PrimitiveType.CALENDAR.isAssignableValue(new Date()));
        assertTrue(PrimitiveType.CALENDAR.isAssignableValue(Calendar.getInstance()));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(new Object()));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(Character.MIN_VALUE));
        assertFalse(PrimitiveType.CALENDAR.isAssignableValue(new Character(Character.MAX_VALUE)));
    }
    
    private static boolean isUnassignableValue(PrimitiveType type, Object value) throws Exception
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
    
    private static final BigInteger bigIntegerValue = new BigInteger("123456789012345678901234567890");

    private static final BigDecimal bigDecimalValue = new BigDecimal("123456789012345678901234567890.123456789012345678901234567890");
    
    @Test
    public void testConvertToTypedObject() throws Exception
    {
        // BOOLEAN
        assertTrue(PrimitiveType.BOOLEAN.convertToTypedObject(true) instanceof Boolean);
        assertEquals(new Boolean(false), PrimitiveType.BOOLEAN.convertToTypedObject(false));
        assertTrue(isUnassignableValue(PrimitiveType.BOOLEAN, "true"));
        assertTrue(isUnassignableValue(PrimitiveType.BYTE, Integer.MIN_VALUE));
                
        // BYTE
        assertTrue(PrimitiveType.BYTE.convertToTypedObject(Byte.MIN_VALUE) instanceof Byte);
        assertEquals(Byte.MAX_VALUE, PrimitiveType.BYTE.convertToTypedObject(Byte.MAX_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.BYTE, "0"));
        assertTrue(isUnassignableValue(PrimitiveType.BYTE, Short.MIN_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.BYTE, Float.MAX_VALUE));
                
        // SHORT
        assertTrue(PrimitiveType.SHORT.convertToTypedObject(Short.MIN_VALUE) instanceof Short);
        assertEquals(new Short(Byte.MAX_VALUE), PrimitiveType.SHORT.convertToTypedObject(Byte.MAX_VALUE));
        assertEquals(new Short(Short.MAX_VALUE), PrimitiveType.SHORT.convertToTypedObject(Short.MAX_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.SHORT, "0"));
        assertTrue(isUnassignableValue(PrimitiveType.SHORT, Integer.MIN_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.SHORT, Float.MAX_VALUE));
        
        // INTEGER
        assertTrue(PrimitiveType.INTEGER.convertToTypedObject(Integer.MIN_VALUE) instanceof Integer);
        assertEquals(new Integer(Byte.MAX_VALUE), PrimitiveType.INTEGER.convertToTypedObject(Byte.MAX_VALUE));
        assertEquals(new Integer(Short.MAX_VALUE), PrimitiveType.INTEGER.convertToTypedObject(Short.MAX_VALUE));
        assertEquals(new Integer(Integer.MAX_VALUE), PrimitiveType.INTEGER.convertToTypedObject(Integer.MAX_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.INTEGER, "0"));
        assertTrue(isUnassignableValue(PrimitiveType.INTEGER, Long.MIN_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.INTEGER, Float.MAX_VALUE));
        
        // LONG
        assertTrue(PrimitiveType.LONG.convertToTypedObject(Long.MIN_VALUE) instanceof Long);
        assertEquals(new Long(Byte.MAX_VALUE), PrimitiveType.LONG.convertToTypedObject(Byte.MAX_VALUE));
        assertEquals(new Long(Short.MAX_VALUE), PrimitiveType.LONG.convertToTypedObject(Short.MAX_VALUE));
        assertEquals(new Long(Integer.MAX_VALUE), PrimitiveType.LONG.convertToTypedObject(Integer.MAX_VALUE));
        assertEquals(new Long(Long.MAX_VALUE), PrimitiveType.LONG.convertToTypedObject(Long.MAX_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.LONG, "0"));
        assertTrue(isUnassignableValue(PrimitiveType.LONG, bigIntegerValue));
        assertTrue(isUnassignableValue(PrimitiveType.LONG, Float.MAX_VALUE));
        
        // BIGINTEGER
        assertTrue(PrimitiveType.BIGINTEGER.convertToTypedObject(bigIntegerValue) instanceof BigInteger);
        assertEquals(BigInteger.valueOf(Byte.MAX_VALUE), PrimitiveType.BIGINTEGER.convertToTypedObject(Byte.MAX_VALUE));
        assertEquals(BigInteger.valueOf(Short.MAX_VALUE), PrimitiveType.BIGINTEGER.convertToTypedObject(Short.MAX_VALUE));
        assertEquals(BigInteger.valueOf(Integer.MAX_VALUE), PrimitiveType.BIGINTEGER.convertToTypedObject(Integer.MAX_VALUE));
        assertEquals(BigInteger.valueOf(Long.MAX_VALUE), PrimitiveType.BIGINTEGER.convertToTypedObject(Long.MAX_VALUE));
        assertEquals(bigIntegerValue, PrimitiveType.BIGINTEGER.convertToTypedObject(bigIntegerValue));
        assertTrue(isUnassignableValue(PrimitiveType.BIGINTEGER, "0"));
        assertTrue(isUnassignableValue(PrimitiveType.BIGINTEGER, Float.MAX_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.BIGINTEGER, bigDecimalValue));
        
        // FLOAT
        assertTrue(PrimitiveType.FLOAT.convertToTypedObject(Float.MIN_VALUE) instanceof Float);
        assertEquals(new Float(Byte.MAX_VALUE), PrimitiveType.FLOAT.convertToTypedObject(Byte.MAX_VALUE));
        assertEquals(new Float(Short.MAX_VALUE), PrimitiveType.FLOAT.convertToTypedObject(Short.MAX_VALUE));
        assertEquals(new Float(Float.MAX_VALUE), PrimitiveType.FLOAT.convertToTypedObject(Float.MAX_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.FLOAT, "0"));
        assertTrue(isUnassignableValue(PrimitiveType.FLOAT, Integer.MAX_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.FLOAT, Double.MAX_VALUE));
        
        // DOUBLE
        assertTrue(PrimitiveType.DOUBLE.convertToTypedObject(Double.MIN_VALUE) instanceof Double);
        assertEquals(new Double(Byte.MAX_VALUE), PrimitiveType.DOUBLE.convertToTypedObject(Byte.MAX_VALUE));
        assertEquals(new Double(Short.MAX_VALUE), PrimitiveType.DOUBLE.convertToTypedObject(Short.MAX_VALUE));
        assertEquals(new Double(Integer.MAX_VALUE), PrimitiveType.DOUBLE.convertToTypedObject(Integer.MAX_VALUE));
        assertEquals(new Double(Float.MAX_VALUE), PrimitiveType.DOUBLE.convertToTypedObject(Float.MAX_VALUE));
        assertEquals(new Double(Double.MAX_VALUE), PrimitiveType.DOUBLE.convertToTypedObject(Double.MAX_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.DOUBLE, "0"));
        assertTrue(isUnassignableValue(PrimitiveType.DOUBLE, Long.MAX_VALUE));
        assertTrue(isUnassignableValue(PrimitiveType.DOUBLE, bigDecimalValue));
        
        // BIGDECIMAL
        assertTrue(PrimitiveType.BIGDECIMAL.convertToTypedObject(bigDecimalValue) instanceof BigDecimal);
        assertEquals(BigDecimal.valueOf(Byte.MAX_VALUE), PrimitiveType.BIGDECIMAL.convertToTypedObject(Byte.MAX_VALUE));
        assertEquals(BigDecimal.valueOf(Short.MAX_VALUE), PrimitiveType.BIGDECIMAL.convertToTypedObject(Short.MAX_VALUE));
        assertEquals(BigDecimal.valueOf(Integer.MAX_VALUE), PrimitiveType.BIGDECIMAL.convertToTypedObject(Integer.MAX_VALUE));
        assertEquals(BigDecimal.valueOf(Long.MAX_VALUE), PrimitiveType.BIGDECIMAL.convertToTypedObject(Long.MAX_VALUE));
        assertEquals(BigDecimal.valueOf(Float.MAX_VALUE), PrimitiveType.BIGDECIMAL.convertToTypedObject(Float.MAX_VALUE));
        assertEquals(BigDecimal.valueOf(Double.MAX_VALUE), PrimitiveType.BIGDECIMAL.convertToTypedObject(Double.MAX_VALUE));
        assertEquals(bigDecimalValue, PrimitiveType.BIGDECIMAL.convertToTypedObject(bigDecimalValue));
        assertTrue(isUnassignableValue(PrimitiveType.BIGDECIMAL, "0"));

        // CALENDAR
        assertTrue(PrimitiveType.CALENDAR.convertToTypedObject(new Date()) instanceof Calendar);
        Calendar calendarValue = Calendar.getInstance();
        assertEquals(calendarValue, PrimitiveType.CALENDAR.convertToTypedObject(new Date(calendarValue.getTimeInMillis())));
    }
}
