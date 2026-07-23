package com.muniai.shared.exception;

import java.util.UUID;

public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException(UUID id) { super(id.toString()); }
}
