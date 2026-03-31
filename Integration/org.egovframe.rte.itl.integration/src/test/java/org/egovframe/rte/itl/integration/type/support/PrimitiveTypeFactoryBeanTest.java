package org.egovframe.rte.itl.integration.type.support;

import jakarta.annotation.Resource;
import org.egovframe.rte.itl.integration.config.TypeSupportContextConfig;
import org.egovframe.rte.itl.integration.type.PrimitiveType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TypeSupportContextConfig.class)
public class PrimitiveTypeFactoryBeanTest {
    @Autowired
    private BeanFactory beanFactory;

    @Resource(name = "boolean")
    private PrimitiveType booleanType;

    @Resource(name = "string")
    private PrimitiveType stringType;

    @Resource(name = "byte")
    private PrimitiveType byteType;

    @Resource(name = "short")
    private PrimitiveType shortType;

    @Resource(name = "integer")
    private PrimitiveType integerType;

    @Resource(name = "long")
    private PrimitiveType longType;

    @Resource(name = "biginteger")
    private PrimitiveType bigintegerType;

    @Resource(name = "float")
    private PrimitiveType floatType;

    @Resource(name = "double")
    private PrimitiveType doubleType;

    @Resource(name = "bigdecimal")
    private PrimitiveType bigdecimalType;

    @Resource(name = "calendar")
    private PrimitiveType calendarType;

    @Test
    public void testDependencyInjection() throws Exception {
        assertEquals(PrimitiveType.BOOLEAN, booleanType);
        assertEquals(PrimitiveType.STRING, stringType);
        assertEquals(PrimitiveType.BYTE, byteType);
        assertEquals(PrimitiveType.SHORT, shortType);
        assertEquals(PrimitiveType.INTEGER, integerType);
        assertEquals(PrimitiveType.LONG, longType);
        assertEquals(PrimitiveType.BIGINTEGER, bigintegerType);
        assertEquals(PrimitiveType.FLOAT, floatType);
        assertEquals(PrimitiveType.DOUBLE, doubleType);
        assertEquals(PrimitiveType.BIGDECIMAL, bigdecimalType);
        assertEquals(PrimitiveType.CALENDAR, calendarType);
    }

    @Test
    public void testReference() throws Exception {
        assertEquals(PrimitiveType.BOOLEAN, beanFactory.getBean("boolean"));
        assertEquals(PrimitiveType.STRING, beanFactory.getBean("string"));
        assertEquals(PrimitiveType.BYTE, beanFactory.getBean("byte"));
        assertEquals(PrimitiveType.SHORT, beanFactory.getBean("short"));
        assertEquals(PrimitiveType.INTEGER, beanFactory.getBean("integer"));
        assertEquals(PrimitiveType.LONG, beanFactory.getBean("long"));
        assertEquals(PrimitiveType.BIGINTEGER, beanFactory.getBean("biginteger"));
        assertEquals(PrimitiveType.FLOAT, beanFactory.getBean("float"));
        assertEquals(PrimitiveType.DOUBLE, beanFactory.getBean("double"));
        assertEquals(PrimitiveType.BIGDECIMAL, beanFactory.getBean("bigdecimal"));
        assertEquals(PrimitiveType.CALENDAR, beanFactory.getBean("calendar"));
    }
}
