package io.github.doflavio.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.doflavio.libraryapi.api.dto.LoanDTO;
import io.github.doflavio.libraryapi.model.entity.Book;
import io.github.doflavio.libraryapi.model.entity.Loan;
import io.github.doflavio.libraryapi.service.BookService;
import io.github.doflavio.libraryapi.service.LoanService;
import io.github.doflavio.libraryapi.service.impl.BookServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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

        LoanDTO emprestimoDTO = LoanDTO.builder().isbn(isbn).customer("Fulano").build();
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
}
