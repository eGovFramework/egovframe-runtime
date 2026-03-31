package org.egovframe.rte.fdl.cmmn.config;

import org.egovframe.rte.fdl.cmmn.cnamespace.Bar;
import org.egovframe.rte.fdl.cmmn.cnamespace.Baz;
import org.egovframe.rte.fdl.cmmn.cnamespace.Foo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CNamespaceConfig {

    @Bean
    public Bar bar() {
        return new Bar();
    }

    @Bean
    public Baz baz() {
        return new Baz();
    }

    @Bean
    public Foo foo() {
        return new Foo(bar(), baz(), "foo@bar.com");
    }

}
