package com.LakePayProj.paymentService.application.interfaces.mappers;

import com.LakePayProj.paymentService.api.DTOs.PaymentDto;
import com.LakePayProj.paymentService.domain.PaymentDomain;
import com.LakePayProj.paymentService.infrastructure.PaymentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IPaymentMapper {
    PaymentEntity paymentDomainToEntity(PaymentDomain domain);

    PaymentDomain paymentEntityToDomain(PaymentEntity entity);

    PaymentEntity dtoToEntity(PaymentDto paymentDto);

    PaymentDto entityToPaymentDto(PaymentEntity paymentEntity);
}
