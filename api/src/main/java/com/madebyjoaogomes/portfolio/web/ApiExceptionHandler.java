package com.madebyjoaogomes.portfolio.web;

import com.madebyjoaogomes.portfolio.content.ContentNotFoundException;
import com.madebyjoaogomes.portfolio.content.SlugConflictException;
import com.madebyjoaogomes.portfolio.project.InvalidProjectOrderException;
import java.util.Comparator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Maps domain errors to RFC 9457 problem details; Spring MVC's own errors come from the base class. */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ContentNotFoundException.class)
    ProblemDetail handleNotFound(ContentNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Conteúdo não encontrado", exception.getMessage());
    }

    @ExceptionHandler(SlugConflictException.class)
    ProblemDetail handleSlugConflict(SlugConflictException exception) {
        return problem(HttpStatus.CONFLICT, "Slug em uso", exception.getMessage());
    }

    @ExceptionHandler(InvalidProjectOrderException.class)
    ProblemDetail handleInvalidOrder(InvalidProjectOrderException exception) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Ordem inválida", exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrity(DataIntegrityViolationException exception) {
        // A concurrent write slipped past the service checks; the constraint name is enough to debug it.
        log.warn("Data integrity violation: {}", exception.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "Conflito de dados", "A alteração viola uma regra de integridade do conteúdo");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        var problem = problem(HttpStatus.BAD_REQUEST, "Requisição inválida", "Um ou mais campos são inválidos");
        problem.setProperty("errors", exception.getBindingResult().getAllErrors().stream()
                .map(ApiExceptionHandler::toFieldError)
                .sorted(Comparator.comparing(error -> error.get("field")))
                .toList());
        return handleExceptionInternal(exception, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    private static Map<String, String> toFieldError(ObjectError error) {
        var field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
        return Map.of("field", field, "message", String.valueOf(error.getDefaultMessage()));
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
