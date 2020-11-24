package org.egovframe.rte.fdl.xml.ehcache;

import java.io.Serializable;
import java.net.URL;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheImpl {
	private static CacheManager manager;
	private static Cache cache;
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheImpl.class);

	public CacheImpl() {
		// Default constructor.
	}

	public CacheManager getCacheManager() {
		try {
			URL url = getClass().getResource("xml/ehcache.xml");
			LOGGER.debug(url.toString());
			manager = CacheManager.create(url);

		} catch (CacheException e) {
			e.printStackTrace();
		}
		return manager;
	}

	public CacheManager getCacheManager(String configFilePath) {
		try {
			manager = CacheManager.create(configFilePath);
			LOGGER.debug("Cache Name : {}", manager.getName());
		} catch (CacheException e) {
			e.printStackTrace();
		}
		return manager;
	}

	/**
	 * Get cache using key stored name.
	 * @param cacheName
	 * @return
	 */
	public Cache getCache(String cacheName) {
		cache = (Cache) manager.getCache(cacheName);
		return cache;
	}

	/**
	 *
	 * @param name
	 * @param value
	 */
	public void storeCache(String name, Serializable value) throws CacheException {
		Element element = new Element(name, value);
		cache.put(element);
	}

	public Serializable retrieveCache(String name) throws CacheException {
		return cache.get(name);
	}
}