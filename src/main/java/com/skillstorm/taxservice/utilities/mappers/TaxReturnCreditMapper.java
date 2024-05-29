package com.skillstorm.taxservice.utilities.mappers;

import com.skillstorm.taxservice.models.TaxReturn;
import org.springframework.stereotype.Component;

import com.skillstorm.taxservice.dtos.TaxReturnCreditDto;
import com.skillstorm.taxservice.models.TaxReturnCredit;

@Component
public class TaxReturnCreditMapper {
  
  public static TaxReturnCreditDto toDto(TaxReturnCredit taxReturnCredit) {
    if (taxReturnCredit != null) {
      TaxReturnCreditDto dto = new TaxReturnCreditDto();
      dto.setId(taxReturnCredit.getId());
      dto.setTaxReturnId(taxReturnCredit.getTaxReturn().getId());
      dto.setNumDependents(taxReturnCredit.getNumDependents());
      dto.setNumDependentsAotc(taxReturnCredit.getNumDependentsAotc());
      dto.setNumChildren(taxReturnCredit.getNumChildren());
      dto.setChildCareExpenses(taxReturnCredit.getChildCareExpenses());
      dto.setEducationExpenses(taxReturnCredit.getEducationExpenses());
      dto.setLlcEducationExpenses(taxReturnCredit.getLlcEducationExpenses());
      dto.setIraContributions(taxReturnCredit.getIraContributions());
      dto.setClaimedAsDependent(taxReturnCredit.isClaimedAsDependent());
      dto.setClaimLlcCredit(taxReturnCredit.isClaimLlcCredit());
      return dto;
    }

    return null;
  }

  public static TaxReturnCredit toEntity(TaxReturnCreditDto dto) {
    if (dto != null) {
      TaxReturnCredit entity = new TaxReturnCredit();
      entity.setId(dto.getId());
      entity.setTaxReturn(new TaxReturn(dto.getTaxReturnId()));
      entity.setNumDependents(dto.getNumDependents());
      entity.setNumDependentsAotc(dto.getNumDependentsAotc());
      entity.setNumChildren(dto.getNumChildren());
      entity.setChildCareExpenses(dto.getChildCareExpenses());
      entity.setEducationExpenses(dto.getEducationExpenses());
      entity.setLlcEducationExpenses(dto.getLlcEducationExpenses());
      entity.setIraContributions(dto.getIraContributions());
      entity.setClaimedAsDependent(dto.isClaimedAsDependent());
      entity.setClaimLlcCredit(dto.isClaimLlcCredit());
      return entity;
    }
    
    return null;
  }

  public static TaxReturnCredit updateEntity(TaxReturnCredit entity, TaxReturnCreditDto dto) {
    // Update the fields of the existing Address entity
    entity.setNumDependents(dto.getNumDependents());
    entity.setNumDependentsAotc(dto.getNumDependentsAotc());
    entity.setNumChildren(dto.getNumChildren());
    entity.setChildCareExpenses(dto.getChildCareExpenses());
    entity.setEducationExpenses(dto.getEducationExpenses());
    entity.setLlcEducationExpenses(dto.getLlcEducationExpenses());
    entity.setIraContributions(dto.getIraContributions());
    entity.setClaimedAsDependent(dto.isClaimedAsDependent());
    entity.setClaimLlcCredit(dto.isClaimLlcCredit());
    return entity;
  }
}
