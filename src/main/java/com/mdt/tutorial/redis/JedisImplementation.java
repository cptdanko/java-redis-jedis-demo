package com.mdt.tutorial.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class JedisImplementation implements RedisWrapper {
    private static final Logger LOGGER = Logger.getLogger(JedisImplementation.class.toString());
    boolean isLocalConnection = false;
    final JedisPool pool = new JedisPool("127.0.0.1", 6379);
    private static ObjectMapper mapper = new ObjectMapper();
    private static JedisImplementation instance = new JedisImplementation();

    @Override
    public <T> long addToList(String key, T value) throws MissingResourceException, JsonProcessingException {
        long operationResult = 0L;
        String valStr = mapper.writeValueAsString(value);
        try (Jedis jedis = pool.getResource()) {
            operationResult = jedis.lpush(key, valStr);
        }
        return operationResult;
    }

    @Override
    public long deleteList(String key) throws MissingResourceException {
        long result = 0L;
        try (Jedis jedis = pool.getResource()) {
            result = jedis.del(key);
        }
        return result;
    }

    @Override
    public <T> List<T> getList(String key, Class<T> className) throws JsonProcessingException, MissingResourceException {
        long size = 0L;
        List<String> entries;
        try (Jedis jedis = pool.getResource()) {
            size = jedis.llen(key);
            entries = jedis.lrange(key, 0, size);
        }

        List<T> result = new ArrayList<>();
        entries.stream().forEach(entry -> {
            T classObj = null;
            try {
                classObj = className.cast(mapper.readValue(entry, className));
                result.add(classObj);
            } catch (JsonProcessingException e) {
                LOGGER.info(() -> String.format("Exception in reading values for key [ %s ]", key));
                LOGGER.info(e.getMessage());
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    @Override
    public <T, G> long addToMap(String key, T field, G value) throws JsonProcessingException, MissingResourceException {
        long operationResult = 0L;
        String serialisedKey = mapper.writeValueAsString(field);
        String serialisedValue = mapper.writeValueAsString(value);
        try (Jedis jedis = pool.getResource()) {
            if (jedis.hexists(key, serialisedKey)) {
                long result = jedis.hdel(key, serialisedKey);
                LOGGER.info(String.format("Delete result [ %d ] ", result));
                result = jedis.hset(key, serialisedKey, serialisedValue);
                LOGGER.info(String.format("Add result [ %d ] ", result));
            }
            else {
                LOGGER.info(String.format("Key [ %s ] - does not exist, so inserting", serialisedKey));
                long result = jedis.hset(key, serialisedKey, serialisedValue);
                logRedisOperationResult(key, result);
            }
        }
        return operationResult;
    }

    @Override
    public <T, G> G getValueFromMap(String key, T field, Class<T> fieldClassName, Class<G> valueClassName) throws JsonProcessingException, MissingResourceException {
        G valueAfterCast = null;
        String serialisedFieldStr = mapper.writeValueAsString(field);
        try (Jedis jedis = pool.getResource()) {
            String value = jedis.hget(key, serialisedFieldStr);
            LOGGER.info(String.format("Value after hget [ %s ] ", value));
            valueAfterCast = deserializeStr(value, valueClassName);
            LOGGER.info(String.format("Value after cast [ %s ] ", valueAfterCast.toString()));
        }
        return valueAfterCast;
    }

    private <T> T deserializeStr(String value, Class<T> className) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            T deserializedResult = className.cast(objectMapper.readValue(value, className));
            return deserializedResult;
        } catch (JsonProcessingException e) {
            LOGGER.info("Exception occured " + e.getMessage());
            LOGGER.severe(e.getMessage());
        } catch (ClassCastException cce) {

        }
        return null;
    }

    @Override
    public <T, G> Map<T, G> getCustomMapFromRedis(String key, String keyLbl, Class<T> keyClass, Class<G> valueClass) {
        ConcurrentMap<T, G> concurrentHashMap = new ConcurrentHashMap<>();
        try {
            Map<String, String> serizlisedMap;
            JedisPool pool = new JedisPool("127.0.0.1", 6379);
            try (Jedis jedis = pool.getResource()) {
                serizlisedMap = jedis.hgetAll(key);
            }
            if (serizlisedMap != null) {
                LOGGER.info(() -> String.format("Got map of strings saved in Redis of size [ %d ] for [ %s ]", serizlisedMap.size(), keyLbl));
            }
            for (Map.Entry<String, String> entry : serizlisedMap.entrySet()) {
                T deserialisedKey = deserializeStr(entry.getKey(), keyClass);
                G deserialisedValue = deserializeStr(entry.getValue(), valueClass);
                LOGGER.info("----------------- end of deserialising value ----------------- ");
                concurrentHashMap.put(deserialisedKey, deserialisedValue);
            }
            LOGGER.info(() -> String.format("------------- extracted [%s] from DB ------------- ", keyLbl));
            // MapUtils.verbosePrint(System.out, String.format("Printing Map for [ %s ]", keyLbl), concurrentHashMap);
        } catch (MissingResourceException mre) {
            LOGGER.info(() -> String.format("RedisCache.getCustomMapFromRedis.exception= %s", mre.getMessage()));
            LOGGER.severe(String.format("Exception while retrieving and parsing map from Redis, %s %s", mre.getMessage(), mre.getLocalizedMessage()));
        } catch (Exception e) {
            LOGGER.info(() -> String.format("RedisCache.getComponentDetailsMap.exception= %s", e.getMessage()));
            LOGGER.severe(String.format("Exception, %s %s", e.getMessage(), e.getLocalizedMessage()));
        }
        return concurrentHashMap;
    }

    public void logRedisOperationResult(String key, long result) {
        LOGGER.info(() -> String.format("Result of setting key %s in Redis = %d", key, result));
    }
}

