package org.egovframe.rte.fdl.xml.ehcache;


import org.egovframe.rte.fdl.xml.EgovSAXValidatorService;
import org.egovframe.rte.fdl.xml.SharedObject;
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

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

/**
 * CategoryControllerTest is TestCase of CategoryController
 *
 * @author Byunghun Woo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/context-xmltest.xml" })

public class CacheXMLTest {
	@Resource(name = "domconcreteCont")
	EgovDOMFactoryServiceImpl domconcrete = null;

	@Resource(name = "saxconcreteCont")
	EgovSAXFactoryServiceImpl saxconcrete = null;

	String cacheServerIP;
	int cacheServerPort=0;
	String Storekey;
	String Retrievekey;
	String XMLFileName;
	private static final Logger LOGGER  = LoggerFactory.getLogger(CacheXMLTest.class);
	String XMLFileName1 = "spring/context-sql.xml";
	String fileName = Thread.currentThread().getContextClassLoader().getResource(XMLFileName1).getFile();

	public void setXMLFileName(String XMLFileName)
	{
		this.XMLFileName = XMLFileName;
	}

    public void setPortNIp(String cacheServerIP,int cacheServerPort)
    {
    	this.cacheServerIP = cacheServerIP;
    	this.cacheServerPort = cacheServerPort; //64208
    }

    public void setStorekey(String Storekey)
    {
    	this.Storekey = Storekey;
    }

    public void setRetrievekey(String Retrievekey)
    {
    	this.Retrievekey = Retrievekey;
    }

    public void sendCacheServer(List<?> list)
	{
		Socket socket = null;
		ObjectOutputStream oos= null;
		ObjectInputStream ooi= null;
		SharedObject sObject = null;

		try {
			socket = new Socket(cacheServerIP, cacheServerPort);
			oos = new ObjectOutputStream(socket.getOutputStream());
			sObject = new SharedObject(Storekey,list);
			oos.writeObject(sObject);
			ooi = new ObjectInputStream(socket.getInputStream());
			sObject = (SharedObject)ooi.readObject();

			LOGGER.debug("서버로 부터 Message : {}", sObject.getValue());
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			try { oos.close(); } catch(Throwable t) {t.printStackTrace();}
			try { socket.close(); } catch(Throwable t) {t.printStackTrace();}
		}
	}

    public SharedObject getCacheServer()
	{
		Socket socket = null;
		ObjectOutputStream oos= null;
		ObjectInputStream ooi= null;
		SharedObject sObject = null;

		try {
			socket = new Socket(cacheServerIP, cacheServerPort);
			oos = new ObjectOutputStream(socket.getOutputStream());
			sObject = new SharedObject("*",Retrievekey);
			oos.writeObject(sObject);
			ooi = new ObjectInputStream(socket.getInputStream());
			sObject = (SharedObject)ooi.readObject();
		} catch(Throwable t) {
			t.printStackTrace();
			System.exit(1);
		} finally {
			try { oos.close(); } catch(Throwable t) {t.printStackTrace();}
			try { socket.close(); } catch(Throwable t) {t.printStackTrace();}
		}

		return sObject;
	}

    public void viewEelement(List<?> list)
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
					LOGGER.debug("attribute : {} attribute value : {}", at.getName(), at.getValue());
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

    @Test
    public void ModuleTest() throws IOException,JDOMException {
		EgovSAXValidatorService saxValidator = null;
		String cacheServerIP = "localhost";
		String Storekey = "1";
		int cacheServerPort = 64208;

		CacheXMLAgent cxa = new CacheXMLAgent();
		cxa.setPortNIp(cacheServerIP, cacheServerPort);
		cxa.setStorekey(Storekey);
		cxa.setXMLFileName(XMLFileName1);

	    saxValidator = saxconcrete.createSAXValidator();
		SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new File(fileName));

        List<?> list = saxValidator.getResult(doc,"//*[@*]");
		LOGGER.debug("ModuleTest list() >>> " + list);
		cxa.viewEelement(list);

        // 1. 캐쉬에 저장
        cxa.sendCacheServer(list);
   }
}
