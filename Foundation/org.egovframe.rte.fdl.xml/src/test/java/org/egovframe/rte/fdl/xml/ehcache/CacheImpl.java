package org.egovframe.rte.fdl.xml.ehcache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;

import java.net.URL;

public class CacheImpl {

    private static CacheManager manager;
    private static Cache<Object, Object> cache;

    public CacheImpl() {
        // Default constructor.
    }

    public CacheManager getCacheManager() {
        URL cacheUrl = getClass().getResource("xml/ehcache.xml");
        Configuration xmlConfig = new XmlConfiguration(cacheUrl);
        manager = CacheManagerBuilder.newCacheManager(xmlConfig);
        return manager;
    }

    public CacheManager getCacheManager(String configFilePath) {
        URL fileUrl = getClass().getResource(configFilePath);
        Configuration xmlConfig = new XmlConfiguration(fileUrl);
        manager = CacheManagerBuilder.newCacheManager(xmlConfig);
        return manager;
    }

    public Cache<Object, Object> getCache(String cacheName) {
        cache = manager.getCache(cacheName, Object.class, Object.class);
        return cache;
    }

    public void storeCache(Object name, Object value) {
        cache.put(name, value);
    }

}
