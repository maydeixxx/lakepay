package com.LakePayProj.userService.repository;

import com.LakePayProj.userService.entity.BlockedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IBlockedTokenRepository extends JpaRepository<BlockedToken, Long> {
    Optional<BlockedToken> findByToken(String token);
}
