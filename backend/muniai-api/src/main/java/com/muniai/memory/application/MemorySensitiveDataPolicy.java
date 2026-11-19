package com.muniai.memory.application;

import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class MemorySensitiveDataPolicy {
    private static final Pattern SECRET_ASSIGNMENT=Pattern.compile("(?i)\\b(password|passcode|api[-_ ]?key|access[-_ ]?token|refresh[-_ ]?token|private[-_ ]?key|card|cvv|bank account|social security|aadhaar|authentication answer)\\b");
    private static final Pattern TOKEN_SHAPE=Pattern.compile("(?i)(sk-[a-z0-9_-]{12,}|-----BEGIN [A-Z ]*PRIVATE KEY-----|bearer\\s+[a-z0-9._-]{12,})");
    public void validate(String key,String value){
        String combined=(key+" "+value).toLowerCase(Locale.ROOT);
        if(SECRET_ASSIGNMENT.matcher(combined).find()||TOKEN_SHAPE.matcher(value).find())
            throw new MemoryException("SENSITIVE_MEMORY_REJECTED","Secrets and highly sensitive values cannot be stored in memory.");
    }
}
