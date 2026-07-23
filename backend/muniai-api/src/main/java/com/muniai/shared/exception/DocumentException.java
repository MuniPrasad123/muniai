package com.muniai.shared.exception;

public class DocumentException extends RuntimeException {
    private final String code;
    public DocumentException(String code, String message) { super(message); this.code=code; }
    public DocumentException(String code, String message, Throwable cause) { super(message, cause); this.code=code; }
    public String code() { return code; }
}
