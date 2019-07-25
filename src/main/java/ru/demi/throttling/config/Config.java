package ru.demi.throttling.config;

import ru.demi.throttling.throttling.Throttler;
import ru.demi.throttling.throttling.ThrottlingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Config implements WebMvcConfigurer {

    private final Throttler throttler;

    public Config(Throttler throttler) {
        this.throttler = throttler;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ThrottlingInterceptor(throttler));
    }
}
