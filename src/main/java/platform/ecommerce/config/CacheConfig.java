package platform.ecommerce.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache configuration with Redis.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCT_CACHE = "products";
    public static final String PRODUCT_DETAIL_CACHE = "productDetails";
    public static final String CATEGORY_CACHE = "categories";
    public static final String CATEGORY_TREE_CACHE = "categoryTree";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Product cache - 30 minutes (frequently accessed, moderate changes)
        cacheConfigurations.put(PRODUCT_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Product detail cache - 15 minutes (more detailed data, fresher needed)
        cacheConfigurations.put(PRODUCT_DETAIL_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Category cache - 1 hour (rarely changes)
        cacheConfigurations.put(CATEGORY_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));

        // Category tree cache - 2 hours (hierarchical data, rarely changes)
        cacheConfigurations.put(CATEGORY_TREE_CACHE, defaultConfig.entryTtl(Duration.ofHours(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
