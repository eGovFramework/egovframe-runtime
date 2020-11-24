package org.egovframe.rte.fdl.xml;

import org.egovframe.rte.fdl.xml.impl.EgovDOMFactoryServiceImpl;
import org.egovframe.rte.fdl.xml.impl.EgovSAXFactoryServiceImpl;

/**
 * @Class Name : EgovXmlset.java
 * @Description : EgovConcreteDOMFactory, EgovConcreteSAXFactory 설정
 * @Modification Information  
 * @
 * @  수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2009.03.18    김종호        최초생성
 * 
 * @author 개발프레임웍크 실행환경 개발팀 김종호
 * @since 2009. 03.18
 * @version 1.0
 * @see
 * 
 *  # Copyright (C) by MOPAS All right reserved.
 */
public class EgovXmlset {
	/** abstractXMLFactoryService 상속한 EgovConcreteDOMFactory **/ 
	private EgovDOMFactoryServiceImpl domconcrete;
	/** abstractXMLFactoryService 상속한 EgovConcreteSAXFactory **/ 
	private EgovSAXFactoryServiceImpl saxconcrete;
	/**
	 * 구성 Class EgovConcreteDOMFactory 설정 
	 * @param domconcrete - 구상 Class EgovConcreteDOMFactory
	 */
	public void setDomconcrete(EgovDOMFactoryServiceImpl domconcrete)
	{
		this.domconcrete = domconcrete;
	}
	/**
	 * 구성 Class EgovConcreteDOMFactory 리턴 
	 * @return 구성 Class EgovConcreteDOMFactory
	 */
	public EgovDOMFactoryServiceImpl getDomconcrete()
	 {
		 return domconcrete;
	 }
	/**
	 * 구성 Class EgovConcreteSAXFactory 설정 
	 * @param saxconcrete - 구상 Class EgovConcreteSAXFactory
	 */
	public void setSaxconcrete(EgovSAXFactoryServiceImpl saxconcrete)
	{
		this.saxconcrete = saxconcrete;
	}
	/**
	 * 구성 Class EgovConcreteSAXFactory 리턴 
	 * @return 구성 Class EgovConcreteSAXFactory
	 */
	public EgovSAXFactoryServiceImpl getSaxconcrete()
	 {
		 return saxconcrete;
	 }
}
