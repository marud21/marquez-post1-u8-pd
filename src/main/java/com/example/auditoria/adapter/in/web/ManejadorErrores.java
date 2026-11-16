package com.example.auditoria.adapter.in.web;

import com.example.auditoria.domain.valueobject.TransicionInvalidaException;
import com.example.auditoria.usecase.HallazgoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/** Traduce las excepciones del dominio y de los casos de uso a codigos HTTP. */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(HallazgoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> noEncontrado(HallazgoNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler({TransicionInvalidaException.class, IllegalStateException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> reglaDeNegocio(RuntimeException ex) {
        return Map.of("error", ex.getMessage(), "tipo", ex.getClass().getSimpleName());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));
        return errores;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> cuerpoInvalido(HttpMessageNotReadableException ex) {
        return Map.of("error", "Cuerpo de la peticion invalido (revise severidad y formato de fechas yyyy-MM-dd)");
    }
}
