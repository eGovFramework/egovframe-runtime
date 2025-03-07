/*
 * Copyright 2009-2014 MOSPA(Ministry of Security and Public Administration).

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.fdl.xml;

import org.egovframe.rte.fdl.xml.exception.ValidatorException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.CharArrayReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * XML문서 파싱 작업시 공통적으로 사용하는 메소드를 포함하는 추상 클래스.
 * 파싱할 XML 문서를 받는 부분과 XML 문서 파싱 후 에러 메시지를 생성하는 부분으로 나뉜다.
 * 각 파서는 파싱하는 부분만이 다르기 때문에 parse() 메소드를 abstract 메소드로 가진다.
 *
 * @author 개발프레임웍크 실행환경 개발팀 김종호
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.03.17	김종호				최초생성
 * </pre>
 * @since 2009.03.17
 */
public abstract class AbstractXMLUtility {

    /**
     * 파일명
     **/
    private String fileName = null;
    /**
     * 스키마 파일명
     **/
    private String schmafileName = null;
    /**
     * xmlValue
     **/
    private String xmlValue = null;
    /**
     * ApplicationContext
     */
    private final ApplicationContext context;
    /**
     * XmlConfig class
     **/
    private final XmlConfig xmlConfig;
    /**
     * xml 설정관리 Class
     */
    private final String savedPath;
    /**
     * xml 설정 파일 경로
     */
    private final String configPath = "classpath*:spring/egovxmlCfg.xml";

    /**
     * AbstractXMLUtility 생성자
     */
    public AbstractXMLUtility() {
        context = new FileSystemXmlApplicationContext(configPath);
        xmlConfig = (XmlConfig) context.getBean("xmlconfig");
        savedPath = xmlConfig.getXmlpath();
    }

    /**
     * XML 문서 경로 리턴
     *
     * @return XML문서 경로
     */
    public String getXMLFile() {
        return fileName;
    }

    /**
     * XML 문서가 파일로 존재할 경우 파일 이름을 전달
     *
     * @param fileName - xml 경로
     */
    public void setXMLFile(String fileName) {
        this.fileName = fileName;
    }

    /**
     * XML SCHEMA 문서경로 리턴
     *
     * @return XML SCHEMA 문서
     */
    public String getSCHEMAFile() {
        return schmafileName;
    }

    /**
     * XML SCHEMA 문서가 파일로 존재할 경우 파일 이름을 전달
     *
     * @param schmafileName - 스키마파일 경로
     */
    public void setSCHEMAFile(String schmafileName) {
        this.schmafileName = schmafileName;
    }

    /**
     * XML문서 String 리턴
     *
     * @return XML문서 String
     */
    public String getXML() {
        return xmlValue;
    }

    /**
     * XML 문서가 String으로 존재할 경우 String 객체를 전달
     *
     * @param xmlValue - XML문서 String형
     */
    public void setXML(String xmlValue) {
        this.xmlValue = xmlValue;
    }

    /**
     * 전달된 String을 파싱이 가능한 InputSource로 변환
     *
     * @return XML 문서 InputStream
     */
    protected InputSource stringToInputSource() {
        char[] xml = getXML().toCharArray();
        CharArrayReader reader = new CharArrayReader(xml);
        return new InputSource(reader);
    }

    /**
     * DOM, SAX 등의 XML 파서에 따라 파싱 작업을 실행할 메소드를 정의하는 추상 메소드
     *
     * @param isValid - Validation 검사 여부
     */
    public abstract boolean parse(boolean isValid) throws IOException, SAXException, ValidatorException;

    /**
     * XML 문서 파싱 완료 후 에러가 발생하여 에러 메시지가 존재할 경우 에러 메시지를 생성
     *
     * @param errorReport - 에러내용 리스트 객체
     */
    protected void makeErrorMessage(Set<?> errorReport)
            throws ValidatorException {
        StringBuilder errorMessage = new StringBuilder();
        Iterator<?> iterator = errorReport.iterator();
        while (iterator.hasNext()) {
            String tmp = (String) iterator.next();
            errorMessage.append(tmp);
            errorMessage.append("<br/>");
            iterator.remove();
        }
        throw new ValidatorException(errorMessage.toString());
    }

