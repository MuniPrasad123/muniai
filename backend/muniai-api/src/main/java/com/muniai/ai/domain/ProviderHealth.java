package com.muniai.ai.domain;

public record ProviderHealth(Status status, String provider, String model) {
    public enum Status { UP, DOWN }
}
