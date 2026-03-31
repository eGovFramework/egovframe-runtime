package org.egovframe.rte.fdl.xml;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.xml.config.XmlTestConfig;
import org.egovframe.rte.fdl.xml.exception.ValidatorException;
import org.egovframe.rte.fdl.xml.impl.EgovDOMFactoryServiceImpl;
import org.egovframe.rte.fdl.xml.impl.EgovSAXFactoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = XmlTestConfig.class)
/**
 * XML Manipulation Test
 * @
 * @  수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2009.03.18    김종호        최초생성
 *
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
    String fileName = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("META-INF/spring/context-test.xml")).getFile();

    /**
     * DOM,SAX Validator 생성
     */
    @Test
    public void ModuleTest() throws XPathExpressionException, ParserConfigurationException, IOException, TransformerException, SAXException {
        saxValidator = saxconcrete.createSAXValidator();
        LOGGER.debug("### ControlXMLTest ModuleTest() fileName : {}", fileName);
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
     */
    public void WellformedValidate(boolean used, boolean isvalid, AbstractXMLUtility service)
            throws ValidatorException, IOException, SAXException {
        if (used) {
            if (service.parse(isvalid)) {
                if (isvalid) {
                    LOGGER.debug("### ControlXMLTest WellformedValidate() Validation 문서입니다.");
                } else {
                    LOGGER.debug("### ControlXMLTest WellformedValidate() well-formed 문서입니다.");
                }
            }
        }
    }

    /**
     * XPath 조회 결과
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
     */
    public void addElement(boolean used, AbstractXMLUtility service, Document doc, String EleName, List<SharedObject> list, String path)
            throws IOException, TransformerException {
        if (used) service.addElement(doc, EleName, list, path);
    }

    /**
     * TextNode Element 추가
     */
    public void addTextElement(boolean used, AbstractXMLUtility service, Document doc, String elemName, List<SharedObject> list, String path)
            throws IOException, TransformerException {
        if (used) service.addTextElement(doc, elemName, list, path);
    }

    /**
     * TextNode Element 수정
     */
    public void updTextElement(boolean used, AbstractXMLUtility service, Document doc, List<SharedObject> list, String path)
            throws IOException, TransformerException {
        if (used) service.updTextElement(doc, list, path);
    }

    /**
     * XML생성
     */
    public void createNewXML(boolean used, AbstractXMLUtility service, Document doc, String eleName, List<SharedObject> list, String path)
            throws IOException, TransformerException {
        if (used) service.createNewXML(doc, eleName, list, path);
    }

    /**
     * Element 삭제
     */
    public void delElement(boolean used, AbstractXMLUtility service, Document doc, String eleName, String path)
            throws IOException, TransformerException {
        if (used) service.delElement(doc, eleName, path);
    }

    /**
     * Element 수정
     */
    public void updElement(boolean used, AbstractXMLUtility service, Document doc, String oldElement, String newElement, String path)
            throws IOException, TransformerException {
        if (used) service.updElement(doc, oldElement, newElement, path);
    }

    /**
     * XML에 추가할 SharedObject List
     */
    public List<SharedObject> insertObject(List<SharedObject> sobject) {
        sobject.add(new SharedObject("신영식", "아빠"));
        sobject.add(new SharedObject("봉미선", "엄마"));
        sobject.add(new SharedObject("신짱아", "동생"));
        sobject.add(new SharedObject("흰둥이", "강아지"));
        return sobject;
    }

    /**
     * XML 수정 SharedObject List
     */
    public List<SharedObject> updTextObject(List<SharedObject> sobject) {
        sobject.add(new SharedObject("Deep", "홍길동12"));
        sobject.add(new SharedObject("Kumar", "을지문덕12"));
        sobject.add(new SharedObject("Deepali", "신사임당12"));
        sobject.add(new SharedObject("pet", "강아지12"));
        return sobject;
    }

    public void viewElement(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        if (attributes.getLength() > 0) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr attr = (Attr) attributes.item(i);
                LOGGER.debug(String.format("Attribute : %s, Attribute value : %s", attr.getName(), attr.getValue()));
            }
            LOGGER.debug(String.format("Element Name: %s, Element Value: %s", element.getNodeName(), element.getTextContent()));
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
