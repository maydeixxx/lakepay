package com.lakepayProj.userService.application.interfaces.services;

import com.lakepayProj.userService.domain.model.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface IUserService {
    List<User> findAllUsers();
    void updateUser(User user);
    User findUserById(Long id);
    void deleteUserByID(Long id);
    void saveUser(User user);
    User findUserByRole(String role);
}
