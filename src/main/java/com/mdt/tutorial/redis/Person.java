package com.mdt.tutorial.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Person {
    private String name;
    private Integer age;
    private String company;

    public Map<String, String>convert() {
        Map<String, String> map = new HashMap<>();
        map.put("name", getName());
        map.put("age", Integer.toString(getAge()));
        map.put("company", getCompany());
        return map;
    }

    public static Person convertToPerson(Map<String, String> personKeyVals) {
        Map<String, String> personKeys = new HashMap<>();
        return Person.builder()
                .age(Integer.parseInt(personKeyVals.get("age")))
                .name(personKeyVals.get("name"))
                .company(personKeyVals.get("company"))
                .build();
    }
}
