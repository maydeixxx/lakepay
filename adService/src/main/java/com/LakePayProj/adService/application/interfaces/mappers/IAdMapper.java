package com.LakePayProj.adService.application.interfaces.mappers;

import com.LakePayProj.adService.api.DTOs.AdDto;
import com.LakePayProj.adService.domain.Ad;
import com.LakePayProj.adService.infrastructure.AdEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface IAdMapper {
    Ad adEntityToAdDomain(AdEntity entity);

    AdEntity adDomainToEntity(Ad ad);

    Ad adDtoToDomain(AdDto adDto);

    @Mapping(target = "login", ignore = true)
    @Mapping(target = "password", ignore = true)
    AdDto adDomainToDto(Ad ad);

    default Map<String, String> getDataFromAd(Ad ad) {
        return Map.of(
                "login", ad.getLogin(),
                "password", ad.getPassword()
        );
    }
}
