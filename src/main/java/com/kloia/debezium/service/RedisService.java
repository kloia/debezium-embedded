package com.kloia.debezium.service;

import com.kloia.debezium.converter.AuthorConverter;
import com.kloia.debezium.model.Author;
import io.debezium.data.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    @Value("${redis.uri}")
    private String redisURI;

    private JedisPool jedisPool;

    private final AuthorConverter authorConverter;

    private void checkAndCreatePool() throws URISyntaxException {
        if (jedisPool == null) {
            URI uri = new URI(redisURI);
            jedisPool = new JedisPool(new GenericObjectPoolConfig<>(), uri);
            log.info("Redis Connection Pool Created : {}", redisURI);
        }
    }

    public void replicateData(Map<String, String> payload, Envelope.Operation operation) {
        try {
            checkAndCreatePool();
        } catch (URISyntaxException e) {
            log.error("URI is not valid");
        }
        String key = payload.get("id");
        try (Jedis jedis = jedisPool.getResource()) {

            if (operation == Envelope.Operation.DELETE) {
                jedis.del(key);
            } else {
                jedis.hmset(key, payload);
            }

        }
    }

    public Author getAuthor(Long id) {
        try {
            checkAndCreatePool();
        } catch (URISyntaxException e) {
            log.error("URI is not valid");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> author = jedis.hgetAll(String.valueOf(id));
            return authorConverter.convert(author);
        }
    }
}
