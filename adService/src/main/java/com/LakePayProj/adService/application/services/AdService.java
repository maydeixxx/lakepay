package com.LakePayProj.adService.application.services;

import com.LakePayProj.adService.application.interfaces.mappers.IAdMapper;
import com.LakePayProj.adService.application.interfaces.repositories.IAdRepository;
import com.LakePayProj.adService.application.interfaces.services.IAdService;
import com.LakePayProj.adService.application.services.kafka.AdProducer;
import com.LakePayProj.adService.domain.Ad;
import com.LakePayProj.adService.infrastructure.AdEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AdService implements IAdService {
    private final IAdRepository repository;
    private final IAdMapper mapper;

    @Override
    public List<Ad> findAdsByCategory(String category) {
        List<AdEntity> adsByCategory = repository.findAdsByCategory(category);
        return adsByCategory.stream()
                .map(mapper::adEntityToAdDomain)
                .toList();
    }

    @Override
    public void updateAd(Long id, Map<String, Object> updates) {
        AdEntity adById = repository.findAdById(id);
        updates.forEach((key, value) -> {
                switch (key) {
                    case "title" -> adById.setTitle(String.valueOf(value));
                    case "body" -> adById.setBody(String.valueOf(value));
                    case "category" -> adById.setCategory(String.valueOf(value));
                    case "quantity" -> adById.setQuantity((Integer) value);
                    case "sold" -> adById.setSold((Boolean) value);
                }
        });
        repository.saveAndFlush(adById);
    }

    @Override
    public void saveAd(Ad ad) {
        repository.save(mapper.adDomainToEntity(ad));
    }

    @Override
    public void deleteAdById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Ad findAdById(Long id) {
        return mapper.adEntityToAdDomain(repository.findAdById(id));
    }

    @Override
    public List<Ad> findAllAds() {
        List<AdEntity> all = repository.findAll();
        return all.stream()
                .map(mapper::adEntityToAdDomain)
                .toList();
    }
}
