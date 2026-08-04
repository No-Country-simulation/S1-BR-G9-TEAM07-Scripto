package com.scripto.backend.vector.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "scripto.vector")
public class VectorProperties {
    private boolean enabled = true;
    private String url = "jdbc:postgresql://localhost:5432/scripto_vector";
    private String username = "scripto";
    private String password = "scripto";
    private int maximumPoolSize = 5;
    private long connectionTimeoutMs = 3_000;
}
