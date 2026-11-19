package com.muniai.memory.application;
public class MemoryException extends RuntimeException {
    private final String code;
    public MemoryException(String code,String message){super(message);this.code=code;}
    public String code(){return code;}
}
