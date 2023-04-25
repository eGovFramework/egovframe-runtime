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

import org.egovframe.rte.fdl.logging.util.EgovResourceReleaser;
import org.egovframe.rte.fdl.xml.exception.ValidatorException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.transform.JDOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import java.io.CharArrayReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * XML문서 파싱 작업시 공통적으로 사용하는 메소드를 포함하는 추상 클래스.
 * 파싱할 XML 문서를 받는 부분과 XML 문서 파싱 후 에러 메시지를 생성하는 부분으로 나뉜다. 
 * 각 파서는 파싱하는 부분만이 다르기 때문에 parse() 메소드를 abstract 메소드로 가진다.
 * 
 * @author 개발프레임웍크 실행환경 개발팀 김종호
 * @since 2009.03.17
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.03.17	김종호				최초생성
 * </pre>
 */
public abstract class AbstractXMLUtility {

	/** 파일명 **/
	private String fileName = null;
	/** 스키마 파일명 **/
	private String schmafileName = null;
	/** xmlValue **/
	private String xmlValue = null;
	/** ApplicationContext */
	private ApplicationContext context;
	/** XmlConfig class **/
	private XmlConfig xmlConfig;
	/** xml 설정관리 Class */
	private String savedPath;
	/** xml 설정 파일 경로 */
	private String configPath = "classpath*:spring/egovxmlCfg.xml";

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractXMLUtility.class);

	/**
	 * AbstractXMLUtility 생성자
	 */
	public AbstractXMLUtility() {
		context = new FileSystemXmlApplicationContext(configPath);
		xmlConfig = (XmlConfig) context.getBean("xmlconfig");
		savedPath = xmlConfig.getXmlpath();
	}

	/**
	 * XML 문서가 파일로 존재할 경우 파일 이름을 전달
	 * @param fileName - xml 경로
	 */
	public void setXMLFile(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * XML 문서 경로 리턴
	 * @return XML문서 경로
	 */
	public String getXMLFile() {
		return fileName;
	}

	/**
	 * XML SCHEMA 문서가 파일로 존재할 경우 파일 이름을 전달
	 * @param schmafileName - 스키마파일 경로
	 */
	public void setSCHEMAFile(String schmafileName) {
		this.schmafileName = schmafileName;
	}

	/**
	 * XML SCHEMA 문서경로 리턴
	 * @return XML SCHEMA 문서
	 */
	public String getSCHEMAFile() {
		return schmafileName;
	}

	/**
	 * XML 문서가 String으로 존재할 경우 String 객체를 전달
	 * @param xmlValue - XML문서 String형
	 */
	public void setXML(String xmlValue) {
		this.xmlValue = xmlValue;
	}

	/**
	 * XML문서 String 리턴
	 * @return XML문서 String
	 */
	public String getXML() {
		return xmlValue;
	}

	/**
	 * 전달된 String을 파싱이 가능한 InputSource로 변환
	 * @return XML 문서 InputStream
	 */
	protected InputSource stringToInputSource() {
		char[] xml = getXML().toCharArray();
		CharArrayReader reader = new CharArrayReader(xml);
		return new InputSource(reader);
	}

	/**
	 * DOM, SAX 등의 XML 파서에 따라 파싱 작업을 실행할 메소드를 정의하는 추상 메소드
	 * @param isValid - Validation 검사 여부
	 * @throws ValidatorException
	  */
	public abstract boolean parse(boolean isValid) throws IOException, SAXException, ValidatorException;

	/**
	 * XML 문서 파싱 완료 후 에러가 발생하여 에러 메시지가 존재할 경우 에러 메시지를 생성
	 * @param errorReport - 에러내용 리스트 객체
	 * @throws ValidatorException
	 */
	protected void makeErrorMessage(Set<?> errorReport) throws ValidatorException {
		StringBuffer errorMessage = new StringBuffer();
		Iterator<?> iterator = errorReport.iterator();
		while (iterator.hasNext()) {
			String tmp = (String) iterator.next();
			errorMessage.append(tmp);
			errorMessage.append("<br/>");
			iterator.remove();
		}
		throw new ValidatorException(errorMessage.toString());
	}

	@SuppressWarnings("deprecation")
	public List<?> getResult(Document doc, String exps) throws JDOMException {
		org.jdom2.xpath.XPath xPath = org.jdom2.xpath.XPath.newInstance(exps);
		return xPath.selectNodes(doc);
	}

	/**
	 * 신규 Element 추가
	 * @param doc - Document 객체
	 * @param addNDName - 추가할 대상 Element명
	 * @param list - 추가할 Element 리스트
	 * @param path - 신규 생설할 XML문서 경로
	 * @throws TransformerConfigurationException
	 */
	public void addElement(Document doc, String addNDName, List<?> list, String path) throws TransformerConfigurationException {
		StreamResult sTResult = null;
		FileOutputStream fos = null;
		Element root = doc.getRootElement();
		addNode(root, addNDName, list);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer serializer = transformerFactory.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "euc-kr");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");

		try {
			if (path != null) {
				fos = new FileOutputStream(path);
			} else {
				fos = new FileOutputStream(savedPath + "addElement.xml");
			}
			sTResult = new StreamResult(fos);
			serializer.transform(new JDOMSource(doc), sTResult);
		} catch (FileNotFoundException e) {
			ReflectionUtils.handleReflectionException(e);
		} catch (TransformerException e) {
			ReflectionUtils.handleReflectionException(e);
		} finally {
			EgovResourceReleaser.close(fos);
		}
	}

	/**
	 * 신규 TextElement 추가
	 * @param doc - Document 객체
	 * @param addNDName - 추가할 대상 Elememt명
	 * @param list - 추가할 TextElement 리스트
	 * @param path
	 */
	public void addTextElement(Document doc, String addNDName, List<?> list, String path) throws TransformerException, IOException {
		StreamResult sTResult = null;
		FileOutputStream fos = null;
		Element root = doc.getRootElement();
		addTextNode(root, addNDName, list);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer serializer = transformerFactory.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "euc-kr");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");

		try {
			if (path != null) {
				fos = new FileOutputStream(path);
			} else {
				fos = new FileOutputStream(savedPath + "addElement.xml");
			}
			sTResult = new StreamResult(fos);
			serializer.transform(new JDOMSource(doc), sTResult);
		} finally {
			EgovResourceReleaser.close(fos);
		}
	}

	/**
	 * TextNode 추가
	 * @param element - 조회대상 Element
	 * @param addNDName - 추가할대상 Element명
	 * @param list - 추가할 TextElement 리스트
	 */
	public void addTextNode(Element element, String addNDName, List<?> list) {
		List<?> childList = element.getChildren();

		if (childList.size() != 0) {
			for (int yy = 0; yy < childList.size(); yy++) {
				Element tmp = (Element) childList.get(yy);
				if (tmp.getName().equals(addNDName)) {
					for (int tt = 0; tt < list.size(); tt++) {
						SharedObject sobj = (SharedObject) list.get(tt);
						tmp.addContent((String) sobj.getValue());
					}
				}
				addTextNode(tmp, addNDName, list);
			}
		} else {
			if (element.getName().equals(addNDName)) {
				for (int tt = 0; tt < list.size(); tt++) {
					SharedObject sobj = (SharedObject) list.get(tt);
					element.addContent((String) sobj.getValue());
				}
			}
		}
	}

	/**
	 * TextElement Update
	 * @param doc - Document 객체
	 * @param list - update TextNode 리스트
	 * @param path - 신규생성 XML문서 경로
	 */
	public void updTextElement(Document doc, List<?> list, String path) throws TransformerException, IOException {
		StreamResult sTResult = null;
		FileOutputStream fos = null;
		Element root = doc.getRootElement();
		updTextNode(root, list);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer serializer = transformerFactory.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "euc-kr");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");

		try {
			if (path != null) {
				fos = new FileOutputStream(path);
			} else {
				fos = new FileOutputStream(savedPath + "updTEXT.xml");
			}
			sTResult = new StreamResult(fos);
			serializer.transform(new JDOMSource(doc), sTResult);
		} finally {
			EgovResourceReleaser.close(fos);
		}
	}

	/**
	 * TextNode Update
	 * @param element - 조회대상 Element
	 * @param list - update TextNode 리스트
	 */
	public void updTextNode(Element element, List<?> list) {
		List<?> childList = element.getChildren();

		if (childList.size() != 0) {
			for (int yy = 0; yy < childList.size(); yy++) {
				Element tmp = (Element) childList.get(yy);
				for (int tt = 0; tt < list.size(); tt++) {
					SharedObject sobj = (SharedObject) list.get(tt);
					if (tmp.getValue().equals((String) sobj.getKey())) {
						tmp.setText((String) sobj.getValue());
						break;
					}
				}
				updTextNode(tmp, list);
			}
		} else {
			for (int tt = 0; tt < list.size(); tt++) {
				SharedObject sobj = (SharedObject) list.get(tt);
				if (element.getValue().equals((String) sobj.getKey())) {
					element.setText((String) sobj.getValue());
					break;
				}
			}
		}
	}

	/**
	 * 신규 Node 추가
	 * @param element - 조회대상 Element
	 * @param addNDName - 노드이름
	 * @param list - add TextNode 리스트
	 */
	public void addNode(Element element, String addNDName, List<?> list) {
		List<?> childList = element.getChildren();

		if (childList.size() != 0) {
			for (int yy = 0; yy < childList.size(); yy++) {
				Element tmp = (Element) childList.get(yy);
				if (tmp.getName().equals(addNDName)) {
					for (int tt = 0; tt < list.size(); tt++) {
						SharedObject sobj = (SharedObject) list.get(tt);
						Element enew = new Element((String) sobj.getKey());
						enew.setText((String) sobj.getValue());
						tmp.addContent(enew);
					}
				}
				addNode(tmp, addNDName, list);
			}
		} else {
			if (element.getName().equals(addNDName)) {
				for (int tt = 0; tt < list.size(); tt++) {
					SharedObject sobj = (SharedObject) list.get(tt);
					Element enew = new Element((String) sobj.getKey());
					enew.setText((String) sobj.getValue());
					element.addContent(enew);
				}
			}
		}
	}

	/**
	 * 노드삭제
	 * @param element - 조회대상 Element
	 * @param name - 노드명
	 */
	public void removeNode(Element element, String name) {
		List<?> list = element.getChildren();

		if (list.size() != 0) {
			for (int i = 0; i < list.size(); i++) {
				Element tmp = (Element) list.get(i);
				if (tmp.getName().equals(name)) {
					tmp.getParentElement().removeChild(name);
				} else {
					removeNode(tmp, name);
				}
			}
		} else {
			if (element.getName().equals(name)) {
				element.getParentElement().removeChild(name);
			}
		}
	}

	/**
	 * Element 삭제
	 * @param doc - document 객체
	 * @param nodeName - 노드명
	 * @param path - 신규생성 XML문서 경로
	 * @throws TransformerException
	 * @throws TransformerConfigurationException
	 * @throws FileNotFoundException
	 */
	public void delElement(Document doc, String nodeName, String path) throws TransformerException, IOException {
		StreamResult sTResult = null;
		FileOutputStream fos = null;
		Element element = doc.getRootElement();
		removeNode(element, nodeName);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer serializer = transformerFactory.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "euc-kr");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");

		try {
			if (path != null) {
				fos = new FileOutputStream(path);
			} else {
				fos = new FileOutputStream(savedPath + "delXML.xml");
			}
			sTResult = new StreamResult(fos);
			serializer.transform(new JDOMSource(doc), sTResult);
		} finally {
			EgovResourceReleaser.close(fos);
		}
	}

	/**
	 * 노드변경
	 * @param element - 조회대상 Element
	 * @param oldNode - 변경할 노드명
	 * @param newNodename - 신규 노드명
	 */
	public void chgNode(Element element, String oldNode, String newNodename) {
		List<?> list = element.getChildren();

		if (list.size() != 0) {
			for (int i = 0; i < list.size(); i++) {
				Element tmp = (Element) list.get(i);
				if (element.getName().equals(oldNode)) {
					element.setName(newNodename);
				} else {
					chgNode(tmp, oldNode, newNodename);
				}
			}
		} else {
			if (element.getName().equals(oldNode)) {
				element.setName(newNodename);
			}
		}
	}

	/**
	 * Element Update
	 * @param doc - document 객체
	 * @param oldNodename - 변경할 노드명
	 * @param newNodeName - 신규 노드명
	 * @param path - 신규생성 XML문서 경로
	 * @throws TransformerException
	 * @throws TransformerConfigurationException
	 * @throws FileNotFoundException
	  */
	public void updElement(Document doc, String oldNodename, String newNodeName, String path) throws TransformerException, IOException {
		StreamResult sTResult = null;
		FileOutputStream fos = null;
		Element root = doc.getRootElement();
		chgNode(root, oldNodename, newNodeName);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer serializer = transformerFactory.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "euc-kr");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");

		try {
			if (path != null) {
				fos = new FileOutputStream(path);
			} else {
				fos = new FileOutputStream(savedPath + "updElement.xml");
			}
			sTResult = new StreamResult(fos);
			serializer.transform(new JDOMSource(doc), sTResult);
		} finally {
			EgovResourceReleaser.close(fos);
		}
	}

	/**
	 * XML문서 생성
	 * @param doc - document 객체
	 * @param newNodeName - Root 노드명
	 * @param list - Element 리스트
	 * @param path - 신규생성 XML문서 경로
	 * @throws TransformerException
	 * @throws TransformerConfigurationException
	 * @throws FileNotFoundException
	 */
	public void createNewXML(Document doc, String newNodeName, List<?> list, String path) throws TransformerException, IOException {
		StreamResult sTResult = null;
		FileOutputStream fos = null;
		Element root = new Element(newNodeName);
		doc.addContent(root);
		createXML(root, list);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer serializer = transformerFactory.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "euc-kr");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");

		try {
			if (path != null) {
				fos = new FileOutputStream(path);
			} else {
				fos = new FileOutputStream(savedPath + "newXML.xml");
			}
			sTResult = new StreamResult(fos);
			serializer.transform(new JDOMSource(doc), sTResult);
		} finally {
			EgovResourceReleaser.close(fos);
		}
	}

	/**
	 * XML 생성
	 * 
	 * @param root - Root Element
	 * @param list - Element 리스트
	 */
	public void createXML(Element root, List<?> list) {
		for (int i = 0; i < list.size(); i++) {
			SharedObject sobj = (SharedObject) list.get(i);
			Element elm = new Element(sobj.getKey());
			elm.setText((String) sobj.getValue());
			root.addContent(elm);
		}
	}

}
