package org.egovframe.rte.fdl.xml;

import org.egovframe.rte.fdl.xml.exception.ValidatorException;
import org.egovframe.rte.fdl.xml.impl.EgovDOMFactoryServiceImpl;
import org.egovframe.rte.fdl.xml.impl.EgovSAXFactoryServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * CategoryControllerTest is TestCase of CategoryController
 *
 * @author Byunghun Woo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/context-xmltest.xml"})
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlXMLTest.class);

    /**
     * EgovAbstractXMLFactoryService 상속한 Class
     **/
    @Resource(name = "domconcreteCont")
    EgovDOMFactoryServiceImpl domconcrete = null;

    /**
     * EgovAbstractXMLFactoryService 상속한 Class
     **/
    @Resource(name = "saxconcreteCont")
    EgovSAXFactoryServiceImpl saxconcrete = null;

    /**
     * AbstractXMLUtility 상속한 DOMValidator
     **/
    EgovDOMValidatorService domValidator = null;

    /**
     * AbstractXMLUtility 상속한 SAXValidator
     **/
    EgovSAXValidatorService saxValidator = null;

    /**
     * 테스트 XML 문서
     **/
    String fileName = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("spring/context-test.xml")).getFile();

    /**
     * DOM,SAX Validator 생성
     */
    @Test
    public void ModuleTest()
            throws XPathExpressionException, ParserConfigurationException, IOException, TransformerException, SAXException {
        saxValidator = saxconcrete.createSAXValidator();
        LOGGER.debug("fileName : {}", fileName);
        saxValidator.setXMLFile(fileName);
        ModuleControl(saxValidator);
    }

    /**
     * ModuleControl 생성자
     *
     * @param service - XMLUtility
     */
    public void ModuleControl(AbstractXMLUtility service)
            throws ParserConfigurationException, XPathExpressionException, IOException, TransformerException, SAXException {
        List<SharedObject> sobject = new ArrayList<>();
        List<SharedObject> sobject1 = new ArrayList<>();
        List<SharedObject> sobject2 = new ArrayList<>();
        List<SharedObject> sobject3 = new ArrayList<>();

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

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
		Document doc = null;
		Document doc1 = null;

		builder = factory.newDocumentBuilder();
		File xmlFile = new File(fileName);
		doc = builder.parse(xmlFile);

		// XPath 조회 모듈
		XPathResult(xpath, service, doc);
		// Element 삽입
		addElement(addEle, service, doc, "person1", insertObject(sobject), tmp_str);
		// Text Element 삽입
		addTextElement(textEle, service, doc, "name", insertObject(sobject1), tmp_str);
		// update TextElement
		updTextElement(updText, service, doc, updTextObject(sobject2), tmp_str);
		// XML 생성
		createNewXML(createXml, service, doc1, "myfamily", insertObject(sobject3), tmp_str);
		// Element 삭제
		delElement(delEle, service, doc, "person1", tmp_str);
		// update Element
		updElement(updEle, service, doc, "person1", "사람1", tmp_str);
    }

    /**
     * Well-Formed,Validation 검사
     *
     * @param used    - 실행여부
     * @param isvalid - Validation 검사여부
     * @param service - XMLUtility
     */
    public void WellformedValidate(boolean used, boolean isvalid, AbstractXMLUtility service)
            throws ValidatorException, IOException, SAXException {
		if (used) {
			if (service.parse(isvalid)) {
				if (isvalid) {
					LOGGER.debug("Validation 문서입니다.");
				} else {
					LOGGER.debug("well-formed 문서입니다.");
				}
			}
		}
    }

    /**
     * XPath 조회 결과
     *
     * @param used    - 실행여부
     * @param service - XMLUtility
     * @param doc     - Document 객체
     */
    public void XPathResult(boolean used, AbstractXMLUtility service, Document doc)
            throws XPathExpressionException {
		if (used) {
			List<?> list = null;
			list = service.getResult(doc, "//*[@*]");
			viewElement((Element) list);
		}
    }

    /**
     * Element 추가
     *
     * @param used    - 실행여부
     * @param service - XMLUtility
     * @param doc     - Document 객체
     * @param EleName - Element 명
     * @param list    - 추가 Element List
     * @param path    - 생성될 XML문서 경로
     */
    public void addElement(boolean used, AbstractXMLUtility service, Document doc, String EleName, List<SharedObject> list, String path)
            throws IOException, TransformerException {
        if (used) service.addElement(doc, EleName, list, path);
    }

    /**
     * TextNode Element 추가
     *
     * @param used     - 실행여부
     * @param service  - XMLUtility
     * @param doc      - Document 객체
     * @param elemName - TextElement 명
     * @param list     - 추가 Element List
     * @param path     - 생성될 XML문서 경로
     */
    public void addTextElement(boolean used, AbstractXMLUtility service, Document doc, String elemName, List<SharedObject> list, String path)
            throws IOException, TransformerException {
        if (used) service.addTextElement(doc, elemName, list, path);
    }

    /**
     * TextNode Element 수정
     *
     * @param used    - 실행여부
     * @param service - XMLUtility
     * @param doc     - Document 객체
     * @param list    - 수정 Element List
     * @param path    - 생성될 XML문서 경로
     */
    public void updTextElement(boolean used, AbstractXMLUtility service, Document doc, List<SharedObject> list, String path)
            throws IOException, TransformerException {
        if (used) service.updTextElement(doc, list, path);
    }

    /**
     * XML생성
     *
     * @param used    - 실행여부
     * @param service - XMLUtility
     * @param doc     - Document 객체
     * @param eleName - Root 명
     * @param list    - 생성 Element List
     * @param path    - 생성될 XML문서 경로
     */
    public void createNewXML(boolean used, AbstractXMLUtility service, Document doc, String eleName, List<SharedObject> list, String path)
            throws IOException, TransformerException {
        if (used) service.createNewXML(doc, eleName, list, path);
    }

    /**
     * Element 삭제
     *
     * @param used    - 실행여부
     * @param service - XMLUtility
     * @param doc     - Document 객체
     * @param eleName - 삭제 Element 명
     * @param path    - 생성될 XML문서 경로
     */
    public void delElement(boolean used, AbstractXMLUtility service, Document doc, String eleName, String path)
            throws IOException, TransformerException {
        if (used) service.delElement(doc, eleName, path);
    }

    /**
     * Element 수정
     *
     * @param used       - 실행여부
     * @param service    - XMLUtility
     * @param doc        - Document 객체
     * @param oldElement - 수정할 Element 명
     * @param newElement - 수정 Element 명
     * @param path       - 생성될 XML문서 경로
     */
    public void updElement(boolean used, AbstractXMLUtility service, Document doc, String oldElement, String newElement, String path)
            throws IOException, TransformerException {
        if (used) service.updElement(doc, oldElement, newElement, path);
    }

    /**
     * XML에 추가할  SharedObject List
     *
     * @param sobject - Element
     * @return Element List
     */
    public List<SharedObject> insertObject(List<SharedObject> sobject) {
        sobject.add(new SharedObject("김종호", "남편"));
        sobject.add(new SharedObject("손영선", "아내"));
        sobject.add(new SharedObject("김재우", "아들"));
        sobject.add(new SharedObject("보들이", "강아지"));
        return sobject;
    }

    /**
     * XML 수정 SharedObject List
     *
     * @param sobject - Element
     * @return Element List
     */
    public List<SharedObject> updTextObject(List<SharedObject> sobject) {
        sobject.add(new SharedObject("Deep", "홍길동12"));
        sobject.add(new SharedObject("Kumar", "을지문덕12"));
        sobject.add(new SharedObject("Deepali", "신사임당12"));
        sobject.add(new SharedObject("pet", "강아지1"));
        return sobject;
    }

    public void viewElement(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        if (attributes.getLength() > 0) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr attr = (Attr) attributes.item(i);
                LOGGER.info(String.format("Attribute : %s, Attribute value : %s", attr.getName(), attr.getValue()));
            }
            LOGGER.info(String.format("Element Name: %s, Element Value: %s", element.getNodeName(), element.getTextContent()));
        }

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode instanceof Element) {
                viewElement((Element) childNode);
            }
        }
    }

}
