package ru.demi.throttling.controller;

import ru.demi.throttling.throttling.Throttling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.temporal.ChronoUnit;

@RestController
public class RootController {

    @Throttling(limit = 50, timeUnit = ChronoUnit.MINUTES)
    @GetMapping("/")
    public String getEmptyString() {
        return "";
    }
}
