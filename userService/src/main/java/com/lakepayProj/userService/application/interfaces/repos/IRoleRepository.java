package com.lakepayProj.userService.application.interfaces.repos;

import com.lakepayProj.userService.domain.valueObject.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findRoleByName(String name);
}
