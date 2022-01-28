package com.kloia.debezium.controller;

import com.kloia.debezium.model.Author;
import com.kloia.debezium.service.AuthorService;
import com.kloia.debezium.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping(value = AuthorController.ENDPOINT)
@RestController
@RequiredArgsConstructor
public class AuthorController {

    public static final String ENDPOINT = "authors";

    private final AuthorService authorService;
    private final RedisService redisService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody Author author) {
        authorService.create(author);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Author>> fetchAll() {
        return new ResponseEntity<>(authorService.fetchAll(), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Author> getAuthor(@PathVariable("id") Long id) {
        return new ResponseEntity<>(redisService.getAuthor(id), HttpStatus.OK);
    }

}
