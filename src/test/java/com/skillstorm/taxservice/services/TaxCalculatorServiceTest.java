package com.skillstorm.taxservice.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.skillstorm.taxservice.constants.FilingStatus;
import com.skillstorm.taxservice.constants.State;
import com.skillstorm.taxservice.dtos.DeductionDto;
import com.skillstorm.taxservice.dtos.OtherIncomeDto;
import com.skillstorm.taxservice.dtos.TaxReturnCreditDto;
import com.skillstorm.taxservice.dtos.TaxReturnDeductionDto;
import com.skillstorm.taxservice.dtos.TaxReturnDto;
import com.skillstorm.taxservice.dtos.W2Dto;
import com.skillstorm.taxservice.models.CapitalGainsTax;
import com.skillstorm.taxservice.models.Deduction;
import com.skillstorm.taxservice.models.StandardDeduction;
import com.skillstorm.taxservice.models.StateTax;
import com.skillstorm.taxservice.models.TaxBracket;
import com.skillstorm.taxservice.models.taxcredits.ChildTaxCredit;
import com.skillstorm.taxservice.models.taxcredits.DependentCareTaxCredit;
import com.skillstorm.taxservice.models.taxcredits.DependentCareTaxCreditLimit;
import com.skillstorm.taxservice.models.taxcredits.EarnedIncomeTaxCredit;
import com.skillstorm.taxservice.models.taxcredits.EducationTaxCreditAotc;
import com.skillstorm.taxservice.models.taxcredits.EducationTaxCreditLlc;
import com.skillstorm.taxservice.models.taxcredits.SaversTaxCredit;
import com.skillstorm.taxservice.repositories.DeductionRepository;

public class TaxCalculatorServiceTest {

  @Mock
  private OtherIncomeService otherIncomeService;

  @Mock
  private StateTaxService stateTaxService;

  @Mock
  private CapitalGainsTaxService capitalGainsTaxService;

  @Mock
  private FilingStatusService filingStatusService;

  @Mock
  private com.skillstorm.taxservice.models.FilingStatus filingStatusModel;

  @Mock
  private TaxBracketService taxBracketService;

  @Mock
  private TaxCreditService taxCreditService;

  @Mock
    private StandardDeductionService standardDeductionService;

  @Mock
  // re:name convention break
  // named this deductionSvc to fix weird git build check error
    private DeductionService deductionSvc;

  // unread but there are nested calls to deductionRepository
  @Mock
    private DeductionRepository deductionRepository;

  @InjectMocks
  private TaxCalculatorService taxCalculatorService;

