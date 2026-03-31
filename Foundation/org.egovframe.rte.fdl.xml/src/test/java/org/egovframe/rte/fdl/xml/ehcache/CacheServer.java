package org.egovframe.rte.fdl.xml.ehcache;

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

    ServerSocket ss = null;
    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;
    CacheImpl cacheImpl = null;

    public CacheServer() {
        setCacheConfig();
        try {
            ss = new ServerSocket(PORT);
        } catch (IOException e) {
            LOGGER.debug("[{}] CacheServer CacheServer() : {}", e.getClass().getName(), e.getMessage());
        }
    }

    public static void main(String[] args) {
        new CacheServer().start();
    }

    public void setCacheConfig() {
        String fileName = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("ehcache.xml")).getFile();
        cacheImpl = new CacheImpl();
        cacheImpl.getCacheManager(fileName);
        cacheImpl.getCache("regCache");
    }

    public void run() {
        while (true) {
            LOGGER.debug("### CacheServer Waiting for connection");
            try {
                Socket s = ss.accept();
                ois = new ObjectInputStream(s.getInputStream());
                oos = new ObjectOutputStream(s.getOutputStream());
                SharedObject sob = (SharedObject) ois.readObject();
                if (sob.getKey().equals("*")) {
                    oos.writeObject(sob.getValue());
                } else {
                    cacheImpl.storeCache(sob.getKey(), sob);
                    SharedObject sObject = new SharedObject("ret_msg", "SUCCESS");
                    oos.writeObject(sObject);
                    LOGGER.debug("### CacheServer run() success... ");
                }
            } catch (ClassNotFoundException | IOException e) {
                LOGGER.debug("[{}] CacheServer run() : {}", e.getClass().getName(), e.getMessage());
            }
        }
    }

}
