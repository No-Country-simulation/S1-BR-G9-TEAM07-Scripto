package com.scripto.backend.user.service;

import com.scripto.backend.dto.UserUpdateDTO;
import com.scripto.backend.dto.UserViewDTO;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
     this.userRepository = userRepository;
    }


    public List<UserViewDTO> findAllUsers() {
        var users =  userRepository.findAll();
        var usersDto = users.stream().map(user -> new UserViewDTO(user)).toList();
        return usersDto;

    }

    private User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public UserViewDTO findUserById(Long id) {
        var user = findUserEntityById(id);
        return new UserViewDTO(user);
    }

    @Transactional
    public void updateUserById(Long id, UserUpdateDTO userUpdateDTO) {
        var user = findUserEntityById(id);
        user.update(userUpdateDTO);
        userRepository.save(user);
    }

}