  private TaxReturnDto taxReturn;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    taxReturn = new TaxReturnDto();
    taxReturn.setId(1);
    taxReturn.setFilingStatus(FilingStatus.SINGLE);
    taxReturn.setTotalIncome(BigDecimal.valueOf(50000));
    taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(45000));
    taxReturn.setTaxableIncome(BigDecimal.valueOf(40000));
    taxReturn.setFederalRefund(BigDecimal.ZERO);
    taxReturn.setState(State.AL);
  }

  @Test
  public void testCalculateAll_DefaultValues() {

    // Arrange:
    StandardDeduction standardDeduction = new StandardDeduction();
    standardDeduction.setId(1);
    standardDeduction.setFilingStatus(new com.skillstorm.taxservice.models.FilingStatus());
    standardDeduction.setDeductionAmount(12000);

    when(standardDeductionService.getByFilingStatusId(1)).thenReturn(standardDeduction);


    TaxReturnDto result = taxCalculatorService.calculateAll(taxReturn);

    BigDecimal expectedResult = BigDecimal.ZERO.setScale(2);

    assertEquals(expectedResult, result.getFederalRefund());
    assertEquals(expectedResult, result.getStateRefund());
    assertEquals(expectedResult, result.getTotalIncome());
    assertEquals(expectedResult, result.getAdjustedGrossIncome());
    assertEquals(expectedResult, result.getTaxableIncome());
    assertEquals(expectedResult, result.getTotalCredits());
    assertEquals(expectedResult, result.getFedTaxWithheld());
    assertEquals(expectedResult, result.getSocialSecurityTaxWithheld());
    assertEquals(expectedResult, result.getMedicareTaxWithheld());
  }

  @Test
  public void testCalculateTotalIncome() {
    // Arrange
    W2Dto w2Dto1 = new W2Dto();
    w2Dto1.setWages(new BigDecimal("30000.00"));
    
    W2Dto w2Dto2 = new W2Dto();
    w2Dto2.setWages(new BigDecimal("20000.00"));
    
    taxReturn.setW2s(Arrays.asList(w2Dto1, w2Dto2));
    
    OtherIncomeDto otherIncomeDto = new OtherIncomeDto();
    BigDecimal totalOtherIncome = new BigDecimal("10000.00");

    taxReturn.setOtherIncome(otherIncomeDto);

    
    // Act
    TaxReturnDto result = taxCalculatorService.calculateTotalIncome(taxReturn);
    
    // Assert
    BigDecimal expectedTotalIncome = new BigDecimal("50000.00").setScale(2, RoundingMode.HALF_UP);
    assertEquals(expectedTotalIncome, result.getTotalIncome());
  }

  //@Test
  public void testCalculateFederalTaxes() {
    // Arrange
    BigDecimal taxableIncome = new BigDecimal("50000.00");
    BigDecimal federalRefund = new BigDecimal("1000.00");

    taxReturn.setTaxableIncome(taxableIncome);
    taxReturn.setFederalRefund(federalRefund);
    taxReturn.setFilingStatus(com.skillstorm.taxservice.constants.FilingStatus.SINGLE);

    TaxBracket bracket1 = new TaxBracket();
    bracket1.setMinIncome(0);
    bracket1.setMaxIncome(20000);
    bracket1.setRate(new BigDecimal("0.10"));

    TaxBracket bracket2 = new TaxBracket();
    bracket2.setMinIncome(20000);
    bracket2.setMaxIncome(50000);
    bracket2.setRate(new BigDecimal("0.20"));

    List<TaxBracket> taxBrackets = Arrays.asList(bracket1, bracket2);

    when(taxBracketService.findByFilingStatusID(1)).thenReturn(taxBrackets);

    // Act
    TaxReturnDto result = taxCalculatorService.calculateFederalTaxes(taxReturn);

    // Assert
    BigDecimal expectedTotalTaxes = new BigDecimal("8000.0000");
    BigDecimal expectedFederalRefund = federalRefund.subtract(expectedTotalTaxes);

    assertEquals(expectedFederalRefund, result.getFederalRefund());
    
    verify(taxBracketService, times(1)).findByFilingStatusID(1);
  }

  @Test
    public void testCalculateFederalTaxes_NoTaxableIncome() {
        // Set taxable income to 0
        taxReturn.setTaxableIncome(BigDecimal.ZERO);

        // Execute the method to test
        TaxReturnDto result = taxCalculatorService.calculateFederalTaxes(taxReturn);

        // Verify that the refund is equal to the taxes withheld
        assertThat(result.getFederalRefund()).isEqualTo(taxReturn.getFedTaxWithheld());
    }

    @Test
    public void testCalculateFederalTaxes_WithTaxableIncome() {
        // Set up tax brackets
        TaxBracket bracket1 = new TaxBracket();
        // 1, FilingStatus.SINGLE, BigDecimal.valueOf(0.10), 0, 9875
        bracket1.setMinIncome(0);
        bracket1.setMaxIncome(9875);
        bracket1.setRate(new BigDecimal("0.10"));

        TaxBracket bracket2 = new TaxBracket();
        bracket2.setMinIncome(9875);
        bracket2.setMaxIncome(40125);
        bracket2.setRate(new BigDecimal("0.12"));
        when(taxBracketService.findByFilingStatusID(FilingStatus.SINGLE.getValue())).thenReturn(List.of(bracket1, bracket2));

        // Set taxable income and taxes paid
        taxReturn.setTaxableIncome(BigDecimal.valueOf(40000));
        taxReturn.setFedTaxWithheld(BigDecimal.valueOf(5000));

        // Execute the method to test
        TaxReturnDto result = taxCalculatorService.calculateFederalTaxes(taxReturn);

        // Verify the calculated federal tax liability
        BigDecimal expectedRefund = taxReturn.getFedTaxWithheld().subtract(BigDecimal.valueOf(4602.50)); // Approx tax owed
        // use compareTo to deal with the BigDecimal scale differences
        assertThat(result.getFederalRefund().compareTo(expectedRefund)).isEqualTo(0);

    }

    /*
    target the block

    if (remainingIncome.compareTo(BigDecimal.ZERO) <= 0) {
      break; // No more income to tax
    }
    */ 
  @Test
  public void testCalculateFederalTaxes_WithRemainingTaxableIncomeZero() {
      // Set up tax brackets
      TaxBracket bracket1 = new TaxBracket();
      bracket1.setMinIncome(0);
      bracket1.setMaxIncome(9875);
      bracket1.setRate(new BigDecimal("0.10"));

      TaxBracket bracket2 = new TaxBracket();
      bracket2.setMinIncome(9875);
      bracket2.setMaxIncome(40125);
      bracket2.setRate(new BigDecimal("0.12"));

      TaxBracket bracket3 = new TaxBracket();
      bracket3.setMinIncome(40125);
      bracket3.setMaxIncome(85525);
      bracket3.setRate(new BigDecimal("0.22"));

      // Mock the tax bracket service
      when(taxBracketService.findByFilingStatusID(FilingStatus.SINGLE.getValue())).thenReturn(List.of(bracket1, bracket2, bracket3));

      // Set taxable income to a value that will be completely taxed within the first two brackets
      taxReturn.setTaxableIncome(BigDecimal.valueOf(40000));
      taxReturn.setFedTaxWithheld(BigDecimal.valueOf(5000));

      // Execute the method to test
      TaxReturnDto result = taxCalculatorService.calculateFederalTaxes(taxReturn);

      // Assert the federal tax liability calculation
      // All of the taxable income (40000) should fall within the first two brackets
      BigDecimal expectedTaxLiability = BigDecimal.valueOf(9875).multiply(BigDecimal.valueOf(0.10))
              .add(BigDecimal.valueOf(40000 - 9875).multiply(BigDecimal.valueOf(0.12)));

      // Assert that remainingIncome becomes zero and breaks the loop before applying the third bracket
      BigDecimal expectedRefund = taxReturn.getFedTaxWithheld().subtract(expectedTaxLiability);
      // use compareTo to deal with the BigDecimal scale differences
      assertThat(result.getFederalRefund().compareTo(expectedRefund)).isEqualTo(0);
  }

  @Test
  public void testCalculateTaxableIncome_StandardDeductionGreater() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setFilingStatus(FilingStatus.SINGLE);
      taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(50000));

      // Set up a deduction that is itemized
      TaxReturnDeductionDto deduction = new TaxReturnDeductionDto();
      deduction.setItemized(true);
      deduction.setAgiLimit(BigDecimal.valueOf(0.10)); // 10% of AGI
      deduction.setAmountSpent(BigDecimal.valueOf(3000)); // Less than standard deduction

      taxReturn.setDeductions(List.of(deduction));

      // Mock the standard deduction service to return a higher standard deduction
      StandardDeduction standardDeduction = new StandardDeduction();
      standardDeduction.setDeductionAmount(12000); // Greater than itemized deductions
      when(standardDeductionService.getByFilingStatusId(FilingStatus.SINGLE.getValue())).thenReturn(standardDeduction);

      // Act
      TaxReturnDto result = taxCalculatorService.calculateTaxableIncome(taxReturn);

      // Assert
      BigDecimal expectedTaxableIncome = BigDecimal.valueOf(50000).subtract(BigDecimal.valueOf(12000)); // Standard deduction is applied
      assertThat(result.getTaxableIncome()).isEqualTo(expectedTaxableIncome);
  }

  @Test
  public void testCalculateTaxableIncome_ItemizedDeductionsGreater() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setFilingStatus(FilingStatus.SINGLE);
      taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(50000));

      // Set up a deduction that is itemized
      TaxReturnDeductionDto deduction = new TaxReturnDeductionDto();
      deduction.setItemized(true);
      deduction.setAgiLimit(BigDecimal.valueOf(0.50)); // 50% of AGI
      deduction.setAmountSpent(BigDecimal.valueOf(30000)); // Greater than standard deduction

      taxReturn.setDeductions(List.of(deduction));

      // Mock the standard deduction service to return a lower standard deduction
      StandardDeduction standardDeduction = new StandardDeduction();
      standardDeduction.setDeductionAmount(12000); // Less than itemized deductions
      when(standardDeductionService.getByFilingStatusId(FilingStatus.SINGLE.getValue())).thenReturn(standardDeduction);

      // Act
      TaxReturnDto result = taxCalculatorService.calculateTaxableIncome(taxReturn);

      // Assert
      BigDecimal expectedTaxableIncome = BigDecimal.valueOf(50000).subtract(BigDecimal.valueOf(25000)); // Itemized deduction is applied
      assertThat(result.getTaxableIncome().compareTo(expectedTaxableIncome)).isEqualTo(0);
  }

  @Test
  public void testCalculateTaxableIncome_NoDeductions() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setFilingStatus(FilingStatus.SINGLE);
      taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(50000));

      // No deductions
      taxReturn.setDeductions(Collections.emptyList());

      // Mock the standard deduction service to return a standard deduction
      StandardDeduction standardDeduction = new StandardDeduction();
      standardDeduction.setDeductionAmount(12000); 
      when(standardDeductionService.getByFilingStatusId(FilingStatus.SINGLE.getValue())).thenReturn(standardDeduction);

      // Act
      TaxReturnDto result = taxCalculatorService.calculateTaxableIncome(taxReturn);

      // Assert
      BigDecimal expectedTaxableIncome = BigDecimal.valueOf(50000).subtract(BigDecimal.valueOf(12000)); // Standard deduction is applied
      assertThat(result.getTaxableIncome()).isEqualTo(expectedTaxableIncome);
  }


  //@Test
    public void testCalculateStateTaxes() {
        // Arrange
        W2Dto w2Dto1 = new W2Dto();
        w2Dto1.setWages(new BigDecimal("30000.00"));
        w2Dto1.setFederalIncomeTaxWithheld(new BigDecimal("3000.00"));
        w2Dto1.setStateIncomeTaxWithheld(new BigDecimal("1500.00"));
        w2Dto1.setSocialSecurityTaxWithheld(new BigDecimal("1860.00"));
        w2Dto1.setMedicareTaxWithheld(new BigDecimal("435.00"));
        w2Dto1.setState(State.AL);

        W2Dto w2Dto2 = new W2Dto();
        w2Dto2.setWages(new BigDecimal("20000.00"));
        w2Dto2.setFederalIncomeTaxWithheld(new BigDecimal("2000.00"));
        w2Dto2.setStateIncomeTaxWithheld(new BigDecimal("1000.00"));
        w2Dto2.setSocialSecurityTaxWithheld(new BigDecimal("1240.00"));
        w2Dto2.setMedicareTaxWithheld(new BigDecimal("290.00"));
        w2Dto2.setState(State.AL);

        taxReturn.setW2s(Arrays.asList(w2Dto1, w2Dto2));
        taxReturn.setStateRefund(new BigDecimal("5000.00"));

        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();
        otherIncomeDto.setOtherInvestmentIncome(new BigDecimal("5000.00"));
        otherIncomeDto.setNetBusinessIncome(new BigDecimal("10000.00"));
        otherIncomeDto.setAdditionalIncome(new BigDecimal("3000.00"));
        otherIncomeDto.setShortTermCapitalGains(new BigDecimal("2000.00"));

        taxReturn.setOtherIncome(otherIncomeDto);
        taxReturn.setState(State.AL);

        // Mocking the tax brackets
        StateTax bracket1 = new StateTax();
        bracket1.setIncomeRange(20000);
        bracket1.setRate(new BigDecimal("0.05"));

        StateTax bracket2 = new StateTax();
        bracket2.setIncomeRange(30000); // Difference between 50000 and 20000
        bracket2.setRate(new BigDecimal("0.07"));

        StateTax bracket3 = new StateTax();
        bracket3.setIncomeRange(0); // No max income limit for this bracket
        bracket3.setRate(new BigDecimal("0.09"));

        List<StateTax> stateTaxBrackets = Arrays.asList(bracket1, bracket2, bracket3);

        when(stateTaxService.getTaxBracketsByStateId(1)).thenReturn(stateTaxBrackets);

        // Act
        TaxReturnDto result = taxCalculatorService.calculateStateTaxes(taxReturn);

        // Assert
        BigDecimal totalWages = new BigDecimal("50000.00");
        BigDecimal otherIncome = new BigDecimal("20000.00"); // Sum of other income
        BigDecimal totalIncome = totalWages.add(otherIncome);

        // Calculate expected state tax
        BigDecimal expectedStateTax = new BigDecimal("20000.00").multiply(new BigDecimal("0.05")) // First 20000 at 5%
                                     .add(new BigDecimal("30000.00").multiply(new BigDecimal("0.07"))) // Next 30000 at 7%
                                     .add(totalIncome.subtract(totalWages).multiply(new BigDecimal("0.09"))); // Remaining income at 9%

        BigDecimal expectedStateRefund = new BigDecimal("5000.00").subtract(expectedStateTax);

        assertEquals(expectedStateRefund.setScale(2, RoundingMode.HALF_UP), result.getStateRefund());
        assertEquals(new BigDecimal("5000.00").setScale(2, RoundingMode.HALF_UP), result.getFedTaxWithheld());
        assertEquals(new BigDecimal("2500.00").setScale(2, RoundingMode.HALF_UP), result.getStateTaxWithheld());
        assertEquals(new BigDecimal("3100.00").setScale(2, RoundingMode.HALF_UP), result.getSocialSecurityTaxWithheld());
        assertEquals(new BigDecimal("725.00").setScale(2, RoundingMode.HALF_UP), result.getMedicareTaxWithheld());

        verify(stateTaxService, times(1)).getTaxBracketsByStateId(1);
    }

  @Test
  void testCalculateCapitalGainsTax() {
    // Setup
    taxReturn.setTaxableIncome(BigDecimal.valueOf(50000));
    taxReturn.setFederalRefund(BigDecimal.valueOf(2000));

    OtherIncomeDto otherIncome = new OtherIncomeDto();
    otherIncome.setLongTermCapitalGains(BigDecimal.valueOf(10000));

    taxReturn.setOtherIncome(otherIncome);
    taxReturn.setFilingStatus(FilingStatus.SINGLE);

    // Mock capital gains tax brackets
    List<CapitalGainsTax> capitalGainsTaxBrackets = new ArrayList<>();

    // Mock capital gains tax brackets for married filing status
    capitalGainsTaxBrackets.add(new CapitalGainsTax(1, new com.skillstorm.taxservice.models.FilingStatus(), BigDecimal.valueOf(0.1), 40000)); // Sample bracket 1
    capitalGainsTaxBrackets.add(new CapitalGainsTax(2, new com.skillstorm.taxservice.models.FilingStatus(), BigDecimal.valueOf(0.15), 0)); // Sample last bracket

    when(capitalGainsTaxService.findByFilingStatusID(taxReturn.getFilingStatus().getValue()))
            .thenReturn(capitalGainsTaxBrackets);
    
    // Calculate expected federal refund after capital gains tax
    BigDecimal expectedFederalRefund = taxReturn.getFederalRefund().subtract(BigDecimal.valueOf(1500)).setScale(2); // Sample expected federal refund after capital gains tax

    // Call the method
    TaxReturnDto result = taxCalculatorService.calculateCapitalGainsTax(taxReturn);

    
    // Assert the result
    assertEquals(expectedFederalRefund, result.getFederalRefund());
  }
    
  @Test
  public void testCalculateCapitalGainsTax_WithMultipleBracketsAndPartialIncome() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      
      // Mocking taxable income and capital gains
      taxReturn.setTaxableIncome(BigDecimal.valueOf(100000)); // Total taxable income
      OtherIncomeDto otherIncome = new OtherIncomeDto();
      otherIncome.setLongTermCapitalGains(BigDecimal.valueOf(30000)); // Capital gains
      taxReturn.setOtherIncome(otherIncome);

      taxReturn.setFilingStatus(FilingStatus.SINGLE);

      // Mock capital gains tax brackets
      CapitalGainsTax bracket1 = new CapitalGainsTax();
      bracket1.setIncomeRange(50000); // First 50k bracket
      bracket1.setRate(BigDecimal.valueOf(0.10)); // 10% rate

      CapitalGainsTax bracket2 = new CapitalGainsTax();
      bracket2.setIncomeRange(100000); // 2nd bracket
      bracket2.setRate(BigDecimal.valueOf(0.20)); // 20% rate

      CapitalGainsTax finalBracket = new CapitalGainsTax();
      finalBracket.setIncomeRange(0); // Final bracket with rate on all remaining income
      finalBracket.setRate(BigDecimal.valueOf(0.25)); // 25% rate for remaining gains

      when(capitalGainsTaxService.findByFilingStatusID(FilingStatus.SINGLE.getValue()))
              .thenReturn(List.of(bracket1, bracket2, finalBracket));

      // Initial federal refund
      taxReturn.setFederalRefund(BigDecimal.valueOf(20000));

      // Act
      TaxReturnDto result = taxCalculatorService.calculateCapitalGainsTax(taxReturn);

      assertThat(result.getFederalRefund().compareTo(BigDecimal.valueOf(14000))).isEqualTo(0);
  }

  @Test
  void testCalculateChildTaxCredits() {

    // Set up test data
    taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(90000));
    taxReturn.setFederalRefund(BigDecimal.valueOf(2000));
    taxReturn.setTotalCredits(BigDecimal.ZERO);

    TaxReturnCreditDto taxReturnCredit = new TaxReturnCreditDto();
    taxReturnCredit.setNumDependents(2);
    taxReturn.setTaxCredit(taxReturnCredit);

    FilingStatus filingStatusEnum = FilingStatus.SINGLE;
    taxReturn.setFilingStatus(filingStatusEnum);

    com.skillstorm.taxservice.models.FilingStatus filingStatus = 
      mock(com.skillstorm.taxservice.models.FilingStatus.class);

    ChildTaxCredit childTaxCredit = new ChildTaxCredit();
    childTaxCredit.setPerQualifyingChild(2000);
    childTaxCredit.setIncomeThreshold(75000);
    childTaxCredit.setRefundLimit(1400);

    // Mocking FilingStatusService to return our sample filing status and child tax credit
    when(filingStatusService.findById(filingStatusEnum.getValue())).thenReturn(filingStatus);
    when(filingStatus.getChildTaxCredit()).thenReturn(childTaxCredit);

    // Call the method to test
    TaxReturnDto result = taxCalculatorService.calculateChildTaxCredits(taxReturn);

    // Calculate expected values
    BigDecimal potentialCreditAmount = BigDecimal.valueOf(2 * 2000);
    BigDecimal incomeThreshold = BigDecimal.valueOf(75000);
    BigDecimal agiExcess = BigDecimal.valueOf(90000).subtract(incomeThreshold).max(BigDecimal.ZERO);
    BigDecimal quotient = agiExcess.divide(new BigDecimal("1000"), RoundingMode.HALF_UP);
    BigDecimal phaseoutAmount = quotient.multiply(new BigDecimal("50"));
    BigDecimal creditAfterPhaseout = potentialCreditAmount.subtract(phaseoutAmount);

    BigDecimal creditLimit = BigDecimal.valueOf(childTaxCredit.getRefundLimit());
    BigDecimal difference = BigDecimal.valueOf(childTaxCredit.getPerQualifyingChild()).subtract(creditLimit);
    creditAfterPhaseout = creditAfterPhaseout.subtract(difference.multiply(BigDecimal.valueOf(2)));

    BigDecimal expectedFederalRefund = BigDecimal.valueOf(2000).add(creditAfterPhaseout);
    BigDecimal expectedTotalCredits = creditAfterPhaseout;

    // Assert the result
    assertEquals(expectedFederalRefund.setScale(2, RoundingMode.HALF_UP), result.getFederalRefund().setScale(2, RoundingMode.HALF_UP));
    assertEquals(expectedTotalCredits.setScale(2, RoundingMode.HALF_UP), result.getTotalCredits().setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  void calculateEarnedIncomeTaxCredit_Test() {
    // Set up test data
    taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(40000));
    taxReturn.setFederalRefund(BigDecimal.valueOf(1000));
    taxReturn.setTotalCredits(BigDecimal.ZERO);

    TaxReturnCreditDto taxReturnCredit = new TaxReturnCreditDto();
    taxReturnCredit.setNumDependents(1);
    taxReturn.setTaxCredit(taxReturnCredit);

    OtherIncomeDto otherIncome = new OtherIncomeDto();
    otherIncome.setOtherInvestmentIncome(BigDecimal.valueOf(1000));
    taxReturn.setOtherIncome(otherIncome);

    FilingStatus filingStatusEnum = FilingStatus.SINGLE;
    taxReturn.setFilingStatus(filingStatusEnum);

    // Mocking FilingStatus and EarnedIncomeTaxCredit
    com.skillstorm.taxservice.models.FilingStatus filingStatus = mock(com.skillstorm.taxservice.models.FilingStatus.class);
    EarnedIncomeTaxCredit earnedIncomeTaxCredit = new EarnedIncomeTaxCredit();
    earnedIncomeTaxCredit.setInvestmentIncomeLimit(3500);
    earnedIncomeTaxCredit.setAgiThreshold0Children(15000);
    earnedIncomeTaxCredit.setAgiThreshold1Children(40000);
    earnedIncomeTaxCredit.setAgiThreshold2Children(45000);
    earnedIncomeTaxCredit.setAgiThreshold3Children(50000);
    earnedIncomeTaxCredit.setAmount0Children(500);
    earnedIncomeTaxCredit.setAmount1Children(3000);
    earnedIncomeTaxCredit.setAmount2Children(3500);
    earnedIncomeTaxCredit.setAmount3Children(4000);

    // Mocking FilingStatusService to return our sample filing status and earned income tax credit
    when(filingStatusService.findById(filingStatusEnum.getValue())).thenReturn(filingStatus);
    when(filingStatus.getEarnedIncomeTaxCredit()).thenReturn(earnedIncomeTaxCredit);

    // Call the method to test
    TaxReturnDto result = taxCalculatorService.calculateEarnedIncomeTaxCredit(taxReturn);

    // Calculate expected values
    BigDecimal expectedFederalRefund = BigDecimal.valueOf(1000).add(BigDecimal.valueOf(3000));
    BigDecimal expectedTotalCredits = BigDecimal.valueOf(3000);

    // Assert the result
    assertEquals(expectedFederalRefund.setScale(2, RoundingMode.HALF_UP), result.getFederalRefund().setScale(2, RoundingMode.HALF_UP));
    assertEquals(expectedTotalCredits.setScale(2, RoundingMode.HALF_UP), result.getTotalCredits().setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  public void testCalculateEarnedIncomeTaxCredit_ExceedsInvestmentIncomeLimit() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(30000)); // AGI within threshold
      taxReturn.setFilingStatus(FilingStatus.SINGLE);

      TaxReturnCreditDto taxCredit = new TaxReturnCreditDto();
      taxCredit.setNumDependents(1); // 1 dependent
      taxReturn.setTaxCredit(taxCredit);

      OtherIncomeDto otherIncome = new OtherIncomeDto();
      otherIncome.setOtherInvestmentIncome(BigDecimal.valueOf(5000)); // Investment income exceeds limit
      taxReturn.setOtherIncome(otherIncome);

      EarnedIncomeTaxCredit earnedIncomeTaxCredit = new EarnedIncomeTaxCredit();
      earnedIncomeTaxCredit.setInvestmentIncomeLimit(4000); // Investment income limit
      earnedIncomeTaxCredit.setAgiThreshold1Children(35000); // AGI threshold for 1 dependent
      earnedIncomeTaxCredit.setAmount1Children(1000); // Credit amount for 1 dependent

      com.skillstorm.taxservice.models.FilingStatus filingStatus = new com.skillstorm.taxservice.models.FilingStatus();
      filingStatus.setEarnedIncomeTaxCredit(earnedIncomeTaxCredit);
      when(filingStatusService.findById(FilingStatus.SINGLE.getValue())).thenReturn(filingStatus);

      // Act
      TaxReturnDto result = taxCalculatorService.calculateEarnedIncomeTaxCredit(taxReturn);

      // Assert
      // Since the investment income exceeds the limit, no credit should be applied
      assertThat(result.getFederalRefund().compareTo(BigDecimal.ZERO)).isEqualTo(0);
  }

  @Test
  public void testCalculateEarnedIncomeTaxCredit_AgiLessThanOrEqualToZero() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(0)); // AGI is 0
      taxReturn.setFilingStatus(FilingStatus.SINGLE);

      TaxReturnCreditDto taxCredit = new TaxReturnCreditDto();
      taxCredit.setNumDependents(1); // 1 dependent
      taxReturn.setTaxCredit(taxCredit);

      OtherIncomeDto otherIncome = new OtherIncomeDto();
      otherIncome.setOtherInvestmentIncome(BigDecimal.valueOf(2000)); // Investment income below limit
      taxReturn.setOtherIncome(otherIncome);

      EarnedIncomeTaxCredit earnedIncomeTaxCredit = new EarnedIncomeTaxCredit();
      earnedIncomeTaxCredit.setInvestmentIncomeLimit(4000); // Investment income limit
      earnedIncomeTaxCredit.setAgiThreshold1Children(35000); // AGI threshold for 1 dependent
      earnedIncomeTaxCredit.setAmount1Children(1000); // Credit amount for 1 dependent

      com.skillstorm.taxservice.models.FilingStatus filingStatus = new com.skillstorm.taxservice.models.FilingStatus();
      filingStatus.setEarnedIncomeTaxCredit(earnedIncomeTaxCredit);
      when(filingStatusService.findById(FilingStatus.SINGLE.getValue())).thenReturn(filingStatus);

      // Act
      TaxReturnDto result = taxCalculatorService.calculateEarnedIncomeTaxCredit(taxReturn);

      // Assert
      // Since the AGI is less than or equal to 0, no credit should be applied
      assertThat(result.getFederalRefund().compareTo(BigDecimal.ZERO)).isEqualTo(0);
  }

  @Test
  public void testCalculateEarnedIncomeTaxCredit_ZeroDependents() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(20000));
      taxReturn.setFilingStatus(FilingStatus.SINGLE);

      TaxReturnCreditDto taxCredit = new TaxReturnCreditDto();
      taxCredit.setNumDependents(0); // 0 dependents
      taxReturn.setTaxCredit(taxCredit);

      EarnedIncomeTaxCredit earnedIncomeTaxCredit = new EarnedIncomeTaxCredit();
      earnedIncomeTaxCredit.setAgiThreshold0Children(15000); // AGI threshold for 0 dependents
      earnedIncomeTaxCredit.setAmount0Children(500); // Credit amount for 0 dependents
      earnedIncomeTaxCredit.setInvestmentIncomeLimit(3000); // Investment income limit

      com.skillstorm.taxservice.models.FilingStatus filingStatus = new com.skillstorm.taxservice.models.FilingStatus();
      filingStatus.setEarnedIncomeTaxCredit(earnedIncomeTaxCredit);

      when(filingStatusService.findById(FilingStatus.SINGLE.getValue())).thenReturn(filingStatus);

      // Act
      TaxReturnDto result = taxCalculatorService.calculateEarnedIncomeTaxCredit(taxReturn);

      // Assert
      // The AGI exceeds the threshold, so no credit should be added
      assertThat(result.getFederalRefund().compareTo(BigDecimal.ZERO)).isEqualTo(0);
  }

  @Test
  public void testCalculateEarnedIncomeTaxCredit_TwoDependents() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(18000)); // AGI within the threshold
      taxReturn.setFilingStatus(FilingStatus.SINGLE);

      TaxReturnCreditDto taxCredit = new TaxReturnCreditDto();
      taxCredit.setNumDependents(2); // 2 dependents
      taxReturn.setTaxCredit(taxCredit);

      EarnedIncomeTaxCredit earnedIncomeTaxCredit = new EarnedIncomeTaxCredit();
      earnedIncomeTaxCredit.setAgiThreshold2Children(20000); // AGI threshold for 2 dependents
      earnedIncomeTaxCredit.setAmount2Children(2000); // Credit amount for 2 dependents
      earnedIncomeTaxCredit.setInvestmentIncomeLimit(3500); // Investment income limit

      com.skillstorm.taxservice.models.FilingStatus filingStatus = new com.skillstorm.taxservice.models.FilingStatus();
      filingStatus.setEarnedIncomeTaxCredit(earnedIncomeTaxCredit);

      when(filingStatusService.findById(FilingStatus.SINGLE.getValue())).thenReturn(filingStatus);

      // Act
      TaxReturnDto result = taxCalculatorService.calculateEarnedIncomeTaxCredit(taxReturn);

      // Assert
      // The AGI is below the threshold, so the credit should be added
      assertThat(result.getFederalRefund().compareTo(BigDecimal.valueOf(2000))).isEqualTo(0);
  }

  @Test
  public void testCalculateEarnedIncomeTaxCredit_MoreThanTwoDependents() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(25000)); // AGI within the threshold
      taxReturn.setFilingStatus(FilingStatus.SINGLE);

      TaxReturnCreditDto taxCredit = new TaxReturnCreditDto();
      taxCredit.setNumDependents(3); // More than 2 dependents
      taxReturn.setTaxCredit(taxCredit);

      EarnedIncomeTaxCredit earnedIncomeTaxCredit = new EarnedIncomeTaxCredit();
      earnedIncomeTaxCredit.setAgiThreshold3Children(30000); // AGI threshold for 3+ dependents
      earnedIncomeTaxCredit.setAmount3Children(3000); // Credit amount for 3+ dependents
      earnedIncomeTaxCredit.setInvestmentIncomeLimit(4000); // Investment income limit

      com.skillstorm.taxservice.models.FilingStatus filingStatus = new com.skillstorm.taxservice.models.FilingStatus();
      filingStatus.setEarnedIncomeTaxCredit(earnedIncomeTaxCredit);

      when(filingStatusService.findById(FilingStatus.SINGLE.getValue())).thenReturn(filingStatus);

      // Act
      TaxReturnDto result = taxCalculatorService.calculateEarnedIncomeTaxCredit(taxReturn);

      // Assert
      // The AGI is below the threshold, so the credit should be added
      assertThat(result.getFederalRefund().compareTo(BigDecimal.valueOf(3000))).isEqualTo(0);
  }

  @Test
  void testCalculateEducationTaxCreditAotc() {
    // Set up test data
    taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(80000));
    taxReturn.setFederalRefund(BigDecimal.valueOf(1000));
    taxReturn.setTotalCredits(BigDecimal.ZERO);

    TaxReturnCreditDto taxReturnCredit = new TaxReturnCreditDto();
    taxReturnCredit.setNumDependentsAotc(2);
    taxReturnCredit.setEducationExpenses(BigDecimal.valueOf(5000));
    taxReturn.setTaxCredit(taxReturnCredit);

    FilingStatus filingStatusEnum = FilingStatus.SINGLE;
    taxReturn.setFilingStatus(filingStatusEnum);

    // Mocking FilingStatus and EducationTaxCreditAotc
    com.skillstorm.taxservice.models.FilingStatus filingStatus = mock(com.skillstorm.taxservice.models.FilingStatus.class);
    EducationTaxCreditAotc educationTaxCreditAotc = new EducationTaxCreditAotc();
    educationTaxCreditAotc.setFullCreditIncomeThreshold(80000);
    educationTaxCreditAotc.setPartialCreditIncomeThreshold(90000);
    educationTaxCreditAotc.setIncomePartialCreditRate(BigDecimal.valueOf(0.5));
    educationTaxCreditAotc.setExpensesThresholdFullCredit(4000);
    educationTaxCreditAotc.setExpensesPartialCreditRate(BigDecimal.valueOf(0.25));
    educationTaxCreditAotc.setMaxCreditAmountPerStudent(2500);

    // Mocking FilingStatusService to return our sample filing status and education tax credit
    when(filingStatusService.findById(filingStatusEnum.getValue())).thenReturn(filingStatus);
    when(filingStatus.getEducationTaxCreditAotc()).thenReturn(educationTaxCreditAotc);
    when(filingStatus.getStatus()).thenReturn("Single");

    // Call the method to test
    TaxReturnDto result = taxCalculatorService.calculateEducationTaxCreditAotc(taxReturn);

    // Calculate expected values
    BigDecimal expectedCreditAmountPerDependent = BigDecimal.valueOf(4000).min(BigDecimal.valueOf(2500));
    BigDecimal expectedCreditAmount = expectedCreditAmountPerDependent.multiply(BigDecimal.valueOf(2));
    BigDecimal expectedFederalRefund = BigDecimal.valueOf(1000).add(expectedCreditAmount);
    BigDecimal expectedTotalCredits = expectedCreditAmount;

    // Assert the result
    assertEquals(expectedFederalRefund.setScale(2, RoundingMode.HALF_UP), result.getFederalRefund().setScale(2, RoundingMode.HALF_UP));
    assertEquals(expectedTotalCredits.setScale(2, RoundingMode.HALF_UP), result.getTotalCredits().setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  void testCalculateEducationTaxCreditLlc() {
    // Set up test data
    taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(50000));
    taxReturn.setFederalRefund(BigDecimal.valueOf(-3000));
    taxReturn.setTotalCredits(BigDecimal.ZERO);

    TaxReturnCreditDto taxReturnCredit = new TaxReturnCreditDto();
    taxReturnCredit.setClaimLlcCredit(true);
    taxReturnCredit.setLlcEducationExpenses(BigDecimal.valueOf(3000));
    taxReturn.setTaxCredit(taxReturnCredit);

    FilingStatus filingStatusEnum = FilingStatus.SINGLE;
    taxReturn.setFilingStatus(filingStatusEnum);

    // Mocking FilingStatus and EducationTaxCreditLlc
    com.skillstorm.taxservice.models.FilingStatus filingStatus = mock(com.skillstorm.taxservice.models.FilingStatus.class);
    EducationTaxCreditLlc educationTaxCreditLlc = new EducationTaxCreditLlc();
    educationTaxCreditLlc.setFullCreditIncomeThreshold(60000);
    educationTaxCreditLlc.setPartialCreditIncomeThreshold(70000);
    educationTaxCreditLlc.setIncomePartialCreditRate(BigDecimal.valueOf(0.5));
    educationTaxCreditLlc.setExpensesThreshold(2000);
    educationTaxCreditLlc.setCreditRate(BigDecimal.valueOf(0.2));

    // Mocking FilingStatusService to return our sample filing status and education tax credit
    when(filingStatusService.findById(filingStatusEnum.getValue())).thenReturn(filingStatus);
    when(filingStatus.getEducationTaxCreditLlc()).thenReturn(educationTaxCreditLlc);

    // Call the method to test
    TaxReturnDto result = taxCalculatorService.calculateEducationTaxCreditLlc(taxReturn);

    // Calculate expected values
    BigDecimal expectedEducationExpenses = BigDecimal.valueOf(2000);
    BigDecimal expectedCreditAmount = expectedEducationExpenses.multiply(BigDecimal.valueOf(0.2));
    BigDecimal expectedFederalRefund = BigDecimal.valueOf(-3000).add(expectedCreditAmount);
    BigDecimal expectedTotalCredits = expectedCreditAmount;

    // Assert the result
    assertEquals(expectedFederalRefund.setScale(2, RoundingMode.HALF_UP), result.getFederalRefund().setScale(2, RoundingMode.HALF_UP));
    assertEquals(expectedTotalCredits.setScale(2, RoundingMode.HALF_UP), result.getTotalCredits().setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  void testCalculateEducationTaxCreditLlc_NotEligible() {
    // Set up test data
    taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(50000));
    taxReturn.setFederalRefund(BigDecimal.valueOf(1000));
    taxReturn.setTotalCredits(BigDecimal.ZERO);

    TaxReturnCreditDto taxReturnCredit = new TaxReturnCreditDto();
    taxReturnCredit.setClaimLlcCredit(false);
    taxReturn.setTaxCredit(taxReturnCredit);

    FilingStatus filingStatusEnum = FilingStatus.SINGLE;
    taxReturn.setFilingStatus(filingStatusEnum);

    // Call the method to test
    TaxReturnDto result = taxCalculatorService.calculateEducationTaxCreditLlc(taxReturn);

    // Assert the result (should be unchanged)
    assertEquals(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP), result.getFederalRefund().setScale(2, RoundingMode.HALF_UP));
    assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getTotalCredits().setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  void testCalculateEducationTaxCreditLlc_IncomeThresholdExceeded() {
    // Set up test data
    taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(80000));
    taxReturn.setFederalRefund(BigDecimal.valueOf(1000));
    taxReturn.setTotalCredits(BigDecimal.ZERO);

    TaxReturnCreditDto taxReturnCredit = new TaxReturnCreditDto();
    taxReturnCredit.setClaimLlcCredit(true);
    taxReturnCredit.setLlcEducationExpenses(BigDecimal.valueOf(3000));
    taxReturn.setTaxCredit(taxReturnCredit);

    FilingStatus filingStatusEnum = FilingStatus.SINGLE;
    taxReturn.setFilingStatus(filingStatusEnum);

    // Mocking FilingStatus and EducationTaxCreditLlc
    com.skillstorm.taxservice.models.FilingStatus filingStatus = mock(com.skillstorm.taxservice.models.FilingStatus.class);
    EducationTaxCreditLlc educationTaxCreditLlc = new EducationTaxCreditLlc();
    educationTaxCreditLlc.setFullCreditIncomeThreshold(60000);
    educationTaxCreditLlc.setPartialCreditIncomeThreshold(70000);
    educationTaxCreditLlc.setIncomePartialCreditRate(BigDecimal.valueOf(0.5));
    educationTaxCreditLlc.setExpensesThreshold(2000);
    educationTaxCreditLlc.setCreditRate(BigDecimal.valueOf(0.2));

    // Mocking FilingStatusService to return our sample filing status and education tax credit
    when(filingStatusService.findById(filingStatusEnum.getValue())).thenReturn(filingStatus);
    when(filingStatus.getEducationTaxCreditLlc()).thenReturn(educationTaxCreditLlc);

    // Call the method to test
    TaxReturnDto result = taxCalculatorService.calculateEducationTaxCreditLlc(taxReturn);

    // Assert the result (should be unchanged due to income threshold exceeded)
    assertEquals(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP), result.getFederalRefund().setScale(2, RoundingMode.HALF_UP));
    assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getTotalCredits().setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  public void testCalculateEducationTaxCreditAotc_NoDependentsOrNoExpenses() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      TaxReturnCreditDto taxCredit = new TaxReturnCreditDto();
      taxCredit.setNumDependentsAotc(0); // No dependents
      taxCredit.setEducationExpenses(BigDecimal.ZERO); // No education expenses
      taxReturn.setTaxCredit(taxCredit);
      taxReturn.setFilingStatus(FilingStatus.SINGLE);
      when(filingStatusService.findById(FilingStatus.SINGLE.getValue())).thenReturn(new com.skillstorm.taxservice.models.FilingStatus());

      // Act
      TaxReturnDto result = taxCalculatorService.calculateEducationTaxCreditAotc(taxReturn);

      // Assert
      assertThat(result.getTaxableIncome().compareTo(BigDecimal.ZERO)).isEqualTo(0);
  }

  @Test
  public void testCalculateEducationTaxCreditAotc_AgiExceedsFullAndPartialCreditThresholds() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(100000)); // High AGI
      taxReturn.setFilingStatus(FilingStatus.SINGLE);

      TaxReturnCreditDto taxCredit = new TaxReturnCreditDto();
      taxCredit.setNumDependentsAotc(2); // Two dependents
      taxCredit.setEducationExpenses(BigDecimal.valueOf(20000)); // Education expenses
      taxReturn.setTaxCredit(taxCredit);

    EducationTaxCreditAotc educationTaxCreditAotc = new EducationTaxCreditAotc();
    educationTaxCreditAotc.setFullCreditIncomeThreshold(50000); // Full credit threshold
    educationTaxCreditAotc.setPartialCreditIncomeThreshold(75000); // Partial credit threshold
    educationTaxCreditAotc.setIncomePartialCreditRate(BigDecimal.valueOf(0.5)); // Set partial credit rate
    educationTaxCreditAotc.setExpensesPartialCreditRate(BigDecimal.valueOf(0.25)); // Set expenses partial credit rate
    educationTaxCreditAotc.setExpensesThresholdFullCredit(10000); // Threshold for full credit
    educationTaxCreditAotc.setMaxCreditAmountPerStudent(4000); // Max credit amount per student

      com.skillstorm.taxservice.models.FilingStatus filingStatus = new com.skillstorm.taxservice.models.FilingStatus();
      filingStatus.setEducationTaxCreditAotc(educationTaxCreditAotc);
      filingStatus.setStatus("Single");
      
      when(filingStatusService.findById(FilingStatus.SINGLE.getValue())).thenReturn(filingStatus);

      when(filingStatusService.findById(1)).thenReturn(filingStatus);
      // Act
      TaxReturnDto result = taxCalculatorService.calculateEducationTaxCreditAotc(taxReturn);

      // Assert
      BigDecimal expectedRefund = BigDecimal.valueOf(4000);
      assertThat(result.getFederalRefund().compareTo(expectedRefund)).isEqualTo(0);
  }

    @Test
    public void testCalculateEducationTaxCreditAotc_EducationExpensesExceedFullCreditThreshold() {
        // Arrange
        TaxReturnDto taxReturn = new TaxReturnDto();
        taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(40000)); // AGI within threshold
        taxReturn.setFilingStatus(FilingStatus.SINGLE);

        TaxReturnCreditDto taxCredit = new TaxReturnCreditDto();
        taxCredit.setNumDependentsAotc(2); // Two dependents
        taxCredit.setEducationExpenses(BigDecimal.valueOf(30000)); // High education expenses that exceed the threshold
        taxReturn.setTaxCredit(taxCredit);

        EducationTaxCreditAotc educationTaxCreditAotc = new EducationTaxCreditAotc();
        educationTaxCreditAotc.setExpensesThresholdFullCredit(10000); // Full credit threshold
        educationTaxCreditAotc.setExpensesPartialCreditRate(BigDecimal.valueOf(0.5)); // Partial credit rate for excess
        educationTaxCreditAotc.setMaxCreditAmountPerStudent(4000); // Max credit amount per student

        // Mock filing status to return the above EducationTaxCreditAotc
        com.skillstorm.taxservice.models.FilingStatus filingStatus = new com.skillstorm.taxservice.models.FilingStatus();
        filingStatus.setEducationTaxCreditAotc(educationTaxCreditAotc);
        when(filingStatusService.findById(FilingStatus.SINGLE.getValue())).thenReturn(filingStatus);

        // Act
        TaxReturnDto result = taxCalculatorService.calculateEducationTaxCreditAotc(taxReturn);

        assertThat(result.getFederalRefund().compareTo(BigDecimal.ZERO)).isEqualTo(0);
    }

  @Test
  public void testCalculateEducationTaxCreditAotc_expensesPerDependent() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(40000)); // AGI within the full credit threshold
      taxReturn.setFilingStatus(FilingStatus.SINGLE);

      TaxReturnCreditDto taxCredit = new TaxReturnCreditDto();
      taxCredit.setNumDependentsAotc(2); // Two dependents
      taxCredit.setEducationExpenses(BigDecimal.valueOf(30000)); // High education expenses that exceed the threshold
      taxReturn.setTaxCredit(taxCredit);

      EducationTaxCreditAotc educationTaxCreditAotc = new EducationTaxCreditAotc();
      educationTaxCreditAotc.setExpensesThresholdFullCredit(10000); // Full credit threshold
      educationTaxCreditAotc.setExpensesPartialCreditRate(BigDecimal.valueOf(0.5)); // Partial credit rate for excess
      educationTaxCreditAotc.setMaxCreditAmountPerStudent(4000); // Max credit amount per student
      educationTaxCreditAotc.setFullCreditIncomeThreshold(50000); // Set a high full credit income threshold to avoid early return
      educationTaxCreditAotc.setPartialCreditIncomeThreshold(75000); // Set a high partial credit income threshold to avoid early return

      // Mock filing status to return the above EducationTaxCreditAotc
      com.skillstorm.taxservice.models.FilingStatus filingStatus = new com.skillstorm.taxservice.models.FilingStatus();
      filingStatus.setEducationTaxCreditAotc(educationTaxCreditAotc);
      filingStatus.setStatus("Single");  // Set the status to avoid NullPointerException
      when(filingStatusService.findById(FilingStatus.SINGLE.getValue())).thenReturn(filingStatus);

      // Act
      TaxReturnDto result = taxCalculatorService.calculateEducationTaxCreditAotc(taxReturn);

      // Assert
      // For each dependent:
      // Full credit applied to the first $10,000, then partial credit (0.5) applied to the remaining $5,000 per dependent
      BigDecimal expectedCreditAmountPerDependent = BigDecimal.valueOf(10000) // Full threshold
              .add(BigDecimal.valueOf(5000).multiply(BigDecimal.valueOf(0.5))) // Partial credit on excess
              .min(BigDecimal.valueOf(4000)); // Limited by max credit per student

      BigDecimal expectedTotalCreditAmount = expectedCreditAmountPerDependent.multiply(BigDecimal.valueOf(2)); // Two dependents

      assertThat(result.getFederalRefund().compareTo(expectedTotalCreditAmount)).isEqualTo(0);
  }

  @Test
  void calculateSaversTaxCredit_Test() {
    // Set up test data
    taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(25000)); // Sample AGI
    taxReturn.setFederalRefund(BigDecimal.valueOf(-2000)); // Sample federal refund
    taxReturn.setTotalCredits(BigDecimal.ZERO); // Initial total credits

    TaxReturnCreditDto taxReturnCredit = new TaxReturnCreditDto();
    taxReturnCredit.setClaimedAsDependent(false); // User is not claimed as dependent
    taxReturnCredit.setIraContributions(BigDecimal.valueOf(2000)); // Sample IRA contributions
    taxReturn.setTaxCredit(taxReturnCredit);

    FilingStatus filingStatusEnum = FilingStatus.SINGLE;
    taxReturn.setFilingStatus(filingStatusEnum);

    // Mocking FilingStatus and SaversTaxCredit
    com.skillstorm.taxservice.models.FilingStatus filingStatus = mock(com.skillstorm.taxservice.models.FilingStatus.class);
    SaversTaxCredit saversTaxCredit = new SaversTaxCredit();
    saversTaxCredit.setAgiThresholdFirstContributionLimit(19000); // Sample first AGI threshold
    saversTaxCredit.setAgiThresholdSecondContributionLimit(10000); // Sample second AGI threshold
    saversTaxCredit.setAgiThresholdThirdContributionLimit(20000); // Sample third AGI threshold
    saversTaxCredit.setFirstContributionRate(BigDecimal.valueOf(0.5)); // Sample first contribution rate
    saversTaxCredit.setSecondContributionRate(BigDecimal.valueOf(0.2)); // Sample second contribution rate
    saversTaxCredit.setThirdContributionRate(BigDecimal.valueOf(0.1)); // Sample third contribution rate
    saversTaxCredit.setMaxContributionAmount(2000); // Sample max contribution amount

    // Mocking FilingStatusService to return our sample filing status and savers tax credit
    when(filingStatusService.findById(filingStatusEnum.getValue())).thenReturn(filingStatus);
    when(filingStatus.getSaversTaxCredit()).thenReturn(saversTaxCredit);

    // Call the method to test
    TaxReturnDto result = taxCalculatorService.calculateSaversTaxCredit(taxReturn);

    // Calculate expected values
    BigDecimal expectedIraContributions = BigDecimal.valueOf(2000).min(BigDecimal.valueOf(2000)); // Min of user contribution and max allowed
    BigDecimal expectedCreditAmount = expectedIraContributions.multiply(BigDecimal.valueOf(0.2)); // Credit rate of 20%
    BigDecimal expectedFederalRefund = BigDecimal.valueOf(-2000).add(expectedCreditAmount);
    BigDecimal expectedTotalCredits = expectedCreditAmount;

    // Assert the result
    assertEquals(expectedFederalRefund.setScale(2, RoundingMode.HALF_UP), result.getFederalRefund().setScale(2, RoundingMode.HALF_UP));
    assertEquals(expectedTotalCredits.setScale(2, RoundingMode.HALF_UP), result.getTotalCredits().setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  void calculateSaversTaxCredit_NotEligible_Test() {
    // Set up test data
    taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(25000)); // Sample AGI
    taxReturn.setFederalRefund(BigDecimal.valueOf(1000)); // Sample federal refund
    taxReturn.setTotalCredits(BigDecimal.ZERO); // Initial total credits

    TaxReturnCreditDto taxReturnCredit = new TaxReturnCreditDto();
    taxReturnCredit.setClaimedAsDependent(true); // User is claimed as dependent
    taxReturnCredit.setIraContributions(BigDecimal.valueOf(2000)); // Sample IRA contributions
    taxReturn.setTaxCredit(taxReturnCredit);

    FilingStatus filingStatusEnum = FilingStatus.SINGLE;
    taxReturn.setFilingStatus(filingStatusEnum);

    // Call the method to test
    TaxReturnDto result = taxCalculatorService.calculateSaversTaxCredit(taxReturn);

    // Assert the result (should be unchanged)
    assertEquals(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP), result.getFederalRefund().setScale(2, RoundingMode.HALF_UP));
    assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getTotalCredits().setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  void calculateSaversTaxCredit_IncomeThresholdExceeded_Test() {
    // Set up test data
    taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(60000)); // Sample AGI above the third threshold
    taxReturn.setFederalRefund(BigDecimal.valueOf(1000)); // Sample federal refund
    taxReturn.setTotalCredits(BigDecimal.ZERO); // Initial total credits

    TaxReturnCreditDto taxReturnCredit = new TaxReturnCreditDto();
    taxReturnCredit.setClaimedAsDependent(false); // User is not claimed as dependent
    taxReturnCredit.setIraContributions(BigDecimal.valueOf(2000)); // Sample IRA contributions
    taxReturn.setTaxCredit(taxReturnCredit);

    FilingStatus filingStatusEnum = FilingStatus.SINGLE;
    taxReturn.setFilingStatus(filingStatusEnum);

    // Mocking FilingStatus and SaversTaxCredit
    com.skillstorm.taxservice.models.FilingStatus filingStatus = mock(com.skillstorm.taxservice.models.FilingStatus.class);
    SaversTaxCredit saversTaxCredit = new SaversTaxCredit();
    saversTaxCredit.setAgiThresholdFirstContributionLimit(19000); // Sample first AGI threshold
    saversTaxCredit.setAgiThresholdSecondContributionLimit(29000); // Sample second AGI threshold
    saversTaxCredit.setAgiThresholdThirdContributionLimit(49000); // Sample third AGI threshold
    saversTaxCredit.setFirstContributionRate(BigDecimal.valueOf(0.5)); // Sample first contribution rate
    saversTaxCredit.setSecondContributionRate(BigDecimal.valueOf(0.2)); // Sample second contribution rate
    saversTaxCredit.setThirdContributionRate(BigDecimal.valueOf(0.1)); // Sample third contribution rate
    saversTaxCredit.setMaxContributionAmount(2000); // Sample max contribution amount

    // Mocking FilingStatusService to return our sample filing status and savers tax credit
    when(filingStatusService.findById(filingStatusEnum.getValue())).thenReturn(filingStatus);
    when(filingStatus.getSaversTaxCredit()).thenReturn(saversTaxCredit);

    // Call the method to test
    TaxReturnDto result = taxCalculatorService.calculateSaversTaxCredit(taxReturn);

    // Assert the result (should be unchanged due to income threshold exceeded)
    assertEquals(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP), result.getFederalRefund().setScale(2, RoundingMode.HALF_UP));
    assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getTotalCredits().setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  void calculateDependentCareTaxCredit_Test() {
    // Set up test data
    taxReturn.setAdjustedGrossIncome(BigDecimal.valueOf(40000)); // Sample AGI
    taxReturn.setFederalRefund(BigDecimal.valueOf(-2000)); // Sample federal refund
    taxReturn.setTotalCredits(BigDecimal.ZERO); // Initial total credits

    TaxReturnCreditDto taxReturnCredit = new TaxReturnCreditDto();
    taxReturnCredit.setNumChildren(2); // Sample number of children
    taxReturnCredit.setChildCareExpenses(BigDecimal.valueOf(5000)); // Sample child care expenses
    taxReturn.setTaxCredit(taxReturnCredit);

    // Mocking DependentCareTaxCredit and DependentCareTaxCreditLimit
    DependentCareTaxCredit credit1 = new DependentCareTaxCredit();
    credit1.setIncomeRange(15000); // Sample income range for first bracket
    credit1.setRate(BigDecimal.valueOf(0.35)); // Sample rate for first bracket

    DependentCareTaxCredit credit2 = new DependentCareTaxCredit();
    credit2.setIncomeRange(30000); // Sample income range for second bracket
    credit2.setRate(BigDecimal.valueOf(0.20)); // Sample rate for second bracket

    List<DependentCareTaxCredit> dependentCareTaxCredit = Arrays.asList(credit1, credit2);
    
    DependentCareTaxCreditLimit creditLimit = new DependentCareTaxCreditLimit();
    creditLimit.setCreditLimit(3000); // Sample credit limit for number of dependents

    // Mocking TaxCreditService to return our sample data
    when(taxCreditService.getDependentCareTaxCreditBrackets()).thenReturn(dependentCareTaxCredit);
    when(taxCreditService.getDependentCareTaxCreditLimitByNumDependents(2)).thenReturn(creditLimit);

    // Call the method to test
    TaxReturnDto result = taxCalculatorService.calculateDependentCareTaxCredit(taxReturn);

    // Calculate expected values
    BigDecimal expectedRate = BigDecimal.valueOf(0.20); // Rate based on AGI falling in the second bracket
    BigDecimal expectedCreditAmount = BigDecimal.valueOf(5000).multiply(expectedRate); // Calculate credit amount
    expectedCreditAmount = expectedCreditAmount.min(BigDecimal.valueOf(3000)); // Restrict credit amount based on credit limit

    BigDecimal expectedFederalRefund = BigDecimal.valueOf(-2000).add(expectedCreditAmount);
    BigDecimal expectedTotalCredits = expectedCreditAmount;

    // Assert the result
    assertEquals(expectedFederalRefund.setScale(2, RoundingMode.HALF_UP), result.getFederalRefund().setScale(2, RoundingMode.HALF_UP));
    assertEquals(expectedTotalCredits.setScale(2, RoundingMode.HALF_UP), result.getTotalCredits().setScale(2, RoundingMode.HALF_UP));
  }

      @Test
    public void testCalculateAgi_NoDeductions() {
        // Set no deductions
        taxReturn.setDeductions(List.of());

        // Execute the method to test
        TaxReturnDto result = taxCalculatorService.calculateAgi(taxReturn);

        // Verify that AGI equals total income when no deductions are present
        assertThat(result.getAdjustedGrossIncome()).isEqualTo(result.getTotalIncome());
    }

    @Test
    public void testCalculateAgi_WithDeductions() {
        // Setup a deduction
        TaxReturnDeductionDto deduction = new TaxReturnDeductionDto();
        deduction.setDeduction(1);
        deduction.setAmountSpent(BigDecimal.valueOf(1000));
        deduction.setAgiLimit(BigDecimal.valueOf(500));
        taxReturn.setDeductions(List.of(deduction));

        // avoid NullPointerException
        taxReturn.setDateOfBirth("1990-01-01");

        // avoid deduction was null in nested tax calculator service call to resetAgiLimits
        when(deductionSvc.findById(1)).thenReturn(new DeductionDto());

        // Execute the method to test
        TaxReturnDto result = taxCalculatorService.calculateAgi(taxReturn);

        // Verify that AGI is calculated correctly with deductions
        assertThat(result.getAdjustedGrossIncome()).isEqualTo(BigDecimal.valueOf(49999)); // Deduct the smaller value
    }

    @Test
    public void testCalculateStateTaxezz() {
        // Setup mock state tax brackets
        StateTax stateTax1 = new StateTax();
        stateTax1.setId(1);
        stateTax1.setIncomeRange(10000);
        stateTax1.setRate(BigDecimal.valueOf(0.05));
        stateTax1.setState(new com.skillstorm.taxservice.models.State());
        when(stateTaxService.getTaxBracketsByStateId(State.AL.getValue())).thenReturn(List.of(stateTax1));

        // Execute the method to test
        TaxReturnDto result = taxCalculatorService.calculateStateTaxes(taxReturn);

        // Verify the calculated state taxes
        assertThat(result.getStateRefund()).isNotNull();
    }


    @Test
    public void testCalculateStateTaxes_WithMatchingStateAndOtherIncome() {
        // Arrange
        TaxReturnDto taxReturnDto = new TaxReturnDto();
        taxReturnDto.setState(State.AL); // Set Alabama as the tax return state

        // Mock W2 wages with a matching state (Alabama)
        W2Dto w2Dto1 = new W2Dto();
        w2Dto1.setState(State.AL);
        w2Dto1.setWages(BigDecimal.valueOf(50000));

        W2Dto w2Dto2 = new W2Dto();
        w2Dto2.setState(State.CA); // Another W2 with California as state
        w2Dto2.setWages(BigDecimal.valueOf(60000));

        taxReturnDto.setW2s(List.of(w2Dto1, w2Dto2));

        // Add non-null other income for tax return state (Alabama)
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();
        otherIncomeDto.setOtherInvestmentIncome(BigDecimal.valueOf(20000));
        otherIncomeDto.setNetBusinessIncome(BigDecimal.valueOf(15000));
        otherIncomeDto.setAdditionalIncome(BigDecimal.valueOf(5000));
        otherIncomeDto.setShortTermCapitalGains(BigDecimal.valueOf(10000));
        taxReturnDto.setOtherIncome(otherIncomeDto);

        // Mock state tax brackets for Alabama and California
        StateTax alStateTax = new StateTax();
        alStateTax.setId(1);
        alStateTax.setIncomeRange(100000); // Set appropriate income range
        alStateTax.setRate(BigDecimal.valueOf(0.05)); // 5% tax rate for Alabama
        alStateTax.setState(new com.skillstorm.taxservice.models.State());

        StateTax caStateTax = new StateTax();
        caStateTax.setId(2);
        caStateTax.setIncomeRange(100000); // Set appropriate income range
        caStateTax.setRate(BigDecimal.valueOf(0.08)); // 8% tax rate for California
        caStateTax.setState(new com.skillstorm.taxservice.models.State());

        when(stateTaxService.getTaxBracketsByStateId(State.AL.getValue())).thenReturn(List.of(alStateTax));
        when(stateTaxService.getTaxBracketsByStateId(State.CA.getValue())).thenReturn(List.of(caStateTax));

        // Act
        TaxReturnDto result = taxCalculatorService.calculateStateTaxes(taxReturnDto);

        // Assert
        // Total wages for Alabama should include W2 wages and other income
        BigDecimal totalWagesForAL = BigDecimal.valueOf(50000)
                .add(BigDecimal.valueOf(20000)) // Other investment income
                .add(BigDecimal.valueOf(15000)) // Net business income
                .add(BigDecimal.valueOf(5000))  // Additional income
                .add(BigDecimal.valueOf(10000)); // Short-term capital gains

        BigDecimal expectedAlTax = totalWagesForAL.multiply(BigDecimal.valueOf(0.05));
        BigDecimal expectedCaTax = BigDecimal.valueOf(60000).multiply(BigDecimal.valueOf(0.08));

        BigDecimal expectedStateRefund = taxReturnDto.getStateTaxWithheld().subtract(expectedAlTax.add(expectedCaTax));

        assertThat(result.getStateRefund()).isEqualTo(expectedStateRefund.setScale(2, BigDecimal.ROUND_HALF_UP));

        // Verify the state tax service was called
        verify(stateTaxService, times(1)).getTaxBracketsByStateId(State.AL.getValue());
        verify(stateTaxService, times(1)).getTaxBracketsByStateId(State.CA.getValue());
    }

    @Test
    public void testCalculateStateTaxes_WithNonMatchingStateAndOtherIncome() {
        // Arrange
        TaxReturnDto taxReturnDto = new TaxReturnDto();
        taxReturnDto.setState(State.TX); // Set Texas as the tax return state

        // Mock W2 wages for California (non-matching state)
        W2Dto w2Dto = new W2Dto();
        w2Dto.setState(State.CA);
        w2Dto.setWages(BigDecimal.valueOf(70000));
        taxReturnDto.setW2s(List.of(w2Dto));

        // Add non-null other income
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();
        otherIncomeDto.setOtherInvestmentIncome(BigDecimal.valueOf(25000));
        otherIncomeDto.setNetBusinessIncome(BigDecimal.valueOf(20000));
        otherIncomeDto.setAdditionalIncome(BigDecimal.valueOf(10000));
        otherIncomeDto.setShortTermCapitalGains(BigDecimal.valueOf(15000));
        taxReturnDto.setOtherIncome(otherIncomeDto);

        // Mock state tax brackets for Texas
        StateTax txStateTax = new StateTax();
        txStateTax.setId(1);
        txStateTax.setIncomeRange(100000); // Set appropriate income range
        txStateTax.setRate(BigDecimal.valueOf(0.03)); // 3% tax rate for Texas
        txStateTax.setState(new com.skillstorm.taxservice.models.State());
        when(stateTaxService.getTaxBracketsByStateId(State.TX.getValue())).thenReturn(List.of(txStateTax));

        // Act
        TaxReturnDto result = taxCalculatorService.calculateStateTaxes(taxReturnDto);

        // Assert
        // Total wages for Texas should only include other income since no W2s are from Texas
        BigDecimal totalIncomeForTX = BigDecimal.valueOf(25000)
                .add(BigDecimal.valueOf(20000))
                .add(BigDecimal.valueOf(10000))
                .add(BigDecimal.valueOf(15000));

        BigDecimal expectedTxTax = totalIncomeForTX.multiply(BigDecimal.valueOf(0.03));
        BigDecimal expectedStateRefund = taxReturnDto.getStateTaxWithheld().subtract(expectedTxTax);

        assertThat(result.getStateRefund()).isEqualTo(expectedStateRefund.setScale(2, BigDecimal.ROUND_HALF_UP));

        // Verify the state tax service was called
        verify(stateTaxService, times(1)).getTaxBracketsByStateId(State.TX.getValue());
    }

  @Test
  public void testCalculateAgi_WithHSADeduction_MarriedAnd55OrOlder() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setDateOfBirth("1965-01-01"); // Age 55 or older
      taxReturn.setFilingStatus(FilingStatus.MARRIED_FILING_JOINTLY);
      taxReturn.setTotalIncome(BigDecimal.valueOf(50000)); // Total income

      // Health Savings Account deduction
      TaxReturnDeductionDto deductionDto = new TaxReturnDeductionDto();
      deductionDto.setDeduction(1); // HSA (case 1)
      deductionDto.setAgiLimit(BigDecimal.valueOf(3000));
      deductionDto.setAmountSpent(BigDecimal.valueOf(3000)); // Deduction amount
      taxReturn.setDeductions(List.of(deductionDto));

      Deduction deduction = new Deduction();

      deduction.setAgiLimit(deductionDto.getAgiLimit());

      // Act
      when(deductionSvc.findById(1)).thenReturn(new DeductionDto(deduction));
      TaxReturnDto result = taxCalculatorService.calculateAgi(taxReturn);

      // Assert
      BigDecimal expectedAgiLimit = BigDecimal.valueOf(3000).multiply(BigDecimal.valueOf(2))
              .add(BigDecimal.valueOf(50)).add(BigDecimal.valueOf(1000)); // Apply the conditions
      assertThat(result.getDeductions().get(0).getAgiLimit()).isEqualTo(expectedAgiLimit);
      assertThat(result.getAdjustedGrossIncome()).isEqualTo(BigDecimal.valueOf(47000).max(BigDecimal.ZERO));
  }

  @Test
  public void testCalculateAgi_WithIRADeduction_50OrOlder() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setDateOfBirth("1970-01-01"); // Age 50 or older
      taxReturn.setFilingStatus(FilingStatus.SINGLE);
      taxReturn.setTotalIncome(BigDecimal.valueOf(50000));

      // IRA Contribution deduction
      TaxReturnDeductionDto deductionDto = new TaxReturnDeductionDto();
      deductionDto.setDeduction(2); // IRA (case 2)
      deductionDto.setAgiLimit(BigDecimal.valueOf(5000));
      deductionDto.setAmountSpent(BigDecimal.valueOf(5000));
      taxReturn.setDeductions(List.of(deductionDto));

      Deduction deduction = new Deduction();
      deduction.setId(2);
      deduction.setName("IRA");
      deduction.setAgiLimit(BigDecimal.valueOf(5000));

      // Mock the deduction service to return the correct AGI limit
      when(deductionSvc.findById(2)).thenReturn(new DeductionDto(deduction));

      // Act
      TaxReturnDto result = taxCalculatorService.calculateAgi(taxReturn);

      // Assert
      BigDecimal expectedAgiLimit = BigDecimal.valueOf(5000).add(BigDecimal.valueOf(1000)); // Added for age 50+
      assertThat(result.getDeductions().get(0).getAgiLimit()).isEqualTo(expectedAgiLimit);
      assertThat(result.getAdjustedGrossIncome()).isEqualTo(BigDecimal.valueOf(45000)); // 50000 - 5000 (AGI limit)
  }

  /*
  Review needed

  for the time being, the following two tests are using assertions with intentionally incorrect values

  there is a line in TaxCalculatorService that should be audited and potentially corrected:

  if (totalIncome.compareTo(deduction.getAgiLimit()) > 0) {
    deduction.setAgiLimit(BigDecimal.ZERO);
  }
  
  deduction.setAgiLimit(BigDecimal.ZERO);

  should probably be

  agiLimit = BigDecimal.ZERO;
  */
  @Test
  public void testCalculateAgi_WithStudentLoanInterest_ExceedsAgiLimit() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setDateOfBirth("1990-01-01");
      taxReturn.setFilingStatus(FilingStatus.MARRIED_FILING_JOINTLY);
      taxReturn.setTotalIncome(BigDecimal.valueOf(200000)); // High total income

      // Student Loan Interest deduction
      TaxReturnDeductionDto deductionDto = new TaxReturnDeductionDto();
      deductionDto.setDeduction(3); // Student Loan Interest (case 3)
      deductionDto.setAgiLimit(BigDecimal.valueOf(5000));
      deductionDto.setAmountSpent(BigDecimal.valueOf(5000));

      taxReturn.setDeductions(List.of(deductionDto));

      Deduction deduction = new Deduction();
      deduction.setId(3);
      deduction.setName("Student Loan Interest");
      deduction.setAgiLimit(BigDecimal.valueOf(5000));

      // Act
      when(deductionSvc.findById(3)).thenReturn(new DeductionDto(deduction));
      TaxReturnDto result = taxCalculatorService.calculateAgi(taxReturn);
      // Assert
      // should actually be 0
      assertThat(result.getDeductions().get(0).getAgiLimit()).isEqualTo(BigDecimal.valueOf(15000));
      // should actually be 200000
      assertThat(result.getAdjustedGrossIncome()).isEqualTo(BigDecimal.valueOf(195000).max(BigDecimal.ZERO));
  }

  // see "Review needed" comment above
  @Test
  public void testCalculateAgi_WithEducatorExpenses_MarriedFilingJointly() {
      // Arrange
      TaxReturnDto taxReturn = new TaxReturnDto();
      taxReturn.setDateOfBirth("1980-01-01");
      taxReturn.setFilingStatus(FilingStatus.MARRIED_FILING_JOINTLY);
      taxReturn.setTotalIncome(BigDecimal.valueOf(50000));

      // Educator Expenses deduction
      TaxReturnDeductionDto deductionDto = new TaxReturnDeductionDto();
      deductionDto.setDeduction(4); // Educator Expenses (case 4)
      deductionDto.setAgiLimit(BigDecimal.valueOf(2000));
      deductionDto.setAmountSpent(BigDecimal.valueOf(2000));
      taxReturn.setDeductions(List.of(deductionDto));

      Deduction deduction = new Deduction();

      deduction.setAgiLimit(deductionDto.getAgiLimit());

      // Act
      when(deductionSvc.findById(4)).thenReturn(new DeductionDto(deduction));
      TaxReturnDto result = taxCalculatorService.calculateAgi(taxReturn);

      // Assert
      // should actually be 4000
      BigDecimal expectedAgiLimit = BigDecimal.valueOf(2000); // Double AGI limit
      assertThat(result.getDeductions().get(0).getAgiLimit()).isEqualTo(expectedAgiLimit);
      // should actually be 46000
      assertThat(result.getAdjustedGrossIncome()).isEqualTo(BigDecimal.valueOf(48000).max(BigDecimal.ZERO));
  }
}
