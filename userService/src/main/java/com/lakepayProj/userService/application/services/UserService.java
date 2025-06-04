package com.lakepayProj.userService.application.services;

import com.lakepayProj.userService.api.DTOs.UserUpdateDTO;
import com.lakepayProj.userService.application.interfaces.mappers.IUserMapper;
import com.lakepayProj.userService.application.interfaces.repos.IRoleRepository;
import com.lakepayProj.userService.application.interfaces.repos.IUserRepository;
import com.lakepayProj.userService.application.interfaces.services.IUserService;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.domain.valueObject.Role;
import com.lakepayProj.userService.infrastructure.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService, UserDetailsService {
    private final IUserRepository userRepository;
    private final IUserMapper mapper;
    private final IRoleRepository roleRepository;

    @Override
    public List<User> findAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                .map(mapper::userEntityToUser)
                .toList();
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateDTO updates) {
        UserEntity userById = userRepository.findUserById(id);
        if (userById == null) {
            throw new IllegalArgumentException("User with id [" + id + "] not found");
        }

        if (updates.getUserName() != null) {
            userById.setUsername(updates.getUserName());
            userRepository.save(userById);
        }
        if (updates.getUrlPhoto() != null) {
            userById.setUrlPhoto(updates.getUrlPhoto());
            userRepository.save(userById);
        }
        if (updates.getRoleIds() != null) {
            Collection<Role> roles = new ArrayList<>();
            for (Integer roleId : updates.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Role with id [" + roleId + "] not found"));
                roles.add(role);
            }
            userById.getRoles().clear();
            userById.getRoles().addAll(roles);
            userRepository.save(userById);
        }
        if (updates.getAdSub() != null) {
            List<String> subscriptions = userById.getSubscriptions();
            if (subscriptions == null) {
                subscriptions = new ArrayList<>();
                userById.setSubscriptions(subscriptions);
                userRepository.save(userById);
            }
            subscriptions.add(updates.getAdSub());
        }
        if (updates.getBalance() != null) {
            userById.setBalance(updates.getBalance());
            userRepository.save(userById);
        }
        if (updates.getDelSub() != null) {
            List<String> subscriptions = userById.getSubscriptions();
            if (subscriptions != null) {
                subscriptions.removeIf(category -> category.equals(updates.getDelSub()));
                userById.setSubscriptions(subscriptions);
                userRepository.save(userById);
            }
        }
    }


    @Override
    public User findUserById(Long id) {
        return mapper.userEntityToUser(userRepository.findUserById(id));
    }

    @Override
    public User findUserByTgId(Long id) {
        UserEntity userByTgId = userRepository.findUserByTgId(id);
        return mapper.userEntityToUser(userByTgId);
    }

    @Override
    @Transactional
    public void deleteUserByID(Long id) {
        UserEntity userById = userRepository.findUserById(id);
        userRepository.delete(userById);
    }

    /**
     * Создание пользователя
     */
    @Override
    public void saveUser(User user) {
        userRepository.save(mapper.userToUserEntity(user));
    }

//    @Override
//    public List<User> findUsersByRole(Role role) {
//        List<UserEntity> usersByRole = repository.findUsersByRoles(role);
//        return usersByRole.stream()
//                .map(mapper::userEntityToUser)
//                .toList();
//    }

    @Override
    public void subscribe(Long id, String category) {
        UserEntity userById = userRepository.findUserById(id);
        if (userById == null) {
            throw new IllegalArgumentException("User with tgId [" + id + "] not found");
        }

        List<String> subscriptions = userById.getSubscriptions();

        if (subscriptions.contains(category)) {
            System.err.println("Пользователь уже подписан на эту категорию!");
            return;
        }

        subscriptions.add(category);
        userById.setSubscriptions(subscriptions);
        userRepository.saveAndFlush(userById);
    }

    @Override
    public void unSubscribe(Long id, String category) {
        UserEntity userById = userRepository.findUserById(id);
        if (userById == null) {
            throw new IllegalArgumentException("User with id = [" + id + "] is null");
        }
        List<String> subscriptions = userById.getSubscriptions();
        subscriptions.removeIf(category1 -> category1.equals(category));
        userById.setSubscriptions(subscriptions);
        userRepository.saveAndFlush(userById);
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return mapper.userEntityToUser(userEntity);
    }

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужно для Spring Security
     *
     * @return пользователь
     */
    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    /**
     * Получение текущего пользователя из контекста Spring Security
     *
     * @return текущий пользователь
     */
    public User getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    @Override
    public List<User> findUserBySubs(String category) {
        List<UserEntity> all = userRepository.findAll();
        return all.stream()
                .filter(userEntity -> userEntity.getSubscriptions().stream().anyMatch(category1 -> category1.equals(category)))
                .map(mapper ::userEntityToUser)
                .toList();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException (
                String.format("User '%s' not found", username)
        ));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                null,
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList()
        );
    }
}
