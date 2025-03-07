package org.egovframe.rte.fdl.xml.ehcache;

import net.sf.ehcache.Element;
import org.egovframe.rte.fdl.xml.SharedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class CacheServer extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheServer.class);
    private final static int PORT = 64208;
    private final static int MINUTES = 1;

    ServerSocket ss = null;
    Socket s = null;
    ObjectInputStream ooi = null;
    ObjectOutputStream ooo = null;
    CacheImpl cachemgrImpl = null;

    public CacheServer() {
        setCacheConfig();
        try {
            ss = new ServerSocket(PORT);
        } catch (IOException e) {
			LOGGER.debug("##### CacheServer CacheServer() Exeption >>> {}", e.getMessage());
        }
    }

	public void setCacheConfig() {
		String fileName = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("ehcache.xml")).getFile();
		cachemgrImpl = new CacheImpl();
		cachemgrImpl.getCacheManager(fileName);
		cachemgrImpl.getCache("regCache");
	}

    public static void main(String[] args) {
        new CacheServer().start();
    }

    public void run() {
        while (true) {
			String tmp_value = null;
			String tmp_key = null;
			LOGGER.debug("연결대기");
			try {
				Socket s = ss.accept();
				ooi = new ObjectInputStream(s.getInputStream());
				ooo = new ObjectOutputStream(s.getOutputStream());
				SharedObject sob = (SharedObject) ooi.readObject();

				if (sob.getKey().equals("*")) {
					tmp_value = (String) sob.getValue();
					Element element = (Element) cachemgrImpl.retrieveCache(tmp_value);
					SharedObject ret_sObject = (SharedObject) element.getObjectValue();
					ooo.writeObject(ret_sObject);
				} else {
					tmp_key = sob.getKey();
					cachemgrImpl.storeCache(tmp_key, sob);
					SharedObject sObject = new SharedObject("ret_msg", "성공적으로 캐슁되어습니다.");
					ooo.writeObject(sObject);
					LOGGER.debug("성공적으로 캐쉬되었습니다.");
				}
			} catch (ClassNotFoundException | IOException e) {
				LOGGER.debug("##### CacheServer run() Exeption >>> {}", e.getMessage());
            }
        }
    }

}
