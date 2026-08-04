package com.scripto.backend.classification.service;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.classification.config.ScriptoAiProperties;
import com.scripto.backend.classification.domain.*;
import com.scripto.backend.classification.local.LocalClassificationEngine;
import com.scripto.backend.classification.nemotron.NemotronClassification;
import com.scripto.backend.classification.nemotron.NemotronClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassificationOrchestratorTest {
    @Test
    void acceptsValidLocalResultWithoutCallingNemotron() {
        LocalClassificationEngine local = mock(LocalClassificationEngine.class);
        NemotronClient nemotron = mock(NemotronClient.class);
        ClassificationResultValidator validator = mock(ClassificationResultValidator.class);
        ScriptoAiProperties properties = new ScriptoAiProperties();
        properties.setLocalEnabled(true);
        LocalClassificationResult localResult = new LocalClassificationResult(
                "Backend Development", 0.91, Level.INTERMEDIATE, 0.80,
                List.of(new TagPrediction("java", 0.9)), 0.8, List.of(),
                "scripto-model-v3", new float[384]
        );
        when(local.classify(anyString(), anyString())).thenReturn(localResult);
        when(validator.validate(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ClassificationOrchestrator orchestrator = new ClassificationOrchestrator(local, nemotron, validator, properties);
        FinalClassification result = orchestrator.classify(new ClassificationInput("Title", "Content", true));

        assertEquals(ClassificationSource.LOCAL, result.source());
        verifyNoInteractions(nemotron);
    }

    @Test
    void usesNemotronWhenLocalPolicyRejectsResult() {
        LocalClassificationEngine local = mock(LocalClassificationEngine.class);
        NemotronClient nemotron = mock(NemotronClient.class);
        ClassificationResultValidator validator = mock(ClassificationResultValidator.class);
        ScriptoAiProperties properties = new ScriptoAiProperties();
        properties.setLocalEnabled(true);
        LocalClassificationResult localResult = new LocalClassificationResult(
                "Backend Development", 0.20, Level.INTERMEDIATE, 0.80,
                List.of(new TagPrediction("java", 0.9)), 0.5,
                List.of(FallbackReason.LOW_CATEGORY_CONFIDENCE),
                "scripto-model-v3", new float[384]
        );
        when(local.classify(anyString(), anyString())).thenReturn(localResult);
        when(nemotron.classify(anyString(), anyString())).thenReturn(
                new NemotronClassification("Backend Development", Level.INTERMEDIATE, List.of("java"), null)
        );
        when(nemotron.modelName()).thenReturn("nvidia/nemotron-3-super-120b-a12b");
        when(validator.validate(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FinalClassification result = new ClassificationOrchestrator(local, nemotron, validator, properties)
                .classify(new ClassificationInput("Title", "Content", true));

        assertEquals(ClassificationSource.NEMOTRON, result.source());
        assertEquals(List.of(FallbackReason.LOW_CATEGORY_CONFIDENCE), result.fallbackReasons());
    }
    @Test
    void doesNotCallNemotronWhenExternalAiIsNotAllowed() {
        LocalClassificationEngine local = mock(LocalClassificationEngine.class);
        NemotronClient nemotron = mock(NemotronClient.class);
        ClassificationResultValidator validator = mock(ClassificationResultValidator.class);
        ScriptoAiProperties properties = new ScriptoAiProperties();
        properties.setLocalEnabled(true);
        when(local.classify(anyString(), anyString())).thenReturn(new LocalClassificationResult(
                "Backend Development", 0.20, Level.INTERMEDIATE, 0.80,
                List.of(new TagPrediction("java", 0.9)), 0.5,
                List.of(FallbackReason.LOW_CATEGORY_CONFIDENCE),
                "scripto-model-v3", new float[384]
        ));

        ClassificationOrchestrator orchestrator = new ClassificationOrchestrator(local, nemotron, validator, properties);
        assertThrows(
                com.scripto.backend.classification.exception.ClassificationUnavailableException.class,
                () -> orchestrator.classify(new ClassificationInput("Title", "Content", false))
        );
        verifyNoInteractions(nemotron);
    }

    @Test
    void recordsDisabledLocalModelAsFallbackReason() {
        LocalClassificationEngine local = mock(LocalClassificationEngine.class);
        NemotronClient nemotron = mock(NemotronClient.class);
        ClassificationResultValidator validator = mock(ClassificationResultValidator.class);
        ScriptoAiProperties properties = new ScriptoAiProperties();
        properties.setLocalEnabled(false);
        when(nemotron.classify(anyString(), anyString())).thenReturn(
                new NemotronClassification("Backend Development", Level.BEGINNER, List.of("java"), null)
        );
        when(nemotron.modelName()).thenReturn("nemotron");
        when(validator.validate(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FinalClassification result = new ClassificationOrchestrator(local, nemotron, validator, properties)
                .classify(new ClassificationInput("Title", "Content", true));

        assertEquals(List.of(FallbackReason.LOCAL_MODEL_DISABLED), result.fallbackReasons());
        verifyNoInteractions(local);
    }

}
