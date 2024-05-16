package com.mdt.tutorial.redis;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

interface RedisWrapper {
    <T> long addToList(String key, T value) throws MissingResourceException, JsonProcessingException;

    long deleteList(String key) throws MissingResourceException;

    <T> List<T> getList(String key, Class<T> className) throws MissingResourceException, JsonProcessingException;

    <T, G> long addToMap(String key, T field, G value) throws MissingResourceException, JsonProcessingException;

    <T, G> G getValueFromMap(String key, T field, Class<T> keyClassName, Class<G> valueClassName) throws MissingResourceException, JsonProcessingException;

    <T, G> Map<T, G> getCustomMapFromRedis(String key, String keyLbl, Class<T> keyClass, Class<G> valueClass);

    void logRedisOperationResult(String key, long result);
}