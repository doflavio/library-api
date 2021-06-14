package io.github.doflavio.libraryapi.service;

import io.github.doflavio.libraryapi.exception.BusinessException;
import io.github.doflavio.libraryapi.model.entity.Book;

import io.github.doflavio.libraryapi.model.repository.BookRepository;
import io.github.doflavio.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl( repository );
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        //Cenário
        Book book = createValidBook();
        Mockito.when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn(false);

        Book bookReturn = Book.builder().id(1l)
                .isbn("123")
                .author("Fulano")
                .title("As aventuras")
                .build();

        Mockito.when( repository.save(book) ).thenReturn(bookReturn);

        //Execução
        Book savedBook = service.save(book);

        //Verificação
        Assertions.assertThat(savedBook.getId()).isNotNull();
        Assertions.assertThat(savedBook.getIsbn()).isEqualTo("123");
        Assertions.assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
        Assertions.assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com isbn duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN() {
        //Cenário
        Book book = createValidBook();
        Mockito.when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn(true);

        //Execução
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        //verificações
        Assertions.assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("isbn já cadastrado.");

        Mockito.verify(repository,Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve obter um livro por Id")
    public void getByIdTest(){
        //Cenário
        Long id = 1l;
        Book book = createValidBook();
        book.setId(id);
        Mockito.when( repository.findById(id) ).thenReturn(Optional.of(book));

        //Execução
        Optional<Book> foundBook = service.getById(id);

        //Verificação
        Assertions.assertThat( foundBook.isPresent() ).isTrue();
        Assertions.assertThat( foundBook.get().getId()).isEqualTo(id);
        Assertions.assertThat( foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        Assertions.assertThat( foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        Assertions.assertThat( foundBook.get().getTitle()).isEqualTo(book.getTitle());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por Id quando ele não existe na base")
    public void getNotFoundByIdTest(){
        //Cenário
        Long id = 1l;
        Mockito.when( repository.findById(id) ).thenReturn(Optional.empty());

        //Execução
        Optional<Book> book = service.getById(id);

        //Verificação
        Assertions.assertThat( book.isPresent() ).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest(){
        //Cenário
        long id = 1l;
        Book book = Book.builder().id(id).build();

        //Execução
        org.junit.jupiter.api.Assertions.assertDoesNotThrow( () -> service.delete(book));

        //Verificação
        Mockito.verify( repository,Mockito.times(1) ).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente.")
    public void deleteInvalidBookTest(){
        //Cenário
        Book book = new Book();

        //Execução/verificação
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        //Verificação
        Mockito.verify(repository, Mockito.never()).delete(book);
    }


    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente.")
    public void updateInvalidBookTest(){
        //Cenário
        Book book = new Book();

        //Execução/verificação
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

        //Verificação
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest(){
        //Cenário
        long id = 1l;

        //Livro a atualizar
        Book updatingBook = Book.builder().id(id).build();

        //Simulação
        Book updatedBook = createValidBook();
        updatedBook.setId(id);

        Mockito.when( repository.save(updatingBook) ).thenReturn(updatedBook);

        //Execução
        Book book = service.update(updatingBook);

        //Verificações
        Assertions.assertThat(book.getId()).isEqualTo(updatedBook.getId());
        Assertions.assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        Assertions.assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        Assertions.assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());

    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest(){
        //Cenário
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Book> lista = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(Arrays.asList(book), pageRequest,1);
        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

        //Execução
        Page<Book> result = service.find(book, pageRequest);

        //Verificação
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent()).isEqualTo(lista);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    private Book createValidBook() {
        return Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
    }

}
