package org.egovframe.rte.fdl.cmmn.config;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

@Configuration
@EnableCaching
public class EhcacheConfig {

    @Bean
    public javax.cache.CacheManager ehCacheManager() {
        CachingProvider provider = Caching.getCachingProvider(EhcacheCachingProvider.class.getName());
        javax.cache.configuration.Configuration<Object, Object> config =
                Eh107Configuration.fromEhcacheCacheConfiguration(
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                        Object.class, Object.class,
                                        ResourcePoolsBuilder.heap(100))
                                .build());

        CacheManager cacheManager = provider.getCacheManager();
        cacheManager.createCache("ehcache", config);
        return cacheManager;
    }

    @Bean
    public org.springframework.cache.CacheManager springCacheManager(javax.cache.CacheManager cm) {
        return new JCacheCacheManager(cm);
    }

}
