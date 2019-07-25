package ru.demi.throttling.throttling;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_GATEWAY, reason = "Too many requests")
public class ThrottlingException extends RuntimeException {
}