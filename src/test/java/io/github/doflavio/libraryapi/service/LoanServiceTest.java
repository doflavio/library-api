package io.github.doflavio.libraryapi.service;

import io.github.doflavio.libraryapi.api.dto.LoanDTO;
import io.github.doflavio.libraryapi.api.dto.LoanFilterDTO;
import io.github.doflavio.libraryapi.exception.BusinessException;
import io.github.doflavio.libraryapi.model.entity.Book;
import io.github.doflavio.libraryapi.model.entity.Loan;
import io.github.doflavio.libraryapi.model.repository.LoanRepository;
import io.github.doflavio.libraryapi.service.impl.LoanServiceImpl;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.tomcat.jni.Local;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {


    LoanService loanService;

    @MockBean
    LoanRepository loanRepository;

    @BeforeEach
    public void setUp(){
        this.loanService = new LoanServiceImpl(loanRepository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo")
    public  void saveLoanTest(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";
        Loan saving = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1l)
                .loanDate(LocalDate.now())
                .customer(customer)
                .book(book)
                .build();

        Mockito.when( loanRepository.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when( loanRepository.save(saving) ).thenReturn(savedLoan);

        Loan loan = loanService.save(saving);

        Assertions.assertThat( loan.getId() ).isEqualTo(savedLoan.getId());
        Assertions.assertThat( loan.getBook().getId() ).isEqualTo(savedLoan.getBook().getId());
        Assertions.assertThat( loan.getCustomer() ).isEqualTo(savedLoan.getCustomer());
        Assertions.assertThat( loan.getLoanDate() ).isEqualTo(savedLoan.getLoanDate());

    }

    @Test
    @DisplayName("Deve lanlar erro de negócio ao salvar um empréstimo com livro emprestado")
    public  void loanedBookSaveTest(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Mockito.when( loanRepository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable exception = Assertions.catchThrowable( () -> loanService.save(savingLoan));

        Assertions.assertThat( exception )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        Mockito.verify( loanRepository, Mockito.never() ).save(savingLoan);
    }

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo ID")
    public void getLoanDetaisTest(){
        //Cenário
        Long id = 1l;
        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when( loanRepository.findById(id) ).thenReturn(Optional.of(loan));

        //Execução
        Optional<Loan> result = loanService.getById(id);

        //Verificação
        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get().getId()).isEqualTo(id);
        Assertions.assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        Assertions.assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        Assertions.assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        Mockito.verify( loanRepository ).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoanTest(){
        //Cenário
        Long id = 1l;
        Loan loan = createLoan();
        loan.setId(id);
        loan.setReturned(true);

        Mockito.when( loanRepository.save(loan)).thenReturn(loan);

        //Execução
        Loan updatedLoan = loanService.update(loan);

        Assertions.assertThat( updatedLoan.getReturned()).isTrue();
        Mockito.verify( loanRepository ).save(loan);

    }

    @Test
    @DisplayName("Deve filtrar empréstimos  pelas propriedades")
    public void findLoanTest(){
        //Cenário
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("123").build();
        Loan loan = createLoan();
        loan.setId(1l);

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Loan> lista = Arrays.asList(loan);
        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest,lista.size());
        Mockito.when(loanRepository.findByBookIsbnOrCustomer(
                Mockito.anyString()
                ,Mockito.anyString()
                , Mockito.any(PageRequest.class))).thenReturn(page);

        //Execução
        Page<Loan> result = loanService.find(loanFilterDTO, pageRequest);

        //Verificação
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent()).isEqualTo(lista);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
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
