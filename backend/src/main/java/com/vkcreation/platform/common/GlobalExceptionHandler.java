package com.vkcreation.platform.common;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException ex) {
  Map<String,String> fields=new LinkedHashMap<>(); ex.getBindingResult().getFieldErrors().forEach(e->fields.putIfAbsent(e.getField(),e.getDefaultMessage()));
  return error(HttpStatus.BAD_REQUEST,"Validation failed",Map.of("fields",fields));
 }
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<Map<String,Object>> status(ResponseStatusException ex) {
  HttpStatus s=HttpStatus.valueOf(ex.getStatusCode().value()); return error(s,ex.getReason()==null?s.getReasonPhrase():ex.getReason(),Map.of());
 }
 @ExceptionHandler(BadCredentialsException.class) ResponseEntity<Map<String,Object>> credentials(){return error(HttpStatus.UNAUTHORIZED,"Invalid email or password",Map.of());}
 @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<Map<String,Object>> unreadable(){return error(HttpStatus.BAD_REQUEST,"Request body is invalid",Map.of());}
 @ExceptionHandler(Exception.class) ResponseEntity<Map<String,Object>> unexpected(Exception ex){return error(HttpStatus.INTERNAL_SERVER_ERROR,"Unexpected server error",Map.of());}
 private ResponseEntity<Map<String,Object>> error(HttpStatus s,String m,Map<String,Object>d){Map<String,Object>b=new LinkedHashMap<>();b.put("timestamp",Instant.now().toString());b.put("status",s.value());b.put("error",s.getReasonPhrase());b.put("message",m);b.put("details",d);return ResponseEntity.status(s).body(b);}
}