    public List<Node> getResult(Document doc, String exps)
            throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.evaluate(exps, doc, XPathConstants.NODESET);
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes.add(nodeList.item(i));
        }
        return nodes;
    }

    public void addElement(Document doc, String addNDName, List<SharedObject> list, String path)
            throws TransformerException, IOException {
        Element root = doc.getDocumentElement();
        addNode(root, addNDName, list);
        saveDocument(doc, path != null ? path : savedPath + "addElement.xml");
    }

    public void addTextElement(Document doc, String addNDName, List<SharedObject> list, String path)
            throws TransformerException, IOException {
        Element root = doc.getDocumentElement();
        addTextNode(root, addNDName, list);
        saveDocument(doc, path != null ? path : savedPath + "addElement.xml");
    }

    public void addTextNode(Element element, String addNDName, List<SharedObject> list) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(addNDName)) {
                for (SharedObject sobj : list) {
                    child.setTextContent(sobj.getValue().toString());
                }
            }
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                addTextNode((Element) child, addNDName, list);
            }
        }
    }

    public void addNode(Element element, String addNDName, List<SharedObject> list) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(addNDName)) {
                for (SharedObject sobj : list) {
                    Element newElement = element.getOwnerDocument().createElement(sobj.getKey());
                    newElement.setTextContent(sobj.getValue().toString());
                    child.appendChild(newElement);
                }
            }
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                addNode((Element) child, addNDName, list);
            }
        }
    }

    public void updElement(Document doc, String oldNodename, String newNodeName, String path) throws IOException, TransformerException {
        Element root = doc.getDocumentElement();
        chgNode(doc, root, oldNodename, newNodeName);
        saveDocument(doc, path != null ? path : savedPath + "updElement.xml");
    }

    public void updTextElement(Document doc, List<SharedObject> list, String path) throws IOException, TransformerException {
        Element root = doc.getDocumentElement();
        updTextNode(root, list);
        saveDocument(doc, path != null ? path : savedPath + "updTEXT.xml");
    }

    public void updTextNode(Element element, List<SharedObject> list) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            for (SharedObject sobj : list) {
                Element newElement = element.getOwnerDocument().createElement(sobj.getKey());
                newElement.setTextContent(sobj.getValue().toString());
                child.appendChild(newElement);
            }
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                updTextNode((Element) child, list);
            }
        }
    }

    public void removeNode(Element element, String name) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(name)) {
                element.removeChild(child);
                i--; // Adjust index after removal
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                removeNode((Element) child, name);
            }
        }
    }

    public void delElement(Document doc, String nodeName, String path) throws TransformerException, IOException {
        Element root = doc.getDocumentElement();
        removeNode(root, nodeName);
        saveDocument(doc, path != null ? path : savedPath + "delXML.xml");
    }

    public void chgNode(Document doc, Element element, String oldNode, String newNode) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(oldNode)) {
                doc.renameNode(child, null, newNode);
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                chgNode(doc, (Element) child, oldNode, newNode);
            }
        }
    }

    public void createNewXML(Document doc, String newNodeName, List<SharedObject> list, String path) throws TransformerException, IOException {
        Element root = doc.createElement(newNodeName);
        doc.appendChild(root);
        createXML(root, list);
        saveDocument(doc, path != null ? path : savedPath + "newXML.xml");
    }

    public void createXML(Element root, List<SharedObject> list) {
        for (SharedObject sobj : list) {
            Element elm = root.getOwnerDocument().createElement(sobj.getKey());
            elm.setTextContent(sobj.getValue().toString());
            root.appendChild(elm);
        }
    }

    private void saveDocument(Document doc, String path) throws TransformerException, IOException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(Files.newOutputStream(Paths.get(path)));
        transformer.transform(source, result);
    }

}
