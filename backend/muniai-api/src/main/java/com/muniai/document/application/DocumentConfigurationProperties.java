package com.muniai.document.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties("muniai.documents")
public record DocumentConfigurationProperties(String uploadDirectory, DataSize maxFileSize) {}
