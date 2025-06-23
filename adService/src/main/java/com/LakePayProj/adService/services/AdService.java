package com.LakePayProj.adService.services;

import com.LakePayProj.adService.DTOs.AdDtoUpdate;
import com.LakePayProj.adService.exceptions.AdNotFoundException;
import com.LakePayProj.adService.exceptions.DatabaseException;
import com.LakePayProj.adService.exceptions.EmptyAdListException;
import com.LakePayProj.adService.mappers.IAdMapper;
import com.LakePayProj.adService.repositories.IAdRepository;
import com.LakePayProj.adService.domain.Ad;
import com.LakePayProj.adService.infrastructure.AdEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AdService implements IAdService {
    private final IAdRepository repository;
    private final IAdMapper mapper;

    @Override
    public List<Ad> findAdsByCategory(String category) {
        List<AdEntity> adsByCategory = repository.findAdsByCategory(category).orElseThrow(() -> new EmptyAdListException(String.format("Ad by category [%s] not found", category)));
        return adsByCategory.stream()
                .map(mapper::adEntityToAdDomain)
                .toList();
    }

    @Override
    public void updateAd(Long id, AdDtoUpdate updates) {
        AdEntity adById = repository.findAdById(id).orElseThrow(() -> new AdNotFoundException(String.format("Ad [%s] not found", id)));
        switch (updates.field()) {
            case "title" -> adById.setTitle(updates.title());
            case "body" -> adById.setBody(updates.body());
            case "category" -> adById.setCategory(updates.category());
            case "price" -> adById.setPrice(updates.price());
            case "login" -> adById.setLogin(updates.login());
            case "password" -> adById.setPassword(updates.password());
            case "sold" -> adById.setSold(updates.sold());
        }
        try {
            repository.saveAndFlush(adById);
        } catch (Exception e) {
            throw new DatabaseException("Error while updating ad", e);
        }
    }

    @Override
    public void saveAd(Ad ad) {
        LocalDate dateOfPush = LocalDate.now();
        ad.setDateOfPush(dateOfPush);
        try {
            repository.save(mapper.adDomainToEntity(ad));
        } catch (Exception e) {
            throw new DatabaseException("Error while saving ad", e);
        }
    }

    @Override
    public void deleteAdById(Long id) {
        try {
            repository.deleteById(id);
        } catch (Exception e) {
            throw new DatabaseException(String.format("error while deleting ad %s", id), e);
        }
    }

    @Override
    public Ad findAdById(Long id) {
        AdEntity ad = repository.findAdById(id).orElseThrow(() -> new AdNotFoundException(String.format("Ad %s not found", id)));
        return mapper.adEntityToAdDomain(ad);
    }

    @Override
    public List<Ad> findAllAds() {
        List<AdEntity> all = repository.findAll();
        if (all.isEmpty()) {
            throw new EmptyAdListException("Ads not found");
        }
        return all.stream()
                .map(mapper::adEntityToAdDomain)
                .sorted((ad1, ad2) -> ad2.getDateOfPush().compareTo(ad1.getDateOfPush()))
                .toList();
    }

    @Override
    public List<Ad> findAdsBySellerId(Long sellerId) {
        List<AdEntity> ads = repository.findAdsBySellerId(sellerId).orElseThrow(() -> new EmptyAdListException(String.format("Ad by sellerId [%s] not found", sellerId)));
        return ads.stream()
                .map(mapper::adEntityToAdDomain)
                .toList();
    }
}
