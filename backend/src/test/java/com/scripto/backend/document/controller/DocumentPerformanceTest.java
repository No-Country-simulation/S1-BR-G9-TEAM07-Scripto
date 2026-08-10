package com.scripto.backend.document.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.service.DocumentService;
import com.scripto.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentControllerPerformanceTest {

    private MockMvc mockMvc;

    private DocumentService documentService;

    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {

        documentService = mock(DocumentService.class);
        objectMapper = new ObjectMapper();

        DocumentController controller =
                new DocumentController(documentService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        user = new User(
                "Usuário Performance",
                "performance@test.com",
                "12345678901",
                "password"
        );

        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                )
        );

        when(documentService.sendDocument(
                any(DocumentRequestDTO.class),
                any(User.class)
        )).thenReturn(null);

        when(documentService.findById(
                anyLong(),
                any(User.class)
        )).thenReturn(null);
    }

    @Test
    void measurePostDocumentLatency() throws Exception {

        int totalRequests = 30;

        List<Long> times = new ArrayList<>();

        DocumentRequestDTO request = new DocumentRequestDTO(
                "Documento de teste",
                "Este é um conteúdo suficientemente grande para passar pela validação.",
                Visibility.PRIVATE,
                false,
                false
        );

        String json = objectMapper.writeValueAsString(request);

        // Aquecimento
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(
                    post("/document")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
            ).andExpect(status().isCreated());
        }

        // Medições
        for (int i = 0; i < totalRequests; i++) {

            long start = System.nanoTime();

            mockMvc.perform(
                    post("/document")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
            ).andExpect(status().isCreated());

            long end = System.nanoTime();

            long elapsedMillis =
                    (end - start) / 1_000_000;

            times.add(elapsedMillis);
        }

        printPerformanceResult(
                "POST /document",
                times,
                5000
        );
    }

    @Test
    void measureGetDocumentLatency() throws Exception {

        int totalRequests = 30;

        List<Long> times = new ArrayList<>();

        // Aquecimento
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(
                    get("/document/1")
            ).andExpect(status().isOk());
        }

        // Medições
        for (int i = 0; i < totalRequests; i++) {

            long start = System.nanoTime();

            mockMvc.perform(
                    get("/document/1")
            ).andExpect(status().isOk());

            long end = System.nanoTime();

            long elapsedMillis =
                    (end - start) / 1_000_000;

            times.add(elapsedMillis);
        }

        printPerformanceResult(
                "GET /document/{id}",
                times,
                500
        );
    }

    private void printPerformanceResult(
            String endpoint,
            List<Long> times,
            long targetMillis
    ) {

        Collections.sort(times);

        int p95Index =
                (int) Math.ceil(times.size() * 0.95) - 1;

        p95Index = Math.max(
                0,
                Math.min(p95Index, times.size() - 1)
        );

        long p95 = times.get(p95Index);

        long minimum = times.get(0);

        long maximum = times.get(times.size() - 1);

        double average =
                times.stream()
                        .mapToLong(Long::longValue)
                        .average()
                        .orElse(0);

        System.out.println();
        System.out.println("==============================================");
        System.out.println("       PERFORMANCE TEST - QA-02");
        System.out.println("==============================================");
        System.out.println("Endpoint : " + endpoint);
        System.out.println("Requests : " + times.size());
        System.out.println("Minimum  : " + minimum + " ms");
        System.out.println("Average  : " + String.format("%.2f", average) + " ms");
        System.out.println("Maximum  : " + maximum + " ms");
        System.out.println("p95      : " + p95 + " ms");
        System.out.println("Meta     : <= " + targetMillis + " ms");
        System.out.println("----------------------------------------------");

        if (p95 <= targetMillis) {
            System.out.println("RESULTADO: PASS");
        } else {
            System.out.println("RESULTADO: FAIL");
        }

        System.out.println("==============================================");
        System.out.println();
    }
}