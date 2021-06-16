package io.github.doflavio.libraryapi.model.repository;

import io.github.doflavio.libraryapi.model.entity.Book;
import io.github.doflavio.libraryapi.model.entity.Loan;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.validation.constraints.AssertTrue;
import java.time.LocalDate;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro")
    public void existsByBookAndNotReturnedTest(){
        //Cenário
       Loan loan = createAndPersitLoan();
       Book book = loan.getBook();

        //Execução
        boolean exists = repository.existsByBookAndNotReturned(book);

        //Verificação
        Assertions.assertThat(exists).isTrue();

    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer")
    public void findByBookIsbnOrCustomerTest(){
        //Cenário
        Loan loan = createAndPersitLoan();

        //Execução
        Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "fulano", PageRequest.of(0, 10));

        //Verificação
        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent().contains(loan));
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);


    }

    private Book createNewBook(String isbn) {
        return Book.builder().isbn(isbn).author("Fulano").title("As aventuras").build();
    }

    private Loan createAndPersitLoan(){
        Book book = createNewBook("123");
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(LocalDate.now()).build();
        entityManager.persist(loan);

        return loan;
    }
}
