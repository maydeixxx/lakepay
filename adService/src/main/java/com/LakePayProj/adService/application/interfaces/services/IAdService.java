package com.LakePayProj.adService.application.interfaces.services;

import com.LakePayProj.adService.domain.Ad;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IAdService {
    void saveAd(Ad ad);
    void deleteAdById(Long id);
    Ad findAdById(Long id);
    List<Ad> findAllAds();
    List<Ad> findAdsByCategory(String category);
}
