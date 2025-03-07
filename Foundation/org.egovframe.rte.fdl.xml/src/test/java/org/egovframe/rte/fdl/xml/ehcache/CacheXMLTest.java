package org.egovframe.rte.fdl.xml.ehcache;

import org.egovframe.rte.fdl.xml.EgovSAXValidatorService;
import org.egovframe.rte.fdl.xml.SharedObject;
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
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

/**
 * CategoryControllerTest is TestCase of CategoryController
 *
 * @author Byunghun Woo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/context-xmltest.xml"})
public class CacheXMLTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheXMLTest.class);

    @Resource(name = "domconcreteCont")
    EgovDOMFactoryServiceImpl domconcrete = null;

    @Resource(name = "saxconcreteCont")
    EgovSAXFactoryServiceImpl saxconcrete = null;

    String cacheServerIP;
    int cacheServerPort = 0;
    String Storekey;
    String Retrievekey;
    String XMLFileName;
    String XMLFileName1 = "spring/context-sql.xml";
    String fileName = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(XMLFileName1)).getFile();

    public void setXMLFileName(String XMLFileName) {
        this.XMLFileName = XMLFileName;
    }

    public void setPortNIp(String cacheServerIP, int cacheServerPort) {
        this.cacheServerIP = cacheServerIP;
        this.cacheServerPort = cacheServerPort; //64208
    }

    public void setStorekey(String Storekey) {
        this.Storekey = Storekey;
    }

    public void setRetrievekey(String Retrievekey) {
        this.Retrievekey = Retrievekey;
    }

    public void sendCacheServer(List<?> list) {
		SharedObject sObject = null;

		try (Socket socket = new Socket(cacheServerIP, cacheServerPort);
			 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			 ObjectInputStream ooi = new ObjectInputStream(socket.getInputStream())) {

			// 서버로 보낼 객체 생성
			sObject = new SharedObject(Storekey, list);
			oos.writeObject(sObject);
			oos.flush();

			// 서버로부터 응답 수신
			sObject = (SharedObject) ooi.readObject();

			LOGGER.debug("서버로부터 받은 메시지: {}", sObject.getValue());
		} catch (IOException | ClassNotFoundException e) {
			LOGGER.debug("##### CacheXMLTest sendCacheServer(List<?> list) Exeption >>> {}", e.getMessage());
		}
    }

    public SharedObject getCacheServer() {
		SharedObject sObject = null;

		try (Socket socket = new Socket(cacheServerIP, cacheServerPort);
			 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			 ObjectInputStream ooi = new ObjectInputStream(socket.getInputStream())) {

			// 서버로 보낼 객체 생성
			sObject = new SharedObject("*", Retrievekey);
			oos.writeObject(sObject);
			oos.flush();

			// 서버로부터 응답 수신
			sObject = (SharedObject) ooi.readObject();

		} catch (IOException | ClassNotFoundException e) {
			LOGGER.debug("##### CacheXMLTest getCacheServer() Exeption >>> {}", e.getMessage());
		}

		return sObject;
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

    @Test
    public void ModuleTest() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        EgovSAXValidatorService saxValidator = null;
        String cacheServerIP = "localhost";
        String Storekey = "1";
        int cacheServerPort = 64208;

        CacheXMLAgent cxa = new CacheXMLAgent();
        cxa.setPortNIp(cacheServerIP, cacheServerPort);
        cxa.setStorekey(Storekey);
        cxa.setXMLFileName(XMLFileName1);

		File xmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();

		saxValidator = saxconcrete.createSAXValidator();
		List<?> list = saxValidator.getResult(doc,"//*[@*]");

        // 1. 캐쉬에 저장
        cxa.sendCacheServer(list);
    }
}
