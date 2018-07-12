package com.example.demorabbitmq;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ReceiveChecker {
    public ConcurrentMap<String, String> checker = new ConcurrentHashMap<>();

    public void recordSend(String key) {
        this.checker.put(key, "");
    }

    public void recordReceive(String key) {
        this.checker.remove(key);
    }

    public Set<String> remains() {
        return new TreeSet<>(this.checker.keySet());
    }
}
