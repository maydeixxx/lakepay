package com.LakePayProj.paymentService.application.interfaces.repos;

import com.LakePayProj.paymentService.application.interfaces.repos.IUserBalanceRepository;
import com.LakePayProj.paymentService.domain.model.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserBalanceRepository extends JpaRepository<UserBalance, Long>{
    @Override
    Optional<UserBalance> findById(Long userId);

    @Override
    UserBalance save(UserBalance balance);
}