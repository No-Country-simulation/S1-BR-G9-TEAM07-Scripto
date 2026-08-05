package com.scripto.backend.classification.nemotron;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "scripto.nemotron")
public class NemotronProperties {
    private String apiKey = "";
    private String baseUrl = "https://integrate.api.nvidia.com/v1/chat/completions";
    private String model = "nvidia/nemotron-3-super-120b-a12b";
    private int connectTimeoutMs = 5_000;
    private int readTimeoutMs = 45_000;
    private int maxAttempts = 2;
}
