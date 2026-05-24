package com.cabybara.prolearningplatform.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {
    @Value("${spring.data.redis.ttl-ms}")
    private Long ttlMs;

    @Value("${spring.data.redis.cache.users-ttl-ms}")
    private Long usersTtlMs;

    @Value("${spring.data.redis.cache.set-detail-ttl-ms}")
    private Long setDetailTtlMs;

    @Value("${spring.data.redis.cache.flashcard-detail-ttl-ms}")
    private Long flashcardDetailTtlMs;

    @Value("${spring.data.redis.cache.exam-detail-ttl-ms}")
    private Long examDetailTtlMs;

    @Value("${spring.data.redis.cache.note-detail-ttl-ms}")
    private Long noteDetailTtlMs;

    @Value("${spring.data.redis.cache.exam-questions-ttl-ms}")
    private Long examQuestionsTtlMs;

    @Value("${spring.data.redis.cache.question-ttl-ms}")
    private Long questionTtlMs;

    @Value("${spring.data.redis.cache.roadmap-detail-ttl-ms}")
    private Long roadmapDetailTtlMs;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer jsonSerializer = buildCacheSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(ttlMs))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("users", defaultConfig.entryTtl(Duration.ofMillis(usersTtlMs)));
        cacheConfigs.put("set_detail", defaultConfig.entryTtl(Duration.ofMillis(setDetailTtlMs)));
        cacheConfigs.put("flashcard_detail", defaultConfig.entryTtl(Duration.ofMillis(flashcardDetailTtlMs)));
        cacheConfigs.put("exam_detail", defaultConfig.entryTtl(Duration.ofMillis(examDetailTtlMs)));
        cacheConfigs.put("note_detail", defaultConfig.entryTtl(Duration.ofMillis(noteDetailTtlMs)));
        cacheConfigs.put("exam_questions", defaultConfig.entryTtl(Duration.ofMillis(examQuestionsTtlMs)));
        cacheConfigs.put("question", defaultConfig.entryTtl(Duration.ofMillis(questionTtlMs)));
        cacheConfigs.put("roadmap_detail", defaultConfig.entryTtl(Duration.ofMillis(roadmapDetailTtlMs)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer jsonSerializer = buildCacheSerializer();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        return template;
    }

    // Builds a dedicated ObjectMapper for Redis that:
    // 1. Handles Java 8 date/time types (OffsetDateTime, etc.)
    // 2. Embeds @class type info so GenericJackson2JsonRedisSerializer can deserialize
    //    back to the correct DTO type (including Java records, which are final classes).
    //    NON_FINAL would skip records since they are implicitly final — EVERYTHING is required.
    @SuppressWarnings("deprecation")
    private GenericJackson2JsonRedisSerializer buildCacheSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.cabybara.prolearningplatform")
                        .allowIfSubType("java.")
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}
