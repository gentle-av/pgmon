package avr.cache;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class TTLConfig implements CachingConfigurer {

    @Bean
    @Primary
    public org.springframework.cache.CacheManager customCacheManager() {
        var builder = com.github.benmanes.caffeine.cache.Caffeine.newBuilder();
        var cacheManager = new org.springframework.cache.caffeine.CaffeineCacheManager();
        cacheManager.setCaffeine(builder);
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }

    @Bean("databaseStatsCacheManager")
    public org.springframework.cache.CacheManager databaseStatsCacheManager() {
        var caffeine = com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(500);
        var cacheManager = new org.springframework.cache.caffeine.CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);

        return cacheManager;
    }

    @Bean("longTermCacheManager")
    public org.springframework.cache.CacheManager longTermCacheManager() {
        var caffeine = com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(100);

        var cacheManager = new org.springframework.cache.caffeine.CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }
}
