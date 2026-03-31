package org.egovframe.rte.fdl.cmmn.config;

import org.egovframe.rte.fdl.cmmn.setter.SetterBar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SetterConfig {

    @Bean
    public SetterBar setterBar() {
        return new SetterBar();
    }

}
