package com.skillstorm.taxservice.services;

import org.springframework.stereotype.Service;

import com.skillstorm.taxservice.constants.State;
import com.skillstorm.taxservice.dtos.TaxReturnCreditDto;
import com.skillstorm.taxservice.dtos.TaxReturnDeductionDto;
import com.skillstorm.taxservice.dtos.TaxReturnDto;
import com.skillstorm.taxservice.dtos.W2Dto;
import com.skillstorm.taxservice.models.CapitalGainsTax;
import com.skillstorm.taxservice.models.FilingStatus;
import com.skillstorm.taxservice.models.StateTax;
import com.skillstorm.taxservice.models.TaxBracket;
import com.skillstorm.taxservice.models.taxcredits.ChildTaxCredit;
import com.skillstorm.taxservice.models.taxcredits.DependentCareTaxCredit;
import com.skillstorm.taxservice.models.taxcredits.DependentCareTaxCreditLimit;
import com.skillstorm.taxservice.models.taxcredits.EarnedIncomeTaxCredit;
import com.skillstorm.taxservice.models.taxcredits.EducationTaxCreditAotc;
import com.skillstorm.taxservice.models.taxcredits.EducationTaxCreditLlc;
import com.skillstorm.taxservice.models.taxcredits.SaversTaxCredit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaxCalculatorService {

    private final TaxCreditService taxCreditService;
    private final FilingStatusService filingStatusService;
    private final TaxBracketService taxBracketService;
    private final StateTaxService stateTaxService;
    private final CapitalGainsTaxService capitalGainsTaxService;
    private final StandardDeductionService standardDeductionService;

    public TaxCalculatorService(TaxCreditService taxCreditService,
                                FilingStatusService filingStatusService,
                                TaxBracketService taxBracketService,
                                StateTaxService stateTaxService,
                                CapitalGainsTaxService capitalGainsTaxService,
                                StandardDeductionService standardDeductionService) {
      this.taxCreditService = taxCreditService;
      this.filingStatusService = filingStatusService;
      this.taxBracketService = taxBracketService;
      this.stateTaxService = stateTaxService;
      this.capitalGainsTaxService = capitalGainsTaxService;
      this.standardDeductionService = standardDeductionService;
    }

    public TaxReturnDto calculateAll(TaxReturnDto taxReturn) {

      // First, perform any income related calculations; calculating total income, then agi, then taxable income
      calculateTotalIncome(taxReturn);
      calculateAgi(taxReturn);
      calculateTaxableIncome(taxReturn);

      // Second, calculate tax liabilities
      calculateWithholdings(taxReturn);
      calculateFederalTaxes(taxReturn);
      calculateStateTaxes(taxReturn);
      calculateCapitalGainsTax(taxReturn);

      // Third, calculate applicable credits applied to federal tax liability
      calculateNonRefundableTaxCredits(taxReturn);
      calculateRefundableTaxCredits(taxReturn);

      // Finally, scale the relevant BigDecimals to be presentable
      taxReturn.setTotalIncome(taxReturn.getTotalIncome().setScale(2, RoundingMode.HALF_UP));
      taxReturn.setAdjustedGrossIncome(taxReturn.getAdjustedGrossIncome().setScale(2, RoundingMode.HALF_UP));
      taxReturn.setTaxableIncome(taxReturn.getTaxableIncome().setScale(2, RoundingMode.HALF_UP));
      taxReturn.setTotalCredits(taxReturn.getTotalCredits().setScale(2, RoundingMode.HALF_UP));
      taxReturn.setFederalRefund(taxReturn.getFederalRefund().setScale(2, RoundingMode.HALF_UP));

      return taxReturn;
    }


  public TaxReturnDto calculateTotalIncome(TaxReturnDto taxReturn) {
      BigDecimal totalIncome = taxReturn.getW2s().stream().map(W2Dto::getWages)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

      if (taxReturn.getOtherIncome() != null) {
        BigDecimal totalOtherIncome = taxReturn.getOtherIncome().getSum();
        totalIncome = totalIncome.add(totalOtherIncome);
      }

      taxReturn.setTotalIncome(totalIncome);

      return taxReturn;
    }


    public TaxReturnDto calculateAgi(TaxReturnDto taxReturn) {
      
      // If there are no deductions to calculate, simply set AGI to the total income
      if (taxReturn.getDeductions().isEmpty()) {
        taxReturn.setAdjustedGrossIncome(taxReturn.getTotalIncome());
        return taxReturn;
      }

      // Set deduction limits based on user's age and filing status:
      setDeductionLimits(taxReturn);

      // Calculate total deductions:
      BigDecimal totalDeductions = taxReturn.getDeductions().stream()
              .map(deduction -> {
                BigDecimal amountSpent = deduction.getAmountSpent();
                BigDecimal agiLimit = deduction.getAgiLimit();
                // Deduct the smaller of the two so we don't exceed the limit:
                return amountSpent.min(agiLimit);
              })
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      // AdjustedGrossIncome = TotalIncome - TotalDeductions:
      BigDecimal agi = taxReturn.getTotalIncome().subtract(totalDeductions);

      // Restrict agi to be >= 0
      taxReturn.setAdjustedGrossIncome(agi.max(BigDecimal.ZERO));

      return taxReturn;
    }

    // Adjust deduction limits based on user's age and filing status
  private void setDeductionLimits(TaxReturnDto taxReturn) {

    // Calculate user's age
    LocalDate dateOfBirth = LocalDate.parse(taxReturn.getDateOfBirth());
    int age = (int) ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now());

    // Set up conditions to make the checks more readable:
    boolean isMarriedFilingJointly = com.skillstorm.taxservice.constants.FilingStatus.MARRIED_FILING_JOINTLY.equals(taxReturn.getFilingStatus());
    boolean is50OrOlder = age >= 50;
    boolean is55OrOlder = age >= 55;
    BigDecimal totalIncome = taxReturn.getTotalIncome();

    // Modify agiLimits for each deduction for users matching the above conditions as applicable:
    taxReturn.getDeductions().stream().map(deduction -> {
      switch (deduction.getDeduction()) {
        // Health Savings Account:
        case 1:
          if (isMarriedFilingJointly) {
            deduction.setAgiLimit(BigDecimal.valueOf(7750));
          }
          if (is55OrOlder) {
            deduction.setAgiLimit(deduction.getAgiLimit().add(BigDecimal.valueOf(1000)));
          }
          break;

        // IRA Contributions:
        case 2:
          if (is50OrOlder) {
            deduction.setAgiLimit(deduction.getAgiLimit().add(BigDecimal.valueOf(1000)));
          }
          break;

        // Student Loan Interest payments:
        case 3:
          if (isMarriedFilingJointly) {
            deduction.setAgiLimit(BigDecimal.valueOf(185000));
          }
          // For Student Loan Interest, eligibility is based on the user's adjusted income. You cannot claim
          // the deduction if your adjusted gross income exceeds the agiLimit, but because of the way
          // we're going to be comparing the AGI to the agiLimit in the next step, we want to set it
          // to 0 if the Adjusted Gross Income exceeds the eligibility cap:
          if (totalIncome.compareTo(deduction.getAgiLimit()) > 0) {
            deduction.setAgiLimit(BigDecimal.ZERO);
          }
          break;

        // Educator Expenses:
        case 4:
          if (isMarriedFilingJointly) {
            deduction.setAgiLimit(deduction.getAgiLimit().multiply(BigDecimal.valueOf(2)));
          }
          break;

      }
      return deduction;
    });
  }


  public TaxReturnDto calculateTaxableIncome(TaxReturnDto taxReturn) {

      // Calculate total itemized deductions. Here, agiLimit represents the percentage of the user's agi that can be deducted from
      // a single claim:
      BigDecimal totalItemizedDeductions = taxReturn.getDeductions().stream()
              .filter(TaxReturnDeductionDto::isItemized)
              .map(deduction -> {
                BigDecimal adjustedGrossIncome = taxReturn.getAdjustedGrossIncome();
                BigDecimal maxDeduction = adjustedGrossIncome.multiply(deduction.getAgiLimit());
                BigDecimal amountSpent = deduction.getAmountSpent();
                return amountSpent.min(maxDeduction);
              })
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Get the standard deduction amount based on user's filing status:
      BigDecimal standardDeduction =
              BigDecimal.valueOf(standardDeductionService.getByFilingStatusId(taxReturn.getFilingStatus().getValue())
                      .getDeductionAmount());

      // Note: We choose the greater of standard deduction vs itemized deductions. We may want to find a way to
      // communicate which choice was made to the user in the future:
      BigDecimal totalDeductions = totalItemizedDeductions.max(standardDeduction);

      // TaxableIncome = AdjustedGrossIncome - TotalDeductions:
      BigDecimal taxableIncome = taxReturn.getAdjustedGrossIncome().subtract(totalDeductions);

      // Restrict taxable income to be >= 0
      taxReturn.setTaxableIncome(taxableIncome.max(BigDecimal.ZERO));

      return taxReturn;
    }


  // Calculate taxes already withheld:
  private void calculateWithholdings(TaxReturnDto taxReturn) {

    // Calculate total federal taxes already paid:
    taxReturn.setFedTaxWithheld(taxReturn.getW2s().stream()
            .map(W2Dto::getFederalIncomeTaxWithheld)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP));

    // Calculate total social security taxes already paid:
    taxReturn.setSocialSecurityTaxWithheld(taxReturn.getW2s().stream()
            .map(W2Dto::getSocialSecurityTaxWithheld)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP));

    // Calculate total medicare taxes already paid:
    taxReturn.setMedicareTaxWithheld(taxReturn.getW2s().stream()
            .map(W2Dto::getMedicareTaxWithheld)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP));

    // Calculate total state taxes already paid:
    taxReturn.setStateTaxWithheld(taxReturn.getW2s().stream()
            .map(W2Dto::getStateIncomeTaxWithheld)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP));
  }


    // Calculate total federal tax liability
    public TaxReturnDto calculateFederalTaxes(TaxReturnDto taxReturn) {

      // Calculate total federal income taxes already paid:
      BigDecimal taxesPaid = taxReturn.getFedTaxWithheld();

      // If taxable income is 0, refund all income taxes paid:
      if (taxReturn.getTaxableIncome().compareTo(BigDecimal.ZERO) <= 0) {
        taxReturn.setFederalRefund(taxesPaid);
        return taxReturn;
      }

      // Get taxable income
      BigDecimal taxableIncome = taxReturn.getTaxableIncome();

      // Get tax brackets based on user's filing status
      List<TaxBracket> taxBrackets = taxBracketService.findByFilingStatusID(taxReturn.getFilingStatus().getValue());

      // Initialize tax amount & remaining income
      BigDecimal taxesOwed = BigDecimal.ZERO;
      BigDecimal remainingIncome = taxableIncome;

      // Calculate the tax amount at each bracket user's income falls into
      for (TaxBracket bracket : taxBrackets) {
          double minIncome = bracket.getMinIncome();
          double maxIncome = bracket.getMaxIncome();
          BigDecimal taxRate = bracket.getRate();

          if (remainingIncome.compareTo(BigDecimal.ZERO) <= 0) {
              break; // No more income to tax
          }

          BigDecimal taxableAmountInBracket = remainingIncome.min(BigDecimal.valueOf(maxIncome - minIncome));
          BigDecimal taxInBracket = taxableAmountInBracket.multiply(taxRate);
          taxesOwed = taxesOwed.add(taxInBracket);
          remainingIncome = remainingIncome.subtract(taxableAmountInBracket);
      }

      // Set the federal tax amount in the TaxReturnDto
      taxReturn.setFederalRefund(taxesPaid
                                          .subtract(taxesOwed));

      // Return the total federal tax liability
      return taxReturn;
    }


    // Calculate total state tax liability
    public TaxReturnDto calculateStateTaxes(TaxReturnDto taxReturn) {

      // Determine the tax return's state
      State taxReturnState = taxReturn.getState();

      // Group W2s by state
      Map<State, List<W2Dto>> w2sByState = taxReturn.getW2s().stream()
        .collect(Collectors.groupingBy(W2Dto::getState));

      // Initialize tax amount:
      BigDecimal totalTaxAmount = BigDecimal.ZERO;

      // Calculate taxes for each group of W2s by state
      for (Map.Entry<State, List<W2Dto>> entry : w2sByState.entrySet()) {
        State state = entry.getKey();
        List<W2Dto> w2sForState = entry.getValue();

        // Calculate total wages for the state
        BigDecimal totalWagesForState = w2sForState.stream()
                                                  .map(W2Dto::getWages)
                                                  .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Check if the state matches the tax return's state
        if (state.equals(taxReturnState) && taxReturn.getOtherIncome() != null) {

          // Include OtherIncome values in the total state wages

          totalWagesForState = totalWagesForState.add(taxReturn.getOtherIncome().getOtherInvestmentIncome()
                  .add(taxReturn.getOtherIncome().getNetBusinessIncome())
                  .add(taxReturn.getOtherIncome().getAdditionalIncome())
                  .add(taxReturn.getOtherIncome().getShortTermCapitalGains()));
        }

        // Calculate state tax for the total wages of the state
        BigDecimal stateTaxForState = calculateTaxForIncome(totalWagesForState, state.getValue());

        // Update total state tax amount
        totalTaxAmount = totalTaxAmount.add(stateTaxForState);
      }
  
      // If the tax return's state does not match any of the W2s' states,
      // calculate taxes on OtherIncome for the tax return's state separately
      if (!w2sByState.containsKey(taxReturnState) && taxReturn.getOtherIncome() != null) {
        BigDecimal otherIncomeTaxForState = calculateTaxForIncome(
                taxReturn.getOtherIncome().getOtherInvestmentIncome()
                  .add(taxReturn.getOtherIncome().getNetBusinessIncome())
                  .add(taxReturn.getOtherIncome().getShortTermCapitalGains())
                  .add(taxReturn.getOtherIncome().getAdditionalIncome()),
                taxReturnState.getValue()
        );
        // Add OtherIncome taxes to the total state tax amount
        totalTaxAmount = totalTaxAmount.add(otherIncomeTaxForState);
      }
  
      // Set results in the relevant DTO fields
      taxReturn.setStateRefund(taxReturn.getStateTaxWithheld()
              .subtract(totalTaxAmount)
              .setScale(2, RoundingMode.HALF_UP));
  
      // Return updated tax return
      return taxReturn;
    }

  
    private BigDecimal calculateTaxForIncome(BigDecimal income, int stateId) {
      BigDecimal totalTaxAmount = BigDecimal.ZERO;
      List<StateTax> taxBrackets = stateTaxService.getTaxBracketsByStateId(stateId);
  
      BigDecimal remainingIncome = income;
  
      for (StateTax bracket : taxBrackets) {
          BigDecimal bracketIncome = remainingIncome.min(BigDecimal.valueOf(bracket.getIncomeRange()));
  
          if (bracketIncome.compareTo(BigDecimal.ZERO) == 0) {
              bracketIncome = remainingIncome;
          }
  
          totalTaxAmount = totalTaxAmount.add(bracketIncome.multiply(bracket.getRate()));
          remainingIncome = remainingIncome.subtract(bracketIncome);
  
          if (remainingIncome.compareTo(BigDecimal.ZERO) <= 0) {
              break;
          }
      }
  
      return totalTaxAmount;
    }


    public TaxReturnDto calculateCapitalGainsTax(TaxReturnDto taxReturn) {

      // Get total taxable income
      BigDecimal taxableIncome = taxReturn.getTaxableIncome();

      // If user hasn't input any other income, or if long term cap gains is 0 simply return
      if (taxReturn.getOtherIncome() == null || taxReturn.getOtherIncome().getLongTermCapitalGains().compareTo(BigDecimal.ZERO) <= 0) {
        return taxReturn;
      }

      // Get capital gains amount
      BigDecimal longTermCapitalGains = taxReturn.getOtherIncome().getLongTermCapitalGains();

      // Initialize tax amounts
      BigDecimal longTermCapitalGainsTaxAmount = BigDecimal.ZERO;

      // Get long term capital gains federal tax rates based on user's filing status
      List<CapitalGainsTax> longTermCapitalGainsTaxBrackets = capitalGainsTaxService.findByFilingStatusID(taxReturn.getFilingStatus().getValue());

      // Initialize remaining income
      BigDecimal ordinaryIncome = taxableIncome.subtract(longTermCapitalGains);
      BigDecimal remainingCapGains = longTermCapitalGains;

      // Calculate long term capital gains tax
      for (CapitalGainsTax bracket : longTermCapitalGainsTaxBrackets) {

        // If at the last bracket (income range is 0) tax remaining gains at that rate
        if (bracket.getIncomeRange() == 0) {
          longTermCapitalGainsTaxAmount = longTermCapitalGainsTaxAmount.add(remainingCapGains.multiply(bracket.getRate()));
          break;
        }

        // Start at tax bracket that ordinary income falls into
        if (ordinaryIncome.compareTo(BigDecimal.valueOf(bracket.getIncomeRange())) >= 0) {
          ordinaryIncome = ordinaryIncome.subtract(BigDecimal.valueOf(bracket.getIncomeRange()).max(BigDecimal.ZERO));
          continue;
        }

        // Any leftover ordinary income is subtracted from the income range
        double bracketRange = (double) bracket.getIncomeRange() - ordinaryIncome.doubleValue();
        ordinaryIncome = BigDecimal.ZERO;

        // Calculate the taxable amount of the gains
        double taxableAmount = Math.min(bracketRange, remainingCapGains.doubleValue());

        // Calculate the tax amount
        longTermCapitalGainsTaxAmount = longTermCapitalGainsTaxAmount.add(BigDecimal.valueOf(taxableAmount).multiply(bracket.getRate()));

        // Subtract taxable amount to see if next bracket should apply
        remainingCapGains = remainingCapGains.subtract(BigDecimal.valueOf(taxableAmount));

        // If there are no more gains to tax, break here
        if (remainingCapGains.doubleValue() <= 0) break;
      }

      // Add tax amount to federal refund of tax return
      taxReturn.setFederalRefund(taxReturn.getFederalRefund()
                                          .subtract(longTermCapitalGainsTaxAmount));

      return taxReturn;
    }


    public TaxReturnDto calculateNonRefundableTaxCredits(TaxReturnDto taxReturn) {
      // If user hasn't input any tax credit info, simply return
      if (taxReturn.getTaxCredit() == null) {
        return taxReturn;
      }

      calculateEducationTaxCreditLlc(taxReturn);
      calculateSaversTaxCredit(taxReturn);
      calculateDependentCareTaxCredit(taxReturn);
      return taxReturn;
    }


    public TaxReturnDto calculateRefundableTaxCredits(TaxReturnDto taxReturn) {
      // If user hasn't input any tax credit info, simply return
      if (taxReturn.getTaxCredit() == null) {
        return taxReturn;
      }

      calculateChildTaxCredits(taxReturn);
      calculateEducationTaxCreditAotc(taxReturn);
      calculateEarnedIncomeTaxCredit(taxReturn);
      return taxReturn;
    }


    public TaxReturnDto calculateChildTaxCredits(TaxReturnDto taxReturn) {
      TaxReturnCreditDto taxReturnCredit = taxReturn.getTaxCredit();
      FilingStatus filingStatus = filingStatusService.findById(taxReturn.getFilingStatus().getValue());
      ChildTaxCredit childTaxCredit = filingStatus.getChildTaxCredit();

      // Get relevant fields
      int numDependents = taxReturnCredit.getNumDependents();
      BigDecimal agi = taxReturn.getAdjustedGrossIncome();

      // If no qualifying dependents, simply return tax return
      if (numDependents <= 0) {
        return taxReturn;
      }

      BigDecimal potentialCreditAmount = BigDecimal.valueOf(numDependents * childTaxCredit.getPerQualifyingChild());

      // Convert int income threshold to BigDecimal
      BigDecimal incomeThreshold = BigDecimal.valueOf(childTaxCredit.getIncomeThreshold());

      // Calculate agiExcess using BigDecimal methods
      BigDecimal agiExcess = agi.subtract(incomeThreshold).max(BigDecimal.ZERO);

      // Divide agiExcess by 1000
      BigDecimal divisor = new BigDecimal("1000");
      BigDecimal quotient = agiExcess.divide(divisor, RoundingMode.HALF_UP);

      // Multiply the result by 50
      BigDecimal multiplier = new BigDecimal("50");
      BigDecimal phaseoutAmount = quotient.multiply(multiplier);

      // Get final potential credit amount
      BigDecimal creditAfterPhaseout = potentialCreditAmount.subtract(phaseoutAmount);

      // Get current federal tax liability
      BigDecimal taxRefund = taxReturn.getFederalRefund();

      if (taxRefund.compareTo(BigDecimal.ZERO) <= 0) {
        taxRefund = taxRefund.add(creditAfterPhaseout);
      } else {
        BigDecimal creditLimit = BigDecimal.valueOf(childTaxCredit.getRefundLimit());
        BigDecimal difference = BigDecimal.valueOf(childTaxCredit.getPerQualifyingChild()).subtract(creditLimit);
        creditAfterPhaseout = creditAfterPhaseout.subtract(difference.multiply(BigDecimal.valueOf(numDependents)));
        taxRefund = taxRefund.add(creditAfterPhaseout);
      }

      // Add credits to total credits, and set tax refund back into dto
      taxReturn.setTotalCredits(taxReturn.getTotalCredits().add(creditAfterPhaseout));
      taxReturn.setFederalRefund(taxRefund);
      
      return taxReturn;
    }

    // refundable
    public TaxReturnDto calculateEarnedIncomeTaxCredit(TaxReturnDto taxReturn) {

      TaxReturnCreditDto taxReturnCredit = taxReturn.getTaxCredit();
      FilingStatus filingStatus = filingStatusService.findById(taxReturn.getFilingStatus().getValue());
      EarnedIncomeTaxCredit earnedIncomeTaxCredit = filingStatus.getEarnedIncomeTaxCredit();

      // Get agi value
      BigDecimal agi = taxReturn.getAdjustedGrossIncome();

      // If user's investment income is greater than the investment income limit: not eligible for this credit; return
      if (taxReturn.getOtherIncome().getOtherInvestmentIncome().doubleValue() >= earnedIncomeTaxCredit.getInvestmentIncomeLimit() ||
          agi.compareTo(BigDecimal.ZERO) <= 0) {
        return taxReturn;
      }

      // Initialize variables
      int numDependents = taxReturnCredit.getNumDependents();
      int agiThreshold;
      int creditAmount;

      // Determine agi threshold and credit amount based on # of dependents
      switch(numDependents) {
        case 0:
          agiThreshold = earnedIncomeTaxCredit.getAgiThreshold0Children();
          creditAmount = earnedIncomeTaxCredit.getAmount0Children();
          break;
        case 1:
          agiThreshold = earnedIncomeTaxCredit.getAgiThreshold1Children();
          creditAmount = earnedIncomeTaxCredit.getAmount1Children();
          break;
        case 2:
          agiThreshold = earnedIncomeTaxCredit.getAgiThreshold2Children();
          creditAmount = earnedIncomeTaxCredit.getAmount2Children();
          break;
        default:
          agiThreshold = earnedIncomeTaxCredit.getAgiThreshold3Children();
          creditAmount = earnedIncomeTaxCredit.getAmount3Children();
      }

      // if agi is greater than agi threshold: not eligible, return
      if (agi.compareTo(BigDecimal.valueOf(agiThreshold)) > 0) return taxReturn;

      // If tax credit is applicable, add credit to federal refund and to total credits
      taxReturn.setTotalCredits(taxReturn.getTotalCredits().add(BigDecimal.valueOf(creditAmount)));
      taxReturn.setFederalRefund(taxReturn.getFederalRefund().add(BigDecimal.valueOf(creditAmount)));

      return taxReturn;
    }
    

    // refundable
    public TaxReturnDto calculateEducationTaxCreditAotc(TaxReturnDto taxReturn) {
      TaxReturnCreditDto taxReturnCredit = taxReturn.getTaxCredit();
      FilingStatus filingStatus = filingStatusService.findById(taxReturn.getFilingStatus().getValue());
      EducationTaxCreditAotc educationTaxCreditAotc = filingStatus.getEducationTaxCreditAotc();

      // Get relevant variables
      int numDependents = taxReturnCredit.getNumDependentsAotc();
      BigDecimal educationExpenses = taxReturnCredit.getEducationExpenses();

      // If no dependents qualify for aotc, or no education expenses, simply return
      if (numDependents <= 0 || educationExpenses.compareTo(BigDecimal.ZERO) <= 0) {
        return taxReturn;
      }

      // Get user's agi
      BigDecimal agi = taxReturn.getAdjustedGrossIncome();

      // Rate at which credit amount is reduced by; default to 1.00
      BigDecimal rate = BigDecimal.valueOf(1);

      // Determine the rate at which credit is reduced by based on user's agi
      if (agi.compareTo(BigDecimal.valueOf(educationTaxCreditAotc.getFullCreditIncomeThreshold())) > 0) {
        agi = agi.subtract(BigDecimal.valueOf(educationTaxCreditAotc.getFullCreditIncomeThreshold()));
        if (agi.compareTo(BigDecimal.valueOf(educationTaxCreditAotc.getPartialCreditIncomeThreshold())) > 0) {
          return taxReturn;
        } else {
          rate = educationTaxCreditAotc.getIncomePartialCreditRate();
        }
      }

      // if married filing separately: not eligible; return
      if (filingStatus.getStatus().equals("Married filing separately")) return taxReturn;

      // Initialize credit amount
      BigDecimal creditAmount = BigDecimal.ZERO;

      // Calculate education expenses per student
      BigDecimal expensesPerDependent = educationExpenses.divide(BigDecimal.valueOf(numDependents), 2, RoundingMode.HALF_UP);

      // If expenses per student exceeds the threshold where 100% of expenses is applied as credit, reduce the expenses
      if (expensesPerDependent.compareTo(BigDecimal.valueOf(educationTaxCreditAotc.getExpensesThresholdFullCredit())) > 0) {
        // Calculate remaining expenses to reduce as credit
        BigDecimal remainingExpenses = expensesPerDependent.subtract(BigDecimal.valueOf(educationTaxCreditAotc.getExpensesThresholdFullCredit()));

        // Reduce expenses by partial credit rate
        BigDecimal reducedExpenses = remainingExpenses.multiply(educationTaxCreditAotc.getExpensesPartialCreditRate());

        expensesPerDependent = BigDecimal.valueOf(educationTaxCreditAotc.getExpensesThresholdFullCredit()).add(reducedExpenses);
      }

      // Calculate credit amount per dependent
      BigDecimal creditAmountPerDependent = expensesPerDependent.min(BigDecimal.valueOf(educationTaxCreditAotc.getMaxCreditAmountPerStudent()));

      // Calculate total credit amount based on number of dependents
      creditAmount = creditAmountPerDependent.multiply(BigDecimal.valueOf(numDependents));

      // Calculate total credit amount based on income and its associated partial credit rate
      creditAmount = creditAmount.multiply(rate);

      // Add total credit amount to federal refund and total credits
      taxReturn.setTotalCredits(taxReturn.getTotalCredits().add(creditAmount));
      taxReturn.setFederalRefund(taxReturn.getFederalRefund().add(creditAmount));

      return taxReturn;
    }

    // non-refundable
    public TaxReturnDto calculateEducationTaxCreditLlc(TaxReturnDto taxReturn) {
      TaxReturnCreditDto taxReturnCredit = taxReturn.getTaxCredit();

      // Get education expenses
      BigDecimal educationExpenses = taxReturnCredit.getLlcEducationExpenses();

      // if user is not eligible, return
      if (!taxReturnCredit.isClaimLlcCredit() || taxReturnCredit.getLlcEducationExpenses().compareTo(BigDecimal.ZERO) <= 0) {
        return taxReturn;
      }

      // Get tax credit static data based on filing status
      FilingStatus filingStatus = filingStatusService.findById(taxReturn.getFilingStatus().getValue());
      EducationTaxCreditLlc educationTaxCreditLlc = filingStatus.getEducationTaxCreditLlc();

      // Get user's AGI
      BigDecimal agi = taxReturn.getAdjustedGrossIncome();

      // Rate at which credit amount is reduced by; default to 1.00
      BigDecimal rate = BigDecimal.valueOf(1);

      // Determine the rate at which credit is reduced by based on user's agi
      if (agi.compareTo(BigDecimal.valueOf(educationTaxCreditLlc.getFullCreditIncomeThreshold())) > 0) {
        agi = agi.subtract(BigDecimal.valueOf(educationTaxCreditLlc.getFullCreditIncomeThreshold()));
        if (agi.compareTo(BigDecimal.valueOf(educationTaxCreditLlc.getPartialCreditIncomeThreshold())) > 0) {
          return taxReturn;
        } else {
          rate = educationTaxCreditLlc.getIncomePartialCreditRate();
        }
      }
      
      // Bound expenses to the maximum allowed consideration for tax credit
      educationExpenses = educationExpenses.min(BigDecimal.valueOf(educationTaxCreditLlc.getExpensesThreshold()));

      // Calculate the credit amount based on the credit rate
      BigDecimal creditAmount = educationExpenses.multiply(educationTaxCreditLlc.getCreditRate());

      // Calculate the final credit amount based on the limiting rate of agi
      creditAmount = creditAmount.multiply(rate);

      // Calculate the amount needed to bring federalRefund to zero
      BigDecimal neededToZero = taxReturn.getFederalRefund().negate();

      // Determine the actual amount of credit added
      BigDecimal actualAddedCredit = creditAmount.min(neededToZero.max(BigDecimal.ZERO));

      // Add total credit amount to federal refund and total credits
      taxReturn.setTotalCredits(taxReturn.getTotalCredits().add(actualAddedCredit));
      taxReturn.setFederalRefund(taxReturn.getFederalRefund().add(actualAddedCredit));

      return taxReturn;
    }

    // non-refundable
    public TaxReturnDto calculateSaversTaxCredit(TaxReturnDto taxReturn) {
      TaxReturnCreditDto taxReturnCredit = taxReturn.getTaxCredit();

      // Get user's IRA contribution amount
      BigDecimal iraContributions = taxReturnCredit.getIraContributions();

      // if user can be claimed as a dependent on another's tax return or if no ira contributions: not eligible; return
      if (taxReturnCredit.isClaimedAsDependent() || iraContributions.compareTo(BigDecimal.ZERO) <= 0) {
        return taxReturn;
      }

      // Get tax credit static data based on user's filing status
      FilingStatus filingStatus = filingStatusService.findById(taxReturn.getFilingStatus().getValue());
      SaversTaxCredit saversTaxCredit = filingStatus.getSaversTaxCredit();

      // Get user's AGI
      BigDecimal agi = taxReturn.getAdjustedGrossIncome();

      BigDecimal agiFirstThreshold = BigDecimal.valueOf(saversTaxCredit.getAgiThresholdFirstContributionLimit());
      BigDecimal agiSecondThreshold = BigDecimal.valueOf(saversTaxCredit.getAgiThresholdSecondContributionLimit());
      BigDecimal agiThirdThreshold = BigDecimal.valueOf(saversTaxCredit.getAgiThresholdThirdContributionLimit());

      // Determine rate at which IRA contributions are applied as credit
      BigDecimal rate = saversTaxCredit.getFirstContributionRate();
      if (agi.compareTo(agiFirstThreshold) > 0) {
        BigDecimal remainingAgi = agi.subtract(agiFirstThreshold);
        if (remainingAgi.compareTo(agiSecondThreshold) > 0) {
          remainingAgi = remainingAgi.subtract(agiSecondThreshold);
          // If agi exceeds this threshold, 0% of IRA contributions are applied as credit; simply return
          if (remainingAgi.compareTo(agiThirdThreshold) > 0) {
            return taxReturn;
          } else {
            rate = saversTaxCredit.getThirdContributionRate();
          }
        } else {
          rate = saversTaxCredit.getSecondContributionRate();
        }
      }

      // Restrict amount of contributions that are considered for tax credit
      iraContributions = iraContributions.min(BigDecimal.valueOf(saversTaxCredit.getMaxContributionAmount()));

      // Calculate credit amount based on rate
      BigDecimal creditAmount = iraContributions.multiply(rate);

      // Calculate the amount needed to bring federalRefund to zero
      BigDecimal neededToZero = taxReturn.getFederalRefund().negate();

      // Determine the actual amount of credit added
      BigDecimal actualAddedCredit = creditAmount.min(neededToZero.max(BigDecimal.ZERO));

      // Add total credit amount to federal refund and total credits
      taxReturn.setTotalCredits(taxReturn.getTotalCredits().add(actualAddedCredit));
      taxReturn.setFederalRefund(taxReturn.getFederalRefund().add(actualAddedCredit));

      return taxReturn;
    }


    // non-refundable
    public TaxReturnDto calculateDependentCareTaxCredit(TaxReturnDto taxReturn) {
      TaxReturnCreditDto taxReturnCredit = taxReturn.getTaxCredit();

      // Get child care expenses
      BigDecimal childCareExpenses = taxReturnCredit.getChildCareExpenses();

      // Get user's number of qualifying children
      int numChildren = taxReturnCredit.getNumChildren();

      // If no children or no child care expenses, simply return
      if (numChildren <= 0 || childCareExpenses.compareTo(BigDecimal.ZERO) <= 0) {
        return taxReturn;
      }

      // Get tax credit static data
      List<DependentCareTaxCredit> dependentCareTaxCredit = taxCreditService.getDependentCareTaxCreditBrackets();

      DependentCareTaxCreditLimit creditLimit = taxCreditService.getDependentCareTaxCreditLimitByNumDependents(numChildren);

      // Get user's AGI
      double remainingAgi = taxReturn.getAdjustedGrossIncome().doubleValue();

      // Initialize rate at which child care expenses is applied as credit
      BigDecimal rate = dependentCareTaxCredit.get(0).getRate();

      // Based on AGI, determine which credit bracket is applied
      for (DependentCareTaxCredit credit : dependentCareTaxCredit) {
        if (remainingAgi > credit.getIncomeRange()) {
          remainingAgi -= credit.getIncomeRange();
          rate = credit.getRate();
        } else {
          rate = credit.getRate();
          break;
        }
      }

      // Calculate credit amount based on rate
      BigDecimal creditAmount = childCareExpenses.multiply(rate);

      // Restrict creditAmount based on credit limit
      creditAmount = creditAmount.min(BigDecimal.valueOf(creditLimit.getCreditLimit()));

      // Calculate the amount needed to bring federalRefund to zero
      BigDecimal neededToZero = taxReturn.getFederalRefund().negate();

      // Determine the actual amount of credit added
      BigDecimal actualAddedCredit = creditAmount.min(neededToZero.max(BigDecimal.ZERO));

      // Add credits to federal refund and total credits
      taxReturn.setTotalCredits(taxReturn.getTotalCredits().add(actualAddedCredit));
      taxReturn.setFederalRefund(taxReturn.getFederalRefund().add(actualAddedCredit));

      return taxReturn;
    }
}


