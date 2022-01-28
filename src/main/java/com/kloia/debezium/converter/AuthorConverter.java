package com.kloia.debezium.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.debezium.model.Author;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthorConverter {
    public Author convert(Map<String, String> value) {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(value, Author.class);
    }
}
