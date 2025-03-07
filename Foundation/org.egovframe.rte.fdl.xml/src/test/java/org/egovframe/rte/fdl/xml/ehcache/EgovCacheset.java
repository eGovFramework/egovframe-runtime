package org.egovframe.rte.fdl.xml.ehcache;

import org.egovframe.rte.fdl.xml.EgovConcreteDOMFactory;
import org.egovframe.rte.fdl.xml.EgovConcreteSAXFactory;

public class EgovCacheset {

    private EgovConcreteDOMFactory domconcrete;

    private EgovConcreteSAXFactory saxconcrete;

    public EgovConcreteDOMFactory getDomconcrete() {
        return domconcrete;
    }

    public void setDomconcrete(EgovConcreteDOMFactory domconcrete) {
        this.domconcrete = domconcrete;
    }

    public EgovConcreteSAXFactory getSaxconcrete() {
        return saxconcrete;
    }

    public void setSaxconcrete(EgovConcreteSAXFactory saxconcrete) {
        this.saxconcrete = saxconcrete;
    }

}
