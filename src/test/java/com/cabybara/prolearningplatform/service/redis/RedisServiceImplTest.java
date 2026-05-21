package com.cabybara.prolearningplatform.service.redis;

import com.cabybara.prolearningplatform.service.redis.impl.RedisServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @Test
    void setWithTimeoutDelegatesToValueOperations() {
        RedisServiceImpl service = new RedisServiceImpl(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.set("key", "value", 30);

        verify(valueOperations).set("key", "value", 30, TimeUnit.SECONDS);
    }

    @Test
    void deleteCollectionSkipsNullOrEmptyKeys() {
        RedisServiceImpl service = new RedisServiceImpl(redisTemplate);

        service.delete((List<String>) null);
        service.delete(List.of());

        verify(redisTemplate, never()).delete(org.mockito.ArgumentMatchers.any(Collection.class));
    }

    @Test
    void deleteCollectionDelegatesWhenKeysPresent() {
        RedisServiceImpl service = new RedisServiceImpl(redisTemplate);

        service.delete(List.of("a", "b"));

        verify(redisTemplate).delete(List.of("a", "b"));
    }

    @Test
    void expireHasKeyAndGetTtlDelegateToRedisTemplate() {
        RedisServiceImpl service = new RedisServiceImpl(redisTemplate);
        when(redisTemplate.hasKey("key")).thenReturn(true);
        when(redisTemplate.expire("key", 20, TimeUnit.SECONDS)).thenReturn(true);
        when(redisTemplate.getExpire("key", TimeUnit.SECONDS)).thenReturn(20L);

        assertTrue(service.hasKey("key"));
        assertTrue(service.expire("key", 20));
        assertEquals(20L, service.getTTL("key"));
    }

    @Test
    void sMembersReturnsEmptySetWhenRedisReturnsNullOrEmpty() {
        RedisServiceImpl service = new RedisServiceImpl(redisTemplate);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("key")).thenReturn(null).thenReturn(Set.of());

        assertTrue(service.sMembers("key").isEmpty());
        assertTrue(service.sMembers("key").isEmpty());
    }

    @Test
    void sMembersConvertsMembersToStringSet() {
        RedisServiceImpl service = new RedisServiceImpl(redisTemplate);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("key")).thenReturn(Set.of(1L, "two"));

        Set<String> members = service.sMembers("key");

        assertEquals(Set.of("1", "two"), members);
    }

    @Test
    void setGetDeleteAndSetOperationsDelegate() {
        RedisServiceImpl service = new RedisServiceImpl(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(valueOperations.get("key")).thenReturn("value");

        service.set("key", "value");
        assertEquals("value", service.get("key"));
        service.delete("key");
        service.sAdd("set", "member");
        service.sRemove("set", "member");

        verify(valueOperations).set("key", "value");
        verify(valueOperations).get("key");
        verify(redisTemplate).delete("key");
        verify(setOperations).add("set", "member");
        verify(setOperations).remove("set", "member");
    }
}
