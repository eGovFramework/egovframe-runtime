package org.egovframe.rte.fdl.xml;

import org.egovframe.rte.fdl.xml.exception.UnsupportedException;
import org.egovframe.rte.fdl.xml.exception.ValidatorException;
import org.egovframe.rte.fdl.xml.impl.EgovDOMFactoryServiceImpl;
import org.egovframe.rte.fdl.xml.impl.EgovSAXFactoryServiceImpl;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * CategoryControllerTest is TestCase of CategoryController
 *
 * @author Byunghun Woo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/context-xmltest.xml" })
/**
 * @Class Name : ControlXMLTest.java
 * @Description : XML Manipulation Test
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
 *  Copyright (C) by MOPAS All right reserved.
 */
public class ControlXMLTest {

	/** EgovAbstractXMLFactoryService 상속한 Class **/
	@Resource(name = "domconcreteCont")
	EgovDOMFactoryServiceImpl domconcrete = null;

	/** EgovAbstractXMLFactoryService 상속한 Class **/
	@Resource(name = "saxconcreteCont")
	EgovSAXFactoryServiceImpl saxconcrete = null;

	/** AbstractXMLUtility 상속한 DOMValidator **/
	EgovDOMValidatorService domValidator = null;
	/** AbstractXMLUtility 상속한 SAXValidator **/
	EgovSAXValidatorService saxValidator = null;

	private static final Logger LOGGER  = LoggerFactory.getLogger(ControlXMLTest.class);
	/** 테스트 XML 문서 **/
    String fileName = Thread.currentThread().getContextClassLoader().getResource("spring/context-test.xml").getFile();

    /**
     * DOM,SAX Validator 생성
     * @throws UnsupportedException
     */
    @Test
    public void ModuleTest() throws UnsupportedException
    {
    	// domValidator = domconcrete.CreateDOMValidator();
    	saxValidator = saxconcrete.createSAXValidator();
    	LOGGER.debug("fileName : {}", fileName);
    	//domValidator.setXMLFile(fileName);
		saxValidator.setXMLFile(fileName);

    	ModuleControl(saxValidator);
    }
    /**
     * ModuleControl 생성자
     * @param service - XMLUtility
     */
    public void ModuleControl(AbstractXMLUtility service)
	{
		SAXBuilder builder = null;
		List<SharedObject> sobject = new ArrayList<SharedObject>();
		List<SharedObject> sobject1 = new ArrayList<SharedObject>();
		List<SharedObject> sobject2 = new ArrayList<SharedObject>();
		List<SharedObject> sobject3 = new ArrayList<SharedObject>();

		boolean wvalid = true;
		boolean isvalidate = false;
		boolean xpath = false;
		boolean addEle = false;
		boolean textEle = false;
		boolean updText = false;
		boolean createXml = false;
		boolean delEle = false;
		boolean updEle = false;
		String tmp_str = null;
		  try
		  {
			  builder = new SAXBuilder();
		      Document doc = builder.build(new File(fileName));
		      Document doc1 = new Document();

		      // well-formed, validate 검사
		      WellformedValidate(wvalid, isvalidate, service);

		      // XPath 조회 모듈
		      XPathResult(xpath,service,doc);
		      // Element 삽입
		      addElement(addEle,service,doc,"person1",insertObject(sobject),tmp_str);
		      // Text Element 삽입
			  addTextElement(textEle,service,doc,"name",insertObject(sobject1),tmp_str);
			  // update TextElement
			  updTextElement(updText,service,doc, updTextObject(sobject2),tmp_str);
			  // XML 생성
			  createNewXML(createXml,service,doc1,"myfamily", insertObject(sobject3),tmp_str);
			  // Element 삭제
			  delElement(delEle,service,doc,"person1",tmp_str);
			  // update Element
			  updElement(updEle,service,doc, "person1", "사람1",tmp_str);
			  }catch(Exception e)
		  {
			  e.printStackTrace();
		  }
	}
    /**
     * Well-Formed,Validation 검사
     * @param used - 실행여부
     * @param isvalid - Validation 검사여부
     * @param service - XMLUtility
     * @throws ValidatorException
     */
    public void WellformedValidate(boolean used,boolean isvalid,AbstractXMLUtility service) throws ValidatorException, IOException, SAXException {
    	if(used)
    	{
    		 if( service.parse(isvalid))
    		 {
    			 if(isvalid)
    			   LOGGER.debug("Validation 문서입니다.");
    			 else
    			   LOGGER.debug("well-formed 문서입니다.");
    		 }
    	}
    }

