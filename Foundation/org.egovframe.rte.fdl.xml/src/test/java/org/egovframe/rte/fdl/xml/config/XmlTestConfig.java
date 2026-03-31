package org.egovframe.rte.fdl.xml.config;

import org.egovframe.rte.fdl.xml.EgovXmlset;
import org.egovframe.rte.fdl.xml.impl.EgovDOMFactoryServiceImpl;
import org.egovframe.rte.fdl.xml.impl.EgovSAXFactoryServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XmlTestConfig {

    /**
     * DOM Factory Service 빈 설정
     */
    @Bean
    public EgovDOMFactoryServiceImpl domconcreteCont() {
        return new EgovDOMFactoryServiceImpl();
    }

    /**
     * SAX Factory Service 빈 설정
     */
    @Bean
    public EgovSAXFactoryServiceImpl saxconcreteCont() {
        return new EgovSAXFactoryServiceImpl();
    }

    /**
     * XML 설정 빈 설정
     */
    @Bean(name = "xmlCofig")
    public EgovXmlset xmlCofig() {
        EgovXmlset xmlset = new EgovXmlset();
        xmlset.setDomconcrete(domconcreteCont());
        xmlset.setSaxconcrete(saxconcreteCont());
        return xmlset;
    }

}
