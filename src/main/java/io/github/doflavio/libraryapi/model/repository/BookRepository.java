package io.github.doflavio.libraryapi.model.repository;

import io.github.doflavio.libraryapi.model.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book,Long> {

}
