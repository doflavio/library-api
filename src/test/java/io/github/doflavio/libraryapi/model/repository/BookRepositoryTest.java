package io.github.doflavio.libraryapi.model.repository;

import io.github.doflavio.libraryapi.model.entity.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.constraints.AssertTrue;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    public void returnTrueWhenIsbnExists(){
        //Cenário
        String isbn = "123";

        Book book = createNewBook(isbn);
        entityManager.persist(book);

        // Execução
        boolean  exists = repository.existsByIsbn(isbn);

        // Verificação
        Assertions.assertThat(exists).isTrue();
    }



    @Test
    @DisplayName("Deve retornar false quando não existir um livro na base com o isbn informado")
    public void returnFalseWhenIsbnDoesnExists(){
        //Cenário
        String isbn = "123";

        // Execução
        boolean  exists = repository.existsByIsbn(isbn);

        // Verificação
        Assertions.assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void findByTest(){
        //Cenário
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        //Execução
        Optional<Book> foundBook = repository.findById(book.getId());

        //Verificação
        Assertions.assertThat(foundBook.isPresent()).isTrue();

    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        //Cenário
        Book book = createNewBook("123");

        //Execução
        Book savedBook = repository.save(book);

        //verificação
        Assertions.assertThat( savedBook.getId() ).isNotNull();
    }


    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest(){
        //Cenário
        Book book = createNewBook("123");
        Book savedBook = entityManager.persist(book);
        Book foundBook = entityManager.find( Book.class, book.getId() );

        //Execução
        repository.delete(foundBook);

        //Vefificação
        Book deletedBook = entityManager.find( Book.class, book.getId() );
        Assertions.assertThat( deletedBook ).isNull();

    }

    private Book createNewBook(String isbn) {
        return Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
    }
}
