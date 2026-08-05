package com.scripto.backend.classification.service;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.classification.domain.ClassificationSource;
import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.classification.exception.ClassificationUnavailableException;
import com.scripto.backend.classification.local.RuntimeBundleLoader;
import com.scripto.backend.classification.local.RuntimeModelBundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassificationResultValidatorTest {
    private ClassificationResultValidator validator;

    @BeforeEach
    void setUp() {
        RuntimeBundleLoader loader = mock(RuntimeBundleLoader.class);
        RuntimeModelBundle bundle = mock(RuntimeModelBundle.class);
        when(loader.bundle()).thenReturn(bundle);
        when(bundle.taxonomyCategories()).thenReturn(Set.of("Backend Development", "Other"));
        validator = new ClassificationResultValidator(loader);
    }

    @Test
    void rejectsOtherWithoutSuggestion() {
        FinalClassification result = result("Other", null, List.of("gardening"));
        assertThrows(ClassificationUnavailableException.class, () -> validator.validate(result));
    }

    @Test
    void acceptsOtherWithSuggestion() {
        FinalClassification result = result("Other", "Urban Gardening", List.of("gardening"));
        FinalClassification validated = validator.validate(result);
        assertEquals("Urban Gardening", validated.suggestedCategory());
    }

    @Test
    void removesDuplicateTagsAndPreservesOrder() {
        FinalClassification result = result("Backend Development", null, List.of("java", "java", "spring boot"));
        FinalClassification validated = validator.validate(result);
        assertEquals(List.of("java", "spring boot"), validated.tags());
    }

    private FinalClassification result(String category, String suggestion, List<String> tags) {
        return new FinalClassification(
                category, null, Level.INTERMEDIATE, null, tags,
                ClassificationSource.NEMOTRON, "scripto-model-v3", "nemotron",
                List.of(), suggestion, null, null
        );
    }
}
