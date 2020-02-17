package egovframework.rte.fdl.xml.ehcache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import egovframework.rte.fdl.xml.SharedObject;

import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheServer extends Thread {

	/** The hang-around time */
	   final static int MINUTES = 1;
	   /** port number */
	   public final static int PORT = 64208;
	   /** The server socket. */
	   ServerSocket ss = null;
	   private static final Logger LOGGER  = LoggerFactory.getLogger(CacheServer.class);
	   Socket s = null;

	   ObjectInputStream ooi = null;
	   ObjectOutputStream ooo = null;

	   CacheImpl cachemgrImpl = null;

	   public CacheServer() throws IOException
	   {
		  setCacheConfig();
	      ss = new ServerSocket(PORT);
	   }

	   public void setCacheConfig()
	   {
		   String fileName = "c:\\temp\\ehcache.xml";//Thread.currentThread().getContextClassLoader().getResource("ehcache.xml").getFile();
		   LOGGER.debug(fileName);
			cachemgrImpl = null;
			   try {
			    cachemgrImpl = new CacheImpl();
			    cachemgrImpl.getCacheManager(fileName);
			    cachemgrImpl.getCache("regCache");
			  } catch (Exception e) {e.printStackTrace();}
	   }

	   public void run() {
	      while (true) {
	         try {
	         String tmp_value = null;
	         String tmp_key = null;
	         LOGGER.debug("연결대기");
	         Socket s = ss.accept();
	          ooi = new ObjectInputStream(s.getInputStream());
	          ooo = new ObjectOutputStream(s.getOutputStream());
	          SharedObject sob = (SharedObject)ooi.readObject();

	         if(sob.getKey().equals("*"))
	         {
	        	 tmp_value = (String)sob.getValue();
	        	 Element element = (Element) cachemgrImpl.retrieveCache(tmp_value); // GET
	        	 SharedObject ret_sObject = (SharedObject)element.getObjectValue();
	        	 //SharedObject sObject = (SharedObject)cacheObj.getObjectValue();
	        	 ooo.writeObject(ret_sObject);
	         }else
	         {
	        	 tmp_key = (String)sob.getKey();
	        	 cachemgrImpl.storeCache(tmp_key, sob);
	        	 SharedObject sObject = new SharedObject("ret_msg","성공적으로 캐슁되어습니다.");
	        	 ooo.writeObject(sObject);
	        	 LOGGER.debug("성공적으로 캐쉬되었습니다.");
	         }

	          } catch (IOException e) {
	        	  LOGGER.debug("에러 : {}", e);
	         }
	         catch(ClassNotFoundException e1)
	         {
	        	 e1.printStackTrace();
	         }finally {
	  		   try
	  		   {
	  			// s.close();
	  			   ooi.close();
	  			   ooo.close();

	  		   } catch(Exception t) {t.printStackTrace();}
			  }
	      }
	   }

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		new CacheServer().start();
	}

}
