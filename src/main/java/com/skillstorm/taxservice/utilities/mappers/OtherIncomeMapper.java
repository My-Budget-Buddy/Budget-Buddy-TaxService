package com.skillstorm.taxservice.utilities.mappers;

import org.springframework.stereotype.Component;

import com.skillstorm.taxservice.dtos.OtherIncomeDto;
import com.skillstorm.taxservice.models.OtherIncome;
import com.skillstorm.taxservice.models.TaxReturn;

@Component
public class OtherIncomeMapper {
  public static OtherIncomeDto toDto(OtherIncome otherIncome) {

    if (otherIncome != null) {
      OtherIncomeDto dto = new OtherIncomeDto();

      dto.setId(otherIncome.getId());
      dto.setTaxReturnId(otherIncome.getTaxReturn().getId());
      dto.setLongTermCapitalGains(otherIncome.getLongTermCapitalGains());
      dto.setShortTermCapitalGains(otherIncome.getShortTermCapitalGains());
      dto.setOtherInvestmentIncome(otherIncome.getOtherInvestmentIncome());
      dto.setNetBusinessIncome(otherIncome.getNetBusinessIncome());
      dto.setAdditionalIncome(otherIncome.getAdditionalIncome());

      return dto;
    }

    return null;
  }

  public static OtherIncome toEntity(OtherIncomeDto dto) {

    if (dto != null) {
      OtherIncome entity = new OtherIncome();

      entity.setId(dto.getId());
      TaxReturn taxReturn = new TaxReturn();
      taxReturn.setId(dto.getTaxReturnId());
      entity.setTaxReturn(taxReturn);
      entity.setLongTermCapitalGains(dto.getLongTermCapitalGains());
      entity.setShortTermCapitalGains(dto.getShortTermCapitalGains());
      entity.setOtherInvestmentIncome(dto.getOtherInvestmentIncome());
      entity.setNetBusinessIncome(dto.getNetBusinessIncome());
      entity.setAdditionalIncome(dto.getAdditionalIncome());

      return entity;
    }

    return null;
  }

  public static OtherIncome updateEntity(OtherIncome entity, OtherIncomeDto dto) {
    entity.setLongTermCapitalGains(dto.getLongTermCapitalGains());
    entity.setShortTermCapitalGains(dto.getShortTermCapitalGains());
    entity.setOtherInvestmentIncome(dto.getOtherInvestmentIncome());
    entity.setNetBusinessIncome(dto.getNetBusinessIncome());
    entity.setAdditionalIncome(dto.getAdditionalIncome());

    return entity;
  }
}
