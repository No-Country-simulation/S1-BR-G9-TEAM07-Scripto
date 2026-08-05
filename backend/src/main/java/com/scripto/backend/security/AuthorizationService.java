package com.scripto.backend.security;

import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new UsernameNotFoundException("E-mail não pode ser nulo.");
        }
        String normalizedEmail = username.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + normalizedEmail);
        }
        return user;
    }
}