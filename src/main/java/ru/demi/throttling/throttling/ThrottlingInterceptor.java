package ru.demi.throttling.throttling;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ThrottlingInterceptor extends HandlerInterceptorAdapter {

    private final Throttler throttler;

    public ThrottlingInterceptor(Throttler throttler) {
        this.throttler = throttler;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            Throttling annotation = handlerMethod.getMethod().getAnnotation(Throttling.class);
            if (annotation != null && !throttler.canExecute(handlerMethod.getMethod(), annotation)) {
                throw new ThrottlingException();
            }
        }

        return true;
    }

}