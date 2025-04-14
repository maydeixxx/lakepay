package com.LakePayProj.adService.application.interfaces.repositories;

import com.LakePayProj.adService.infrastructure.AdEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAdRepository extends JpaRepository<AdEntity, Long> {
    AdEntity findAdById(Long id);
    List<AdEntity> findAdsByCategory(String category);
}
