package org.egovframe.rte.itl.integration.config;

import org.egovframe.rte.itl.integration.type.support.PrimitiveTypeFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TypeSupportContextConfig {

    @Bean(name = "boolean")
    public PrimitiveTypeFactoryBean booleanType() {
        return new PrimitiveTypeFactoryBean();
    }

    @Bean(name = "string")
    public PrimitiveTypeFactoryBean stringType() {
        return new PrimitiveTypeFactoryBean();
    }

    @Bean(name = "byte")
    public PrimitiveTypeFactoryBean byteType() {
        return new PrimitiveTypeFactoryBean();
    }

    @Bean(name = "short")
    public PrimitiveTypeFactoryBean shortType() {
        return new PrimitiveTypeFactoryBean();
    }

    @Bean(name = "integer")
    public PrimitiveTypeFactoryBean integerType() {
        return new PrimitiveTypeFactoryBean();
    }

    @Bean(name = "long")
    public PrimitiveTypeFactoryBean longType() {
        return new PrimitiveTypeFactoryBean();
    }

    @Bean(name = "biginteger")
    public PrimitiveTypeFactoryBean bigIntegerType() {
        return new PrimitiveTypeFactoryBean();
    }

    @Bean(name = "float")
    public PrimitiveTypeFactoryBean floatType() {
        return new PrimitiveTypeFactoryBean();
    }

    @Bean(name = "double")
    public PrimitiveTypeFactoryBean doubleType() {
        return new PrimitiveTypeFactoryBean();
    }

    @Bean(name = "bigdecimal")
    public PrimitiveTypeFactoryBean bigDecimalType() {
        return new PrimitiveTypeFactoryBean();
    }

    @Bean(name = "calendar")
    public PrimitiveTypeFactoryBean calendarType() {
        return new PrimitiveTypeFactoryBean();
    }

}
