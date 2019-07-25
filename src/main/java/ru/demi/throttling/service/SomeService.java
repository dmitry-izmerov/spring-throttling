package ru.demi.throttling.service;

import ru.demi.throttling.throttling.Throttling;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
public class SomeService {

    @Throttling(limit = 50, timeUnit = ChronoUnit.SECONDS, time = 5)
    public String method() {
        return "something";
    }
}
