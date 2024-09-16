package com.skillstorm.taxservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class TaxCreditCalculatorTest {

    // -------------------------------------------------------------------------
    // Tests for calculateChildTaxCredit
    // -------------------------------------------------------------------------
    @Test
    void testCalculateChildTaxCredit_FullCredit_JointFilers() {
        // Joint filers, AGI below limit, full credit
        double result = TaxCreditCalculator.calculateChildTaxCredit(3, 350000, "joint", 10000, 60000);
        assertEquals(6000, result, "Expected full credit of 6000");
    }

    @Test
    void testCalculateChildTaxCredit_Phaseout_NoCredit_SingleFilers() {
        // Single filer, AGI above limit, no credit (complete phaseout)
        double result = TaxCreditCalculator.calculateChildTaxCredit(2, 250000, "single", 8000, 40000);
        assertEquals(1500, result, "Expected partial credit of 1500 due to phaseout");
    }
    
    // if (agi >= phaseoutStart) for single filers
    @Test
    void testCalculateAOTC_SingleFiler_AGIAbovePhaseout() {
        // Single filer with AGI above phaseout start (80,000) and qualified expenses
        double result = TaxCreditCalculator.calculateAOTC(2500, 85000, "single");
        assertTrue(result < 2500, "Expected reduced credit due to AGI phaseout for single filer");
    }

    // if (agi >= phaseoutStart) for joint filers
    @Test
    void testCalculateAOTC_JointFilers_AGIAbovePhaseout() {
        // Joint filers with AGI above phaseout start (160,000) and qualified expenses
        double result = TaxCreditCalculator.calculateAOTC(2500, 170000, "joint");
        assertTrue(result < 2500, "Expected reduced credit due to AGI phaseout for joint filers");
    }

    // if (agi >= phaseoutStart) with full phaseout (no credit)
    @Test
    void testCalculateAOTC_JointFilers_FullPhaseout() {
        // Joint filers with AGI well above phaseout start (160,000), full phaseout (credit reduced to 0)
        double result = TaxCreditCalculator.calculateAOTC(2500, 190000, "joint");
        assertEquals(0, result, "Expected no credit due to full phaseout for joint filers");
    }

    @Test
    void testCalculateChildTaxCredit_RefundOnly_SingleFilers() {
        // Single filer, no tax liability, refund (capped at $1600/child)
        double result = TaxCreditCalculator.calculateChildTaxCredit(1, 40000, "single", 0, 30000);
        assertEquals(1600, result, "Expected refund of 1600");
    }

    @Test
    void testCalculateChildTaxCredit_PartialCredit_JointFilers() {
        // Joint filers, AGI in phaseout range, partial credit
        double result = TaxCreditCalculator.calculateChildTaxCredit(2, 420000, "joint", 5000, 50000);
        assertEquals(3000, result, "Expected partial credit of 3000");
    }

    @Test
    void testCalculateChildTaxCredit_LowEarnedIncome_SingleFilers() {
        // Single filer, no refund due to low earned income
        double result = TaxCreditCalculator.calculateChildTaxCredit(1, 180000, "single", 2000, 2000);
        assertEquals(2000, result, "Expected credit of 2000 due to no refund eligibility");
    }

    // -------------------------------------------------------------------------
    // Tests for calculateEITC
    // -------------------------------------------------------------------------
    @Test
    void testCalculateEITC_SingleFiler_OneChild() {
        // Single filer, 1 child, AGI below limit
        double result = TaxCreditCalculator.calculateEITC(1, 45000, "single", 10000, 15000);
        assertEquals(3995, result, "Expected EITC of 3995");
    }

    @Test
    void testCalculateEITC_JointFilers_ThreeChildren() {
        // Joint filers, 3 children, AGI below limit
        double result = TaxCreditCalculator.calculateEITC(3, 60000, "joint", 8000, 25000);
        assertEquals(7430, result, "Expected EITC of 7430");
    }

    @Test
    void testCalculateEITC_SingleFiler_NoChildren() {
        // Single filer, no children, AGI below limit
        double result = TaxCreditCalculator.calculateEITC(0, 15000, "single", 5000, 10000);
        assertEquals(600, result, "Expected EITC of 600");
    }

    @Test
    void testCalculateEITC_JointFilers_TwoChildren_AgiAtLimit() {
        // Joint filers, 2 children, AGI at limit
        double result = TaxCreditCalculator.calculateEITC(2, 59478, "joint", 10000, 20000);
        assertEquals(6604, result, "Expected EITC of 6604");
    }

    // (numQualifyingChildren == 2) ? 59478 :
    @Test
    void testCalculateEITC_SingleFiler_TwoChildren_AGIBelowLimit() {
        // Single filer with 2 children, AGI below limit
        double result = TaxCreditCalculator.calculateEITC(2, 50000, "single", 5000, 20000);
        assertEquals(6604, result, "Expected EITC of 6604 for single filer with 2 children");
    }

    // (numQualifyingChildren == 1) ? 46560 :
    @Test
    void testCalculateEITC_SingleFiler_OneChild_AGIBelowLimit() {
        // Single filer with 1 child, AGI below limit
        double result = TaxCreditCalculator.calculateEITC(1, 45000, "single", 5000, 20000);
        assertEquals(3995, result, "Expected EITC of 3995 for single filer with 1 child");
    }

    // 17640;
    @Test
    void testCalculateEITC_SingleFiler_NoChildren_AGIBelowLimit() {
        // Single filer with no children, AGI below limit
        double result = TaxCreditCalculator.calculateEITC(0, 15000, "single", 5000, 10000);
        assertEquals(600, result, "Expected EITC of 600 for single filer with no children");
    }

    // (numQualifyingChildren == 2) ? 59478 :
    @Test
    void testCalculateEITC_JointFilers_TwoChildren_AGIBelowLimit() {
        // Joint filers with 2 children, AGI below limit
        double result = TaxCreditCalculator.calculateEITC(2, 55000, "joint", 5000, 30000);
        assertEquals(6604, result, "Expected EITC of 6604 for joint filers with 2 children");
    }

    // (numQualifyingChildren == 1) ? 53120 :
    @Test
    void testCalculateEITC_JointFilers_OneChild_AGIBelowLimit() {
        // Joint filers with 1 child, AGI below limit
        double result = TaxCreditCalculator.calculateEITC(1, 50000, "joint", 5000, 25000);
        assertEquals(3995, result, "Expected EITC of 3995 for joint filers with 1 child");
    }

    // 24210;
    @Test
    void testCalculateEITC_JointFilers_NoChildren_AGIBelowLimit() {
        // Joint filers with no children, AGI below limit
        double result = TaxCreditCalculator.calculateEITC(0, 20000, "joint", 5000, 15000);
        assertEquals(600, result, "Expected EITC of 600 for joint filers with no children");
    }

    // (numQualifyingChildren >= 3) ? 56838 :
    @Test
    void testCalculateEITC_SingleFiler_ThreeChildren_AGIBelowLimit() {
        // Single filer with 3 qualifying children, AGI below limit
        double result = TaxCreditCalculator.calculateEITC(3, 55000, "single", 5000, 30000);
        assertEquals(7430, result, "Expected EITC of 7430 for single filer with 3 children");
    }

    @Test
    void testCalculateEITC_SingleFiler_AgiAboveLimit() {
        // Single filer, 1 child, AGI above limit
        double result = TaxCreditCalculator.calculateEITC(1, 48000, "single", 10000, 15000);
        assertEquals(0, result, "Expected no EITC due to AGI exceeding limit");
    }

    @Test
    void testCalculateEITC_InvestmentIncomeTooHigh() {
        // Investment income too high
        double result = TaxCreditCalculator.calculateEITC(2, 50000, "joint", 12000, 20000);
        assertEquals(0, result, "Expected no EITC due to high investment income");
    }

    @Test
    void testCalculateEITC_NoEarnedIncome() {
        // No earned income
        double result = TaxCreditCalculator.calculateEITC(1, 40000, "single", 8000, 0);
        assertEquals(0, result, "Expected no EITC due to lack of earned income");
    }

    // -------------------------------------------------------------------------
    // Tests for calculateAOTC
    // -------------------------------------------------------------------------
    @Test
    void testCalculateAOTC_SingleFiler_ExpensesWithinTier() {
        // Single filer, AGI below limit, qualified expenses within first tier
        double result = TaxCreditCalculator.calculateAOTC(1500, 70000, "single");
        assertEquals(1500, result, "Expected AOTC of 1500");
    }

    @Test
    void testCalculateAOTC_JointFilers_ExpensesInBothTiers() {
        // Joint filers, AGI below limit, qualified expenses in both tiers
        double result = TaxCreditCalculator.calculateAOTC(3500, 150000, "joint");
        assertEquals(2375, result, "Expected AOTC of 2375");
    }

    @Test
    void testCalculateAOTC_JointFilers_AgiAboveLimit() {
        // Joint filers, AGI above limit
        double result = TaxCreditCalculator.calculateAOTC(4000, 190000, "joint");
        assertEquals(0, result, "Expected no AOTC due to AGI exceeding limit");
    }

    @Test
    void testCalculateAOTC_MarriedFilingSeparately() {
        // Married filing separately
        double result = TaxCreditCalculator.calculateAOTC(2000, 50000, "separate");
        assertEquals(0, result, "Expected no AOTC for married filing separately");
    }

    // -------------------------------------------------------------------------
    // Tests for calculateLLC
    // -------------------------------------------------------------------------
    @Test
    void testCalculateLLC_ExpensesBelowLimit() {
        // Qualified expenses below limit
        double result = TaxCreditCalculator.calculateLLC(5000);
        assertEquals(1000, result, "Expected LLC of 1000");
    }

    @Test
    void testCalculateLLC_ExpensesAtLimit() {
        // Qualified expenses at limit
        double result = TaxCreditCalculator.calculateLLC(10000);
        assertEquals(2000, result, "Expected LLC of 2000");
    }

    @Test
    void testCalculateLLC_ExpensesAboveLimit() {
        // Qualified expenses above limit
        double result = TaxCreditCalculator.calculateLLC(15000);
        assertEquals(2000, result, "Expected LLC capped at 2000");
    }

    // -------------------------------------------------------------------------
    // Tests for calculateSaversCredit
    // -------------------------------------------------------------------------
    @Test
    void testCalculateSaversCredit_SingleFiler_LowAgi_MaxContribution() {
        // Single filer, low AGI, max contribution
        double result = TaxCreditCalculator.calculateSaversCredit(2000, 15000, "single");
        assertEquals(1000, result, "Expected Saver's Credit of 1000");
    }

    @Test
    void testCalculateSaversCredit_JointFilers_PartialCredit() {
        // Joint filers, mid-range AGI, partial credit
        double result = TaxCreditCalculator.calculateSaversCredit(1200, 45000, "joint");
        assertEquals(240, result, "Expected Saver's Credit of 240");
    }

    @Test
    void testCalculateSaversCredit_HeadOfHousehold_HighAgi_NoCredit() {
        // Head of household, high AGI, no credit
        double result = TaxCreditCalculator.calculateSaversCredit(1800, 60000, "head");
        assertEquals(0, result, "Expected no Saver's Credit due to high AGI");
    }

    @Test
    void testCalculateSaversCredit_SingleFiler_AgiAtThreshold() {
        // Single filer, AGI at threshold for 10% credit
        double result = TaxCreditCalculator.calculateSaversCredit(800, 23750, "single");
        assertEquals(160, result, "Expected Saver's Credit of 160");
    }

    @Test
    void testCalculateSaversCredit_NegativeContribution() {
        // Negative contribution (invalid input)
        double result = TaxCreditCalculator.calculateSaversCredit(-500, 20000, "single");
        assertEquals(0, result, "Expected no Saver's Credit for negative contribution");
    }

    @Test
    void testCalculateSaversCredit_ContributionExceedingLimit() {
        // Contribution exceeding $2,000 (invalid input)
        double result = TaxCreditCalculator.calculateSaversCredit(2500, 18000, "single");
        assertEquals(0, result, "Expected no Saver's Credit for contribution exceeding limit");
    }

    @Test
    void testCalculateSaversCredit_InvalidFilingStatus() {
        // Invalid filing status
        double result = TaxCreditCalculator.calculateSaversCredit(1000, 35000, "invalid");
        assertEquals(0, result, "Expected no Saver's Credit for invalid filing status");
    }

    // if (agi <= 43500) { creditRate = 0.50; } for joint filers
    @Test
    void testCalculateSaversCredit_JointFiler_AGIAt50PercentCredit() {
        // Joint filer with AGI <= 43,500 and contribution of 2,000
        double result = TaxCreditCalculator.calculateSaversCredit(2000, 43500, "joint");
        assertEquals(1000, result, "Expected maximum Saver's Credit of 1000 for joint filer with AGI <= 43,500");
    }

    // else if (agi <= 73000) { creditRate = 0.10; } for joint filers
    @Test
    void testCalculateSaversCredit_JointFiler_AGIAt10PercentCredit() {
        // Joint filer with AGI <= 73,000 and contribution of 2,000
        double result = TaxCreditCalculator.calculateSaversCredit(2000, 73000, "joint");
        assertEquals(200, result, "Expected Saver's Credit of 200 for joint filer with AGI <= 73,000");
    }

    // if (agi <= 32625) { creditRate = 0.50; } for head of household
    @Test
    void testCalculateSaversCredit_HeadOfHousehold_AGIAt50PercentCredit() {
        // Head of household with AGI <= 32,625 and contribution of 2,000
        double result = TaxCreditCalculator.calculateSaversCredit(2000, 32625, "head");
        assertEquals(1000, result, "Expected maximum Saver's Credit of 1000 for head of household with AGI <= 32,625");
    }

    // else if (agi <= 54750) { creditRate = 0.10; } for head of household
    @Test
    void testCalculateSaversCredit_HeadOfHousehold_AGIAt10PercentCredit() {
        // Head of household with AGI <= 54,750 and contribution of 2,000
        double result = TaxCreditCalculator.calculateSaversCredit(2000, 54750, "head");
        assertEquals(200, result, "Expected Saver's Credit of 200 for head of household with AGI <= 54,750");
    }

    // else if (agi <= 36500) { creditRate = 0.10; } for single filers
    @Test
    void testCalculateSaversCredit_SingleFiler_AGIAt10PercentCredit() {
        // Single filer with AGI <= 36,500 and contribution of 2,000
        double result = TaxCreditCalculator.calculateSaversCredit(2000, 36500, "single");
        assertEquals(200, result, "Expected Saver's Credit of 200 for single filer with AGI <= 36,500");
    }

    // else if (agi <= 35625) { creditRate = 0.20; } for head of household
    @Test
    void testCalculateSaversCredit_HeadOfHousehold_AGIAt20PercentCredit() {
        // Head of household with AGI <= 35,625 and contribution of 2,000
        double result = TaxCreditCalculator.calculateSaversCredit(2000, 35000, "head");
        assertEquals(400, result, "Expected Saver's Credit of 400 for head of household with AGI <= 35,625");
    }
}
