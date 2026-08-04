package com.scripto.backend.classification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "scripto.ai")
public class ScriptoAiProperties {
    private boolean localEnabled = true;
    private String modelPath = "./models/scripto-model-v3";
}
