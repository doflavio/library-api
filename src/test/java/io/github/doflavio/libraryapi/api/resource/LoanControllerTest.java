package io.github.doflavio.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.doflavio.libraryapi.api.dto.LoanDTO;
import io.github.doflavio.libraryapi.api.dto.LoanFilterDTO;
import io.github.doflavio.libraryapi.api.dto.ReturnedLoanDTO;
import io.github.doflavio.libraryapi.exception.BusinessException;
import io.github.doflavio.libraryapi.model.entity.Book;
import io.github.doflavio.libraryapi.model.entity.Loan;
import io.github.doflavio.libraryapi.service.BookService;
import io.github.doflavio.libraryapi.service.LoanService;
import io.github.doflavio.libraryapi.service.impl.BookServiceImpl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.quality.MockitoHint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Deve realizar um emprestimo")
    public void createLoanTest() throws Exception{

        //Cenário
        Long id = 1l;
        String isbn = "123";

        LoanDTO emprestimoDTO = LoanDTO.builder().isbn(isbn).email("customer@email.com").customer("Fulano").build();
        String loanJson = new ObjectMapper().writeValueAsString(emprestimoDTO);

        Book book = Book.builder().id(id).isbn(isbn).build();
        BDDMockito.given( bookService.getBookByIsbn("123") ).willReturn(Optional.of(book));

        Loan loan = Loan.builder()
                .id(1l)
                .customer("Fulano")
                .book(book)
                .loanDate(LocalDate.now())
                .build();

        BDDMockito.given( loanService.save( Mockito.any(Loan.class)) ).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                                        .post(LOAN_API)
                                                        .accept(MediaType.APPLICATION_JSON)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(loanJson);

        //Execução/verificação
        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated() )
                .andExpect( MockMvcResultMatchers.content().string("1" ));

        //Verificação
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro indexistente.")
    public void invalidInsbnCreateLoanTest() throws Exception{
        //Cenário
        String isbn = "123";
        LoanDTO emprestimoDTO = LoanDTO.builder().isbn(isbn).customer("Fulano").build();
        String loanJson = new ObjectMapper().writeValueAsString(emprestimoDTO);

        Book book = Book.builder().id(1l).isbn(isbn).build();
        BDDMockito.given( bookService.getBookByIsbn("123") ).willReturn(Optional.of(book));

        BDDMockito.given( loanService.save( Mockito.any(Loan.class)) )
                .willThrow(new BusinessException("Book already loaned"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                                    .post(LOAN_API)
                                                    .accept(MediaType.APPLICATION_JSON)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(loanJson);

        //Execução/verificação
        mvc.perform(request)
                .andExpect( MockMvcResultMatchers.status().isBadRequest())
                .andExpect( MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect( MockMvcResultMatchers.jsonPath("errors[0]")
                                .value("Book already loaned"));


    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro emprestado.")
    public void loanedBookErrorOnCreateLoanTest() throws Exception{
        //Cenário
        String isbn = "123";
        LoanDTO emprestimoDTO = LoanDTO.builder().isbn(isbn).customer("Fulano").build();
        String loanJson = new ObjectMapper().writeValueAsString(emprestimoDTO);

        BDDMockito.given( bookService.getBookByIsbn("123") ).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loanJson);

        //Execução/verificação
        mvc.perform(request)
                .andExpect( MockMvcResultMatchers.status().isBadRequest())
                .andExpect( MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect( MockMvcResultMatchers.jsonPath("errors[0]")
                        .value("Book not found for passed isbn"));


    }

    @Test
    @DisplayName("Deve retornar um livro")
    public void returnBookTest() throws Exception{
        //Cenário { returned:true }
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();

        Loan loan = Loan.builder().id(1l).build();

        BDDMockito.given(  loanService.getById(Mockito.anyLong()) )
                .willReturn( Optional.of(loan) );

        String json = new ObjectMapper().writeValueAsString(dto);

       MockHttpServletRequestBuilder request = MockMvcRequestBuilders
               .patch(LOAN_API.concat("/1"))
               .accept(MediaType.APPLICATION_JSON)
               .contentType(MediaType.APPLICATION_JSON)
               .content(json);

        //Execução/verificação
       mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

       Mockito.verify( loanService, Mockito.times(1) ).update(loan);
    }

    @Test
    @DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente")
    public void returnInexistentBookTest() throws Exception{
        //Cenário { returned:true }
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(  loanService.getById(Mockito.anyLong()) ).willReturn( Optional.empty() );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //Execução/verificação
        mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Deve Filtar empréstimos")
    public void findLoansTest() throws Exception{
        //Cenário
        Long id = 1l;

        Loan loan = createLoan();
        loan.setId(id);
        Book bookInLoan = Book.builder().id(1l).isbn("321").build();
        loan.setBook(bookInLoan);

        BDDMockito.given( loanService.find(Mockito.any(LoanFilterDTO.class),Mockito.any(Pageable.class)) )
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0,10),1));

        // Execução ("api/books?")
        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
                bookInLoan.getIsbn(),loan.getCustomer());


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk() )
                .andExpect( MockMvcResultMatchers.jsonPath("content" , Matchers.hasSize(1)))
                .andExpect( MockMvcResultMatchers.jsonPath("totalElements" ).value(1))
                .andExpect( MockMvcResultMatchers.jsonPath("pageable.pageSize" ).value(10))
                .andExpect( MockMvcResultMatchers.jsonPath("pageable.pageNumber" ).value(0));

    }

    public Loan createLoan(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";
        return Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
    }

}
