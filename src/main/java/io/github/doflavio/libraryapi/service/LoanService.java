package io.github.doflavio.libraryapi.service;

import io.github.doflavio.libraryapi.api.dto.LoanFilterDTO;
import io.github.doflavio.libraryapi.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LoanService {
    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable);
}
