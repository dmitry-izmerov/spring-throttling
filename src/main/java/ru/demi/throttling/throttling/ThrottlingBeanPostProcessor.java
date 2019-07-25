package ru.demi.throttling.throttling;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class ThrottlingBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, Class> classesByBeanName = new HashMap<>();
    private final Throttler throttler;

    public ThrottlingBeanPostProcessor(Throttler throttler) {
        this.throttler = throttler;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

        for (Annotation annotation : bean.getClass().getAnnotations()) {
            if (annotation instanceof Controller || annotation instanceof RestController) {
                return bean;
            }
        }

        for (Method method : bean.getClass().getMethods()) {
            Throttling annotation = method.getAnnotation(Throttling.class);
            if (annotation != null) {
                classesByBeanName.put(beanName, bean.getClass());
                break;
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        Class<?> clazz = classesByBeanName.get(beanName);
        if (clazz == null) {
            return bean;
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {

            Method found = clazz.getMethod(method.getName(), method.getParameterTypes());
            Throttling annotation = AnnotationUtils.findAnnotation(found, Throttling.class);
            if (annotation != null && !throttler.canExecute(found, annotation)) {
                throw new ThrottlingException();
            }

            return proxy.invokeSuper(obj, args);
        });

        return clazz.cast(enhancer.create());
    }
}