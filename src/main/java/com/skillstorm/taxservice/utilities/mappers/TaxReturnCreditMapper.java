package com.skillstorm.taxservice.utilities.mappers;

import org.springframework.stereotype.Component;

import com.skillstorm.taxservice.dtos.TaxReturnCreditDto;
import com.skillstorm.taxservice.models.TaxReturnCredit;

@Component
public class TaxReturnCreditMapper {
  
  public static TaxReturnCreditDto toDto(TaxReturnCredit taxReturnCredit) {
    if (taxReturnCredit != null) {
      TaxReturnCreditDto dto = new TaxReturnCreditDto();
      dto.setTaxReturnId(taxReturnCredit.getTaxReturn().getId());
      dto.setNumDependentsAotc(taxReturnCredit.getNumDependentsAotc());
      dto.setNumChildren(taxReturnCredit.getNumChildren());
      dto.setChildCareExpenses(taxReturnCredit.getChildCareExpenses());
      dto.setEducationExpenses(taxReturnCredit.getEducationExpenses());
      dto.setLlcEducationExpenses(taxReturnCredit.getLlcEducationExpenses());
      dto.setIraContributions(taxReturnCredit.getIraContributions());
      dto.setClaimedAsDependent(taxReturnCredit.isClaimedAsDependent());
      return dto;
    }

    return null;
  }

  public static TaxReturnCredit toEntity(TaxReturnCreditDto dto) {
    if (dto != null) {
      TaxReturnCredit entity = new TaxReturnCredit();
      entity.setNumDependentsAotc(dto.getNumDependentsAotc());
      entity.setNumChildren(dto.getNumChildren());
      entity.setChildCareExpenses(dto.getChildCareExpenses());
      entity.setEducationExpenses(dto.getEducationExpenses());
      entity.setLlcEducationExpenses(dto.getLlcEducationExpenses());
      entity.setIraContributions(dto.getIraContributions());
      entity.setClaimedAsDependent(dto.isClaimedAsDependent());
      return entity;
    }
    
    return null;
  }

  public static TaxReturnCredit updateEntity(TaxReturnCredit entity, TaxReturnCreditDto dto) {
    // Update the fields of the existing Address entity
    entity.setNumDependentsAotc(dto.getNumDependentsAotc());
    entity.setNumChildren(dto.getNumChildren());
    entity.setChildCareExpenses(dto.getChildCareExpenses());
    entity.setEducationExpenses(dto.getEducationExpenses());
    entity.setIraContributions(dto.getIraContributions());
    entity.setClaimedAsDependent(dto.isClaimedAsDependent());
    return entity;
  }
}
