package com.kloia.debezium.service;

import com.kloia.debezium.model.Author;
import com.kloia.debezium.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static io.debezium.data.Envelope.Operation;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final RedisService redisService;

    public void create(Author author) {
        try {
            authorRepository.save(author);
        } catch (Exception e) {
            log.info("An error occurred while saving: Object: " + author + "Exception: " + e);
        }
    }

    public List<Author> fetchAll() {
        return (List<Author>) authorRepository.findAll();
    }

    public void replicateData(Map<String, String> payload, Operation operation) {
       redisService.replicateData(payload, operation);
    }

}
