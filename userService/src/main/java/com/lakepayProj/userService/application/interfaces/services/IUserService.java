package com.lakepayProj.userService.application.interfaces.services;

import com.lakepayProj.userService.domain.model.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public interface IUserService {
    List<User> findAllUsers();
    void updateUser(Long id, Map<String, Object> updates);
    User findUserById(Long id);
    void deleteUserByID(Long id);
    void saveUser(User user);
    User findUserByRole(String role);
}
