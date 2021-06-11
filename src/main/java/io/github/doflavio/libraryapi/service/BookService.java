package io.github.doflavio.libraryapi.service;

import io.github.doflavio.libraryapi.model.entity.Book;

import java.util.Optional;

public interface BookService {
    Book save(Book any);

    Optional<Book> getById(Long id);
}
