package com.scripto.backend.classification.nemotron;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.classification.exception.ClassificationUnavailableException;
import com.scripto.backend.classification.local.RuntimeBundleLoader;
import com.scripto.backend.classification.local.RuntimeModelBundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NemotronClientTest {

    private NemotronProperties properties;
    private RuntimeBundleLoader loader;
    private RuntimeModelBundle bundle;
    private NemotronClient nemotronClient;

    @BeforeEach
    void setUp() {
        properties = mock(NemotronProperties.class);
        loader = mock(RuntimeBundleLoader.class);
        bundle = mock(RuntimeModelBundle.class);

        when(properties.getApiKey()).thenReturn("test-api-key");
        when(properties.getModel()).thenReturn("test-model");
        when(properties.getConnectTimeoutMs()).thenReturn(1000);
        when(properties.getReadTimeoutMs()).thenReturn(1000);
        when(properties.getMaxAttempts()).thenReturn(1);
        when(properties.getBaseUrl()).thenReturn("http://localhost:9999");

        when(loader.bundle()).thenReturn(bundle);

        when(bundle.taxonomyCategories()).thenReturn(
                Set.of(
                        "Backend Development",
                        "Data Science",
                        "Frontend Development",
                        "Other"
                )
        );

        nemotronClient = new NemotronClient(
                properties,
                new ObjectMapper(),
                loader
        );
    }

    @Test
    void shouldRejectInvalidDifficultyReturnedByNemotron() {

        InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                () -> invokeParseDifficulty("INVALID_LEVEL")
        );

        assertInstanceOf(
                ClassificationUnavailableException.class,
                exception.getCause()
        );

        assertEquals(
                "Nemotron returned an invalid difficulty",
                exception.getCause().getMessage()
        );
    }

    @Test
    void shouldAcceptBeginnerDifficulty() throws Exception {
        Level result = invokeParseDifficulty("BEGINNER");

        assertEquals(Level.BEGINNER, result);
    }

    @Test
    void shouldAcceptIntermediateDifficulty() throws Exception {
        Level result = invokeParseDifficulty("INTERMEDIATE");

        assertEquals(Level.INTERMEDIATE, result);
    }

    @Test
    void shouldAcceptAdvancedDifficulty() throws Exception {
        Level result = invokeParseDifficulty("ADVANCED");

        assertEquals(Level.ADVANCED, result);
    }

    @Test
    void shouldNormalizeDifficultyToUpperCase() throws Exception {
        Level result = invokeParseDifficulty("intermediate");

        assertEquals(Level.INTERMEDIATE, result);
    }

    @Test
    void shouldIgnoreSpacesAroundDifficulty() throws Exception {
        Level result = invokeParseDifficulty("  advanced  ");

        assertEquals(Level.ADVANCED, result);
    }

    @Test
    void shouldRejectBlankDifficulty() {

        InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                () -> invokeParseDifficulty("")
        );

        assertInstanceOf(
                ClassificationUnavailableException.class,
                exception.getCause()
        );

        assertEquals(
                "Nemotron returned an invalid difficulty",
                exception.getCause().getMessage()
        );
    }

    @Test
    void shouldReturnConfiguredModelName() {
        assertEquals(
                "test-model",
                nemotronClient.modelName()
        );
    }

    private Level invokeParseDifficulty(String difficulty) throws Exception {

        Method method = NemotronClient.class.getDeclaredMethod(
                "parseDifficulty",
                String.class
        );

        method.setAccessible(true);

        try {
            return (Level) method.invoke(
                    nemotronClient,
                    difficulty
            );
        } catch (InvocationTargetException exception) {
            throw exception;
        }
    }
}