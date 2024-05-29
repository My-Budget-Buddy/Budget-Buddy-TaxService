package com.skillstorm.taxservice.dtos;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaxReturnCreditDto {

  private int id;
  private int taxReturnId;
  private int numDependents = 0;
  private int numDependentsAotc = 0;
  private int numChildren = 0;
  private BigDecimal childCareExpenses = BigDecimal.ZERO;
  private BigDecimal educationExpenses = BigDecimal.ZERO;
  private BigDecimal llcEducationExpenses = BigDecimal.ZERO;
  private BigDecimal iraContributions = BigDecimal.ZERO;
  private boolean claimedAsDependent = false;
  private boolean claimLlcCredit = false;
}
