package com.LakePayProj.paymentService.application.interfaces.mappers;

import com.LakePayProj.paymentService.api.DTOs.TransactionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IPaymentMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "adId", source = "adId")
    @Mapping(target = "status", source = "status")
    TransactionDto transactionToDto(Transaction transaction);

    Transaction dtoToTransaction(TransactionDto dto);
}