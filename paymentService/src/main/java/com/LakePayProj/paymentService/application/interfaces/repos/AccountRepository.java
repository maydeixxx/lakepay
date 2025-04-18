package com.LakePayProj.paymentService.application.interfaces.repos;

import com.LakePayProj.paymentService.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {}
