package ru.demi.throttling.controller;

import ru.demi.throttling.throttling.Throttling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.temporal.ChronoUnit;

@RestController
public class TestController {

    @Throttling(limit = 50, timeUnit = ChronoUnit.SECONDS, time = 5)
    @GetMapping("/test")
    public String testCall() {
        return "some";
    }
}
