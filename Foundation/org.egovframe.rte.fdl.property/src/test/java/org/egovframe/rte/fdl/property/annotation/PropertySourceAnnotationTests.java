package org.egovframe.rte.fdl.property.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertySourceAnnotationTests {

    @Test
    public void withExplicitName() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(ConfigWithExplicitName.class);
        ctx.refresh();

        assertTrue(ctx.getEnvironment().getPropertySources().contains("p1"));
        assertEquals("p1TestBean", ctx.getBean(TestBean.class).getName());

        String name;

        MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
        Iterator<org.springframework.core.env.PropertySource<?>> iterator = sources.iterator();

        do {
            name = iterator.next().getName();
        }
        while (iterator.hasNext());

        assertEquals("p1", name);

        ctx.close();
    }

    @Test
    public void orderingIsLifo() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(ConfigWithImplicitName.class, P2Config.class);
        ctx.refresh();
        assertEquals("p2TestBean", ctx.getBean(TestBean.class).getName());
        ctx.close();

        AnnotationConfigApplicationContext ctx2 = new AnnotationConfigApplicationContext();
        ctx2.register(P2Config.class, ConfigWithImplicitName.class);
        ctx2.refresh();
        assertEquals("p1TestBean", ctx2.getBean(TestBean.class).getName());
        ctx2.close();
    }

    @Test
    public void withNameAndMultipleResourceLocations() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(ConfigWithNameAndMultipleResourceLocations.class);
        ctx.refresh();
        assertTrue(ctx.getEnvironment().containsProperty("from.p1"));
        assertTrue(ctx.getEnvironment().containsProperty("from.p2"));
        ctx.close();
    }

    @Configuration
    @PropertySource(value = "classpath:/META-INF/properties/p1.properties")
    static class ConfigWithImplicitName {
        @Autowired
        Environment env;

        @Bean
        public TestBean testBean() {
            return new TestBean(env.getProperty("testbean.name"));
        }
    }

    @Configuration
    @PropertySource(name = "p1", value = "classpath:/META-INF/properties/p1.properties")
    static class ConfigWithExplicitName {
        @Autowired
        Environment env;

        @Bean
        public TestBean testBean() {
            return new TestBean(env.getProperty("testbean.name"));
        }
    }

    @Configuration
    @PropertySource("classpath:/META-INF/properties/p2.properties")
    static class P2Config {
    }

    @Configuration
    @PropertySource(name = "psName", value = {"classpath:/META-INF/properties/p1.properties", "classpath:/META-INF/properties/p2.properties"})
    static class ConfigWithNameAndMultipleResourceLocations {
        @Autowired
        Environment env;

        @Bean
        public String TesDoubleProperties() {
            return env.getProperty("testbean.name");
        }
    }

}
