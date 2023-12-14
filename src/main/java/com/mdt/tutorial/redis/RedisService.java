package com.mdt.tutorial.redis;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class RedisService {
    private static final Logger logger = Logger.getLogger(RedisService.class.getName());

    public void p(Object o) {
        logger.info(o.toString());
    }
    /**
     * Initial connection and setup of Redis
     */
    @PostConstruct
    public void connect(){
        JedisPool pool = new JedisPool("127.0.0.1", 6379);
        try (Jedis jedis = pool.getResource()) {
            jedis.set("foo", "bar");
            logger.info("getting foo");
            logger.info(jedis.get("foo"));

            Map<String, String> hash = new HashMap<>();;
            hash.put("name", "Connie");
            hash.put("surname", "Zhang");
            hash.put("company", "IGS");
            hash.put("age", "32");
            jedis.hset("user-session:123", hash);

            p(jedis.hgetAll("user-session:123"));
        }
    }
    public String addPerson(Person person) {
        JedisPooled jedis = new JedisPooled("localhost", 6379);
        String returnKey = "user-session:"+ person.getName();
        jedis.hset(returnKey, person.convert());
        return returnKey;
    }
    public Person getPerson(String key) {
        JedisPooled jedis = new JedisPooled("localhost", 6379);
        Map<String, String> personMap = jedis.hgetAll(key);
        return Person.convertToPerson(personMap);
    }
}
