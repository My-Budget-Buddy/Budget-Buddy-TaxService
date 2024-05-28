package com.skillstorm.taxservice.dtos;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OtherIncomeDto {
  
  private int id;
  private int taxReturnId;
  private BigDecimal longTermCapitalGains = BigDecimal.ZERO;
  private BigDecimal shortTermCapitalGains = BigDecimal.ZERO;
  private BigDecimal otherInvestmentIncome = BigDecimal.ZERO;
  private BigDecimal netBusinessIncome = BigDecimal.ZERO;
  private BigDecimal additionalIncome = BigDecimal.ZERO;

  @JsonIgnore
  public BigDecimal getSum() {
    return longTermCapitalGains.add(shortTermCapitalGains)
      .add(otherInvestmentIncome).add(netBusinessIncome)
      .add(additionalIncome);
  }
}
