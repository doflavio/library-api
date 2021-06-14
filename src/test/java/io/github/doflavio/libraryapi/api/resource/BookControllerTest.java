package io.github.doflavio.libraryapi.api.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.doflavio.libraryapi.api.dto.BookDTO;
import io.github.doflavio.libraryapi.exception.BusinessException;
import io.github.doflavio.libraryapi.model.entity.Book;
import io.github.doflavio.libraryapi.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.print.attribute.standard.Media;
import java.util.Arrays;
import java.util.Optional;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso.")
    public void createBookTest() throws Exception {
        BookDTO dto = createNewBookDto();

        Book savedBook = Book.builder().id(10l).author("Artur").title("As aventuras").isbn("001").build();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        // Verificação
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(10l))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(dto.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(dto.getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(dto.getIsbn()))

        ;
    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para a criação um livro.")
    public void createInvalidBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect( MockMvcResultMatchers.status().isBadRequest())
                .andExpect( MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar um livro com isbn já utilizado por outro.")
    public void createBookWithDuplicateIsbn() throws Exception {

        BookDTO dto = createNewBookDto();
        String json = new ObjectMapper().writeValueAsString(dto);

        String mensagemErro = "isbn já cadastrado.";
        BDDMockito.given( service.save(Mockito.any( Book.class)))
                    .willThrow(new BusinessException(mensagemErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        // Verificação
        mvc.perform(request)
                .andExpect( MockMvcResultMatchers.status().isBadRequest())
                .andExpect( MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect( MockMvcResultMatchers.jsonPath("errors[0]").value(mensagemErro))
        ;

    }

    @Test@DisplayName("Deve obter infomações de um livro.")
    public void getBookDetailsTest() throws Exception{

        //Cenário(given)
        Long id = 1l;

        Book book = Book.builder().id(id)
                        .author("Artur")
                        .title("As aventuras")
                        .isbn("001")
                        .build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        //Execução(when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // Verificação
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1l))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(createNewBookDto().getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(createNewBookDto().getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(createNewBookDto().getIsbn()));


    }

    @Test@DisplayName("Deve retornar resource not found quando o livro procurado não existir")
    public void bookNotFoundTest() throws Exception{

        //Cenário(given)
        Long id = 1l;
        BDDMockito.given(service.getById( Mockito.anyLong()) ).willReturn(Optional.empty());

        //Execução(when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // Verificação
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception{

        //Cenário(given)
        Long id = 1l;
        BDDMockito.given(service.getById( Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(id).build()));

        //Execução(when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // Verificação
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar")
    public void deleteBookInexistenteTest() throws Exception{

        //Cenário(given)
        Long id = 1l;
        BDDMockito.given(service.getById( Mockito.anyLong())).willReturn(Optional.empty());

        //Execução(when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // Verificação
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception{
        //Cenário(given)
        Long id = 1l;
        String isbn = "321";
        String json = new ObjectMapper().writeValueAsString(createNewBookDto());

        //Atualizando (exemplo objeto como se estivesse na base de dados)
        Book updatingBook = Book.builder()
                .id(id)
                .title("some title")
                .author("some author")
                .isbn(isbn)
                .build();
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));

        //Atualizado
        Book updatedBook = Book.builder()
                .id(id)
                .author("Artur")
                .title("As aventuras")
                .isbn(isbn)
                .build();

        BDDMockito.given( service.update(updatingBook) ).willReturn(updatedBook);

        //Execução(when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        // Verificação
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1l))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(createNewBookDto().getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(createNewBookDto().getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(isbn));
    }

    @Test
    @DisplayName("Deve retornar resource 404 ao tentar atualizar um livro inexistente")
    public void updateBookInexistenteTest() throws Exception{
        //Cenário(given)
        Long id = 1l;
        String json = new ObjectMapper().writeValueAsString(createNewBookDto());
        BDDMockito.given(service.getById( Mockito.anyLong())).willReturn(Optional.empty());

        //Execução(when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        // Verificação
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Deve Filtar livros")
    public void findBookTest() throws Exception{

        //Cenário
        Long id = 1l;
        Book book = Book.builder()
                .id(id)
                .title(createNewBookDto().getTitle())
                .author(createNewBookDto().getAuthor())
                .isbn(createNewBookDto().getIsbn())
                .build();

        BDDMockito.given( service.find(Mockito.any(Book.class),Mockito.any(Pageable.class)) )
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0,100),1));

        // Execução ("api/books?")
        String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(),book.getAuthor());


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk() )
                .andExpect( MockMvcResultMatchers.jsonPath("content" , Matchers.hasSize(1)))
                .andExpect( MockMvcResultMatchers.jsonPath("totalElements" ).value(1))
                .andExpect( MockMvcResultMatchers.jsonPath("pageable.pageSize" ).value(100))
                .andExpect( MockMvcResultMatchers.jsonPath("pageable.pageNumber" ).value(0));

    }

    private BookDTO createNewBookDto() {
        return BookDTO.builder().author("Artur").title("As aventuras").isbn("001").build();
    }

}
