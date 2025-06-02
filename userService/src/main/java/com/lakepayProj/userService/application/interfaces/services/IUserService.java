package com.lakepayProj.userService.application.interfaces.services;

import com.lakepayProj.userService.api.DTOs.UserUpdateDTO;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.domain.valueObject.Role;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface IUserService {
    List<User> findAllUsers();
    void updateUser(Long id, UserUpdateDTO updates);
    User findUserById(Long id);
    User findUserByTgId(Long id);
    void deleteUserByID(Long id);
    void saveUser(User user);
//    List<User> findUsersByRole(Role role);
    void subscribe(Long id, String category);
    void unSubscribe(Long id, String category);
    List<User> findUserBySubs(String category);
}
