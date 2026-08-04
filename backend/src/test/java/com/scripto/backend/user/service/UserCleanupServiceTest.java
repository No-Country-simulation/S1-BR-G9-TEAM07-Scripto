package com.scripto.backend.user.service;

import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserCleanupServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserCleanupService userCleanupService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveExcluirContasExpiradas() {

        // Arrange
        User user1 = new User();
        user1.setDeletedAt(LocalDateTime.now().minusDays(31));
        user1.setActive(false);

        User user2 = new User();
        user2.setDeletedAt(LocalDateTime.now().minusDays(40));
        user2.setActive(false);

        List<User> users = List.of(user1, user2);

        when(userRepository.findAllByActiveFalseAndDeletedAtBefore(any(LocalDateTime.class)))
                .thenReturn(users);

        // Act
        userCleanupService.deleteExpiredAccounts();

        // Assert
        verify(userRepository)
                .findAllByActiveFalseAndDeletedAtBefore(any(LocalDateTime.class));

        verify(userRepository)
                .deleteAll(users);
    }

    @Test
    void deveNaoFalharQuandoNaoExistiremContasExpiradas() {

        // Arrange
        when(userRepository.findAllByActiveFalseAndDeletedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());

        // Act
        userCleanupService.deleteExpiredAccounts();

        // Assert
        verify(userRepository)
                .findAllByActiveFalseAndDeletedAtBefore(any(LocalDateTime.class));

        verify(userRepository)
                .deleteAll(List.of());
    }

    

}