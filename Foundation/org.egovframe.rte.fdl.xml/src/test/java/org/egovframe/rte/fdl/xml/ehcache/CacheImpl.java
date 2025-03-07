package org.egovframe.rte.fdl.xml.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URL;

public class CacheImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheImpl.class);
    private static CacheManager manager;
    private static Cache cache;

    public CacheImpl() {
        // Default constructor.
    }

    public CacheManager getCacheManager() {
        try {
            URL url = getClass().getResource("xml/ehcache.xml");
            manager = CacheManager.create(url);
			LOGGER.debug("##### CacheImpl etCacheManager() Cache Name : {}", manager.getName());
        } catch (CacheException e) {
			LOGGER.debug("##### CacheImpl getCacheManager() Exception >>> {}", e.getMessage());
        }
        return manager;
    }

    public CacheManager getCacheManager(String configFilePath) {
        try {
            manager = CacheManager.create(configFilePath);
            LOGGER.debug("##### CacheImpl etCacheManager(String configFilePath) Cache Name : {}", manager.getName());
        } catch (CacheException e) {
			LOGGER.debug("##### CacheImpl etCacheManager(String configFilePath) Exception >>> {}", e.getMessage());
        }
        return manager;
    }

    public Cache getCache(String cacheName) {
        cache = manager.getCache(cacheName);
        return cache;
    }

    public void storeCache(String name, Serializable value) {
        Element element = new Element(name, value);
        cache.put(element);
    }

    public Serializable retrieveCache(String name) {
        return cache.get(name);
    }

}
