package io.github.doflavio.libraryapi.service.impl;

import io.github.doflavio.libraryapi.model.entity.Book;
import io.github.doflavio.libraryapi.model.repository.BookRepository;
import io.github.doflavio.libraryapi.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        return repository.save(book);
    }
}
