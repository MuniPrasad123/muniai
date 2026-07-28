package com.muniai.document.application;

public interface EmbeddingProvider {
    float[] embed(String text);
    String model();
    int dimension();
}
