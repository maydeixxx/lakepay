package com.LakePayProj.adService.application.interfaces.mappers;

import com.LakePayProj.adService.api.DTOs.AdDto;
import com.LakePayProj.adService.domain.Ad;
import com.LakePayProj.adService.infrastructure.AdEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IAdMapper {
    Ad adEntityToAdDomain(AdEntity entity);

    AdEntity adDomainToEntity(Ad ad);

    Ad adDtoToDomain(AdDto adDto);

    AdDto adDomainToDto(Ad ad);
}