    /**
     * XPath 조회 결과
     * @param used - 실행여부
     * @param service - XMLUtility
     * @param doc - Document 객체
     * @throws JDOMException
     */
    public  void XPathResult(boolean used,AbstractXMLUtility service,Document doc) throws JDOMException
	{
		if(used)
		{
		  List<?> list = service.getResult(doc,"//*[@*]");
		  viewEelement(list);
		}
	}
	/**
	 * Element 추가
	 * @param used - 실행여부
	 * @param service - XMLUtility
	 * @param doc - Document 객체
	 * @param EleName - Element 명
	 * @param list - 추가 Element List
	 * @param path - 생성될 XML문서 경로
	 * @throws JDOMException
	 * @throws TransformerException
	 * @throws FileNotFoundException
	 */
	public  void addElement(boolean used,AbstractXMLUtility service,Document doc,String EleName,List<?> list,String path)
	 throws JDOMException,TransformerException,FileNotFoundException,IOException
	{
		if(used)
		 service.addElement(doc,EleName,list,path);
	}
	/**
	 * TextNode Element 추가
	 * @param used - 실행여부
	 * @param service - XMLUtility
	 * @param doc - Document 객체
	 * @param elemName - TextElement 명
	 * @param list - 추가 Element List
	 * @param path - 생성될 XML문서 경로
	 * @throws JDOMException
	 * @throws TransformerException
	 * @throws FileNotFoundException
	 */
	public void addTextElement(boolean used,AbstractXMLUtility service,Document doc,String elemName,List<?> list,String path)
	throws JDOMException,TransformerException,FileNotFoundException,IOException
	{
		if(used)
			 service.addTextElement(doc,elemName,list,path);
	}
	/**
	 * TextNode Element 수정
	 * @param used - 실행여부
	 * @param service - XMLUtility
	 * @param doc - Document 객체
	 * @param list - 수정 Element List
	 * @param path - 생성될 XML문서 경로
	 * @throws JDOMException
	 * @throws TransformerException
	 * @throws FileNotFoundException
	 */
	public void updTextElement(boolean used,AbstractXMLUtility service,Document doc,List<?> list,String path)
	throws JDOMException,TransformerException,FileNotFoundException,IOException
	{
		if(used)
			 service.updTextElement(doc,list,path);
	}
	/**
	 * XML생성
	 * @param used - 실행여부
	 * @param service - XMLUtility
	 * @param doc - Document 객체
	 * @param EleName - Root 명
	 * @param list - 생성 Element List
	 * @param path - 생성될 XML문서 경로
	 * @throws JDOMException
	 * @throws TransformerException
	 * @throws FileNotFoundException
	 */
    public void createNewXML(boolean used,AbstractXMLUtility service,Document doc,String EleName,List<?> list,String path)
    throws JDOMException,TransformerException,FileNotFoundException,IOException
    {
    	if(used)
    	  service.createNewXML(doc,EleName, list,path);
    }
	/**
	 * Element 삭제
	 * @param used - 실행여부
	 * @param service - XMLUtility
	 * @param doc - Document 객체
	 * @param EleName - 삭제 Element 명
	 * @param path - 생성될 XML문서 경로
	 * @throws JDOMException
	 * @throws TransformerException
	 * @throws FileNotFoundException
	 */
    public void delElement(boolean used,AbstractXMLUtility service,Document doc,String EleName,String path)
    throws JDOMException,TransformerException,FileNotFoundException,IOException
    {
    	if(used)
    		service.delElement(doc, EleName,path);
    }
    /**
     * Element 수정
     * @param used - 실행여부
     * @param service - XMLUtility
     * @param doc - Document 객체
     * @param oldElement - 수정할 Element 명
     * @param newElement - 수정 Element 명
     * @param path - 생성될 XML문서 경로
     * @throws JDOMException
     * @throws TransformerException
     * @throws FileNotFoundException
     */
    public void updElement(boolean used,AbstractXMLUtility service,Document doc,String oldElement, String newElement,String path)
    throws JDOMException,TransformerException,FileNotFoundException,IOException
    {
    	if(used)
    		service.updElement(doc, oldElement, newElement,path);
    }
    /**
     * List 내용 조회
     * @param list - Element List
     */
	public  void viewEelement(List<?> list)
    {
    	Iterator<?> i = list.iterator();
        while (i.hasNext()) {
        	Element element = (Element) i.next();
        	  List<?> attList = element.getAttributes();
        	  if (attList.size() != 0)
        	  {
                  // 역시 속성리스트를 다시 iterator 로 담고
                  Iterator<?> ii = attList.iterator();

                  while(ii.hasNext()) {
                          /** Attribute 파싱 **/
                          // iterator 로 부터 하나의 속성을 꺼내와서...
                          Attribute at = (Attribute)ii.next();
                          LOGGER.debug("attribute : {} attribute value : {}", at.getName(),  at.getValue());
                          LOGGER.debug("Element1 Name : {} Element1 Value : {}", (String)element.getName(), (String)element.getValue());
                  }        // end of while
            }        // end of 속성 if
            List<?> list2 = element.getChildren();
            if(list2.size() > 1)
            {
            	viewEelement(list2);
            }
    }
    }
	/**
	 * XML에 추가할  SharedObject List
	 * @param sobject - Element
	 * @return Element List
	 */
	 public  List<SharedObject> insertObject(List<SharedObject> sobject)
	  {
	  	sobject.add(new SharedObject("김종호","남편"));
	  	sobject.add(new SharedObject("손영선","아내"));
	  	sobject.add(new SharedObject("김재우","아들"));
	  	sobject.add(new SharedObject("보들이","강아지"));

	//  	for(SharedObject sob:sobject)
	//  		logger.debug("sobject getValue:" +(String)sob.getValue());

	  	return sobject;
	  }
	 /**
	  * XML 수정 SharedObject List
	  * @param sobject - Element
	  * @return Element List
	  */
	 public  List<SharedObject> updTextObject(List<SharedObject> sobject)
	  {
	  	sobject.add(new SharedObject("Deep","홍길동12"));
	  	sobject.add(new SharedObject("Kumar","을지문덕12"));
	  	sobject.add(new SharedObject("Deepali","신사임당12"));
	  	sobject.add(new SharedObject("pet","강아지1"));
	 /*
	  	for(SharedObject sob:sobject)
	  		System.out.println("sobject getValue:" +(String)sob.getValue());
	  */
	  	return sobject;
	  }
  }
