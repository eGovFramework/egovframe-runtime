package org.egovframe.rte.fdl.xml.ehcache;

import org.egovframe.rte.fdl.xml.SharedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class CacheXMLAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheXMLAgent.class);

    String cacheServerIP;
    int cacheServerPort = 0;
    String Storekey;
    String Retrievekey;
    String XMLFileName;

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
		LOGGER.debug("CacheXMLAgent cacheServerIP >>> {}", cacheServerIP);
		LOGGER.debug("CacheXMLAgent cacheServerPort >>> {}", cacheServerPort);
		LOGGER.debug("CacheXMLAgent Storekey >>> {}", Storekey);

		try (Socket socket = new Socket(cacheServerIP, cacheServerPort);
			 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			 ObjectInputStream ooi = new ObjectInputStream(socket.getInputStream())) {

			SharedObject sObject = new SharedObject(Storekey, list);
			oos.writeObject(sObject);
			oos.flush();

			SharedObject response = (SharedObject) ooi.readObject();
			LOGGER.debug("CacheXMLAgent Message from server: {}", response.getValue());

		} catch (ClassNotFoundException | IOException e) {
			LOGGER.debug("CacheXMLAgent sendCacheServer(List<?> list) Exeption >>> {}", e.getMessage());
		}
    }

    public SharedObject getCacheServer() {
		LOGGER.debug("CacheXMLAgent getCacheServer cacheServerIP >>> {}", cacheServerIP);
		LOGGER.debug("CacheXMLAgent getCacheServer cacheServerPort >>> {}", cacheServerPort);
		LOGGER.debug("CacheXMLAgent getCacheServer Storekey >>> {}", Storekey);

		SharedObject sObject = null;

		try (Socket socket = new Socket(cacheServerIP, cacheServerPort);
			 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			 ObjectInputStream ooi = new ObjectInputStream(socket.getInputStream())) {

			sObject = new SharedObject("*", Retrievekey);
			oos.writeObject(sObject);
			oos.flush();

			sObject = (SharedObject) ooi.readObject();

		} catch (ClassNotFoundException | IOException e) {
			LOGGER.debug("CacheXMLAgent getCacheServer() Exeption >>> {}", e.getMessage());
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

	public static void main(String[] args) {
		try {
			String cacheServerIP = "192.168.100.162";
			String storeKey = "1";
			String xmlFileName = "spring/context-sql.xml";
			int cacheServerPort = 64208;

			File xmlFile = new File(xmlFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			CacheXMLAgent cxa = new CacheXMLAgent();
			Element rootElement = doc.getDocumentElement();
			cxa.viewElement(rootElement);
		} catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

}
