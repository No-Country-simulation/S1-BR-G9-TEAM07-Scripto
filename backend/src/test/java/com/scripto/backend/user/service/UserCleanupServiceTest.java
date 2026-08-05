package com.scripto.backend.user.service;

import com.scripto.backend.aianalyse.repository.AIAnalysisRepository;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.tag.repository.DocumentTagRepository;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserCleanupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentTagRepository documentTagRepository;

    @Mock
    private AIAnalysisRepository analysisRepository;

    @InjectMocks
    private UserCleanupService userCleanupService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveExcluirContasExpiradas() {
        User user1 = new User();
        user1.setId(1L);
        user1.setDeletedAt(LocalDateTime.now().minusDays(31));
        user1.setActive(false);
        user1.setDocuments(new ArrayList<>());

        User user2 = new User();
        user2.setId(2L);
        user2.setDeletedAt(LocalDateTime.now().minusDays(40));
        user2.setActive(false);
        user2.setDocuments(new ArrayList<>());

        List<User> users = List.of(user1, user2);
        when(userRepository.findAllByActiveFalseAndDeletedAtBefore(any(LocalDateTime.class))).thenReturn(users);

        userCleanupService.deleteExpiredAccounts();

        verify(userRepository).delete(user1);
        verify(userRepository).delete(user2);
    }

    @Test
    void deveNaoFalharQuandoNaoExistiremContasExpiradas() {
        when(userRepository.findAllByActiveFalseAndDeletedAtBefore(any(LocalDateTime.class))).thenReturn(List.of());

        userCleanupService.deleteExpiredAccounts();

        verify(userRepository, never()).delete(any());
    }
}