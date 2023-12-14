package com.mdt.tutorial.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/redis")
public class RedisController {
    @Autowired
    private RedisService redisService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return new ResponseEntity<>("ping", HttpStatus.OK);
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addKey(@RequestBody Person toAdd) {
        // add a key to redis database
        String returnKey = redisService.addPerson(toAdd);
        return new ResponseEntity<>(returnKey, HttpStatus.OK);
    }
    @GetMapping(value = "/get/{key}")
    public ResponseEntity<Person> getPerson(@PathVariable("key") String key) {
        return new ResponseEntity<>(redisService.getPerson(key), HttpStatus.OK);
    }
    @GetMapping("/all")
    public ResponseEntity<List<String>> getEntries() {
        List<String> entries = new ArrayList<>(){{
            add("Sam");
            add("Kirk");
        }};
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }
}
