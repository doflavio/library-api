package io.github.doflavio.libraryapi.api.resource;

import io.github.doflavio.libraryapi.api.dto.BookDTO;
import io.github.doflavio.libraryapi.api.exception.ApiErros;
import io.github.doflavio.libraryapi.exception.BusinessException;
import io.github.doflavio.libraryapi.model.entity.Book;
import io.github.doflavio.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService service;
    private ModelMapper modelMapper;

    public BookController(BookService service,ModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @GetMapping("{id}")
    public BookDTO get(@PathVariable Long id) {
        return service.getById(id).map(
            book -> modelMapper.map(book, BookDTO.class)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest){
        Book filter = modelMapper.map(dto,Book.class);
        Page<Book> result = service.find(filter,pageRequest);
        List<BookDTO> list = result.getContent().stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageRequest, result.getTotalElements());

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        Book entity = modelMapper.map(dto,Book.class);

        entity = service.save(entity);

        return modelMapper.map(entity,BookDTO.class);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        Book book = service.getById(id)
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
        service.delete(book);

    }

    @PutMapping("{id}")
    public BookDTO update(@PathVariable Long id, BookDTO dto){
        return service.getById(id).map( book -> {
                    book.setAuthor(dto.getAuthor());
                    book.setTitle(dto.getTitle());
                    book = service.update(book);
                    return modelMapper.map(book,BookDTO.class);
        }).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
    }

}
