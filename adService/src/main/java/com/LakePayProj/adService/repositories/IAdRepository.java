package com.LakePayProj.adService.repositories;

import com.LakePayProj.adService.entity.AdEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IAdRepository extends JpaRepository<AdEntity, Long> {
    Optional<AdEntity> findAdById(Long id);
    Optional<List<AdEntity>> findAdsByCategory(String category);
    Optional<List<AdEntity>> findAdsBySellerId(Long sellerId);
}
