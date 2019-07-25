package ru.demi.throttling.throttling;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class Throttler {

    private Map<ThrottlingKey, List<Long>> timestamps = new ConcurrentHashMap<>();

    public boolean canExecute(Method method, Throttling annotation) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String remoteAddr = servletRequest.getRemoteAddr();

        ThrottlingKey key = new ThrottlingKey(remoteAddr, method);
        long timestamp = System.currentTimeMillis();

        if (!timestamps.containsKey(key)) {
            List<Long> list = new ArrayList<>();
            list.add(timestamp);
            timestamps.put(key, list);
            return true;
        }

        timestamps.computeIfPresent(key, (oldKey, list) -> {
            list.add(timestamp);
            return timestamps.get(key).stream()
                .filter(ts -> {
                    LocalDateTime tsDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), TimeZone.getDefault().toZoneId());
                    LocalDateTime now = LocalDateTime.now();
                    return !tsDate.isBefore(now.minus(annotation.time(), annotation.timeUnit()));
                })
                .limit(annotation.limit())
                .collect(Collectors.toList());
        });

        return timestamps.get(key).size() != annotation.limit();
    }

    private static class ThrottlingKey {
        String remoteAddr;
        Method method;

        public ThrottlingKey(String remoteAddr, Method method) {
            this.remoteAddr = remoteAddr;
            this.method = method;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ThrottlingKey that = (ThrottlingKey) o;
            return remoteAddr.equals(that.remoteAddr) &&
                method.equals(that.method);
        }

        @Override
        public int hashCode() {
            return Objects.hash(remoteAddr, method);
        }
    }
}


