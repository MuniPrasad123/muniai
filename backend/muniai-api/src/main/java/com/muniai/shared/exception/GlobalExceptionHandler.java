package com.muniai.shared.exception;
import com.muniai.observability.CorrelationId; import com.muniai.shared.api.ApiError; import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant; import java.util.List; import org.slf4j.*; import org.springframework.http.*; import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class GlobalExceptionHandler {
 private static final Logger LOGGER=LoggerFactory.getLogger(GlobalExceptionHandler.class);
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e,HttpServletRequest r){return error(HttpStatus.BAD_REQUEST,"INVALID_REQUEST","The request is invalid.",r,e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).toList());}
 @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException e,HttpServletRequest r){return error(HttpStatus.BAD_REQUEST,"INVALID_REQUEST","The request body is invalid.",r,List.of());}
 @ExceptionHandler(MessageTooLongException.class) ResponseEntity<ApiError> tooLong(MessageTooLongException e,HttpServletRequest r){return error(HttpStatus.BAD_REQUEST,"MESSAGE_TOO_LONG","The message exceeds the configured maximum length.",r,List.of("maximumLength: "+e.maximum()));}
 @ExceptionHandler(AiProviderUnavailableException.class) ResponseEntity<ApiError> unavailable(AiProviderUnavailableException e,HttpServletRequest r){return error(HttpStatus.SERVICE_UNAVAILABLE,"AI_PROVIDER_UNAVAILABLE","The local AI provider is unavailable.",r,List.of());}
 @ExceptionHandler(AiProviderTimeoutException.class) ResponseEntity<ApiError> timeout(AiProviderTimeoutException e,HttpServletRequest r){return error(HttpStatus.GATEWAY_TIMEOUT,"AI_PROVIDER_TIMEOUT","The local AI provider timed out.",r,List.of());}
 @ExceptionHandler(MissingModelException.class) ResponseEntity<ApiError> missing(MissingModelException e,HttpServletRequest r){return error(HttpStatus.SERVICE_UNAVAILABLE,"AI_MODEL_NOT_CONFIGURED","The local AI model is not configured.",r,List.of());}
 @ExceptionHandler(MalformedProviderResponseException.class) ResponseEntity<ApiError> malformed(MalformedProviderResponseException e,HttpServletRequest r){return error(HttpStatus.BAD_GATEWAY,"AI_PROVIDER_INVALID_RESPONSE","The local AI provider returned an invalid response.",r,List.of());}
 @ExceptionHandler(Exception.class) ResponseEntity<ApiError> unexpected(Exception e,HttpServletRequest r){LOGGER.error("Unexpected request failure",e);return error(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL_ERROR","An unexpected internal error occurred.",r,List.of());}
 private ResponseEntity<ApiError> error(HttpStatus s,String c,String m,HttpServletRequest r,List<String>d){return ResponseEntity.status(s).body(new ApiError(Instant.now(),s.value(),c,m,r.getRequestURI(),CorrelationId.current(),d));}
}
