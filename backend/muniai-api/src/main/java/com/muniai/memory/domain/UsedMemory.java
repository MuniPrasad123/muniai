package com.muniai.memory.domain;
import java.util.UUID;
public record UsedMemory(UUID memoryId, MemoryCategory category, String key, String displayLabel) {}
