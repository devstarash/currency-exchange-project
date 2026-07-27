package ru.starashchuk.currency.exchange.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.starashchuk.currency.exchange.controller.exception.*;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final int numberExceptionCode = 0;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleNotValidArgumentsException(BindingResult result) {
        List<String> exceptionsMessages = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
        String exceptionMessage = exceptionsMessages.get(numberExceptionCode);
        ExceptionResponse response = new ExceptionResponse(exceptionMessage);
        return response;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNotFoundException(NotFoundException ex) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return response;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleBadRequestException(BadRequestException ex) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return response;
    }

    @ExceptionHandler(AlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleAlreadyExistException(AlreadyExistException ex) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return response;
    }

    @ExceptionHandler(DatabaseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleDatabaseExceptionException(DatabaseException ex) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage());
        return response;
    }
}
