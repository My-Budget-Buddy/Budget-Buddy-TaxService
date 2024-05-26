package com.skillstorm.taxservice.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skillstorm.taxservice.constants.FilingStatus;
import com.skillstorm.taxservice.constants.State;
import com.skillstorm.taxservice.models.TaxReturn;
import com.skillstorm.taxservice.utilities.mappers.OtherIncomeMapper;
import com.skillstorm.taxservice.utilities.mappers.TaxReturnCreditMapper;

import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TaxReturnDto {

    private int id;

    @Min(value = 2015, message = "{year.must}")
    private int year;

    private int userId;

    private FilingStatus filingStatus;

    @Size(min = 1, max = 50, message = "{firstName.size}")
    private String firstName;

    @Size(min = 1, max = 50, message = "{lastName.size}")
    private String lastName;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "{email.invalid}")
    private String email;

    @Pattern(regexp = "^\\d{3}-\\d{3}-\\d{4}$", message = "{phone.invalid}")
    private String phoneNumber;

    private String address;
    private String city;
    private State state;

    @Pattern(regexp = "^\\d{5}$", message = "{zip.invalid}")
    private String zip;

    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$", message = "{date.invalid}")
    private String dateOfBirth;

    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{4}$", message = "{ssn.invalid}")
    private String ssn;

    private List<W2Dto> w2s;
    private List<TaxReturnDeductionDto> deductions;
    private OtherIncomeDto otherIncome;
    private TaxReturnCreditDto taxCredit;
    private BigDecimal totalIncome;
    private BigDecimal adjustedGrossIncome;
    private BigDecimal taxableIncome;
    private BigDecimal fedTaxWithheld;
    private BigDecimal stateTaxWithheld;
    private BigDecimal socialSecurityTaxWithheld;
    private BigDecimal medicareTaxWithheld;
    private BigDecimal totalCredits;
    private BigDecimal federalRefund;
    private BigDecimal stateRefund;

    public TaxReturnDto() {
        // Default values to avoid null pointers:
        this.filingStatus = FilingStatus.SINGLE;
        this.state = State.AL;
        w2s = List.of();
        deductions = List.of();
        totalIncome = BigDecimal.ZERO.setScale(2);
        adjustedGrossIncome = BigDecimal.ZERO.setScale(2);
        taxableIncome = BigDecimal.ZERO.setScale(2);
        fedTaxWithheld = BigDecimal.ZERO.setScale(2);
        stateTaxWithheld = BigDecimal.ZERO.setScale(2);
        socialSecurityTaxWithheld = BigDecimal.ZERO.setScale(2);
        medicareTaxWithheld = BigDecimal.ZERO.setScale(2);
        totalCredits = BigDecimal.ZERO.setScale(2);
        federalRefund = BigDecimal.ZERO.setScale(2);
        stateRefund = BigDecimal.ZERO.setScale(2);
    }

    @JsonCreator
    public TaxReturnDto(@JsonProperty("filingStatus") String filingStatus) {
        this();
        if(filingStatus != null) {
            this.filingStatus = FilingStatus.fromString(filingStatus.trim().toUpperCase());
        }
    }

    public TaxReturnDto(TaxReturn taxReturn) {
        this();
        this.id = taxReturn.getId();
        this.year = taxReturn.getYear();
        this.userId = taxReturn.getUserId();
        this.filingStatus = FilingStatus.fromValue(taxReturn.getFilingStatus());
        this.firstName = taxReturn.getFirstName();
        this.lastName = taxReturn.getLastName();
        this.email = taxReturn.getEmail();
        this.phoneNumber = taxReturn.getPhoneNumber();
        this.address = taxReturn.getAddress();
        this.city = taxReturn.getCity();
        this.state = taxReturn.getState();
        this.zip = taxReturn.getZip();
        if (taxReturn.getDateOfBirth() != null) {
            this.dateOfBirth = taxReturn.getDateOfBirth().toString();
        }
        this.ssn = taxReturn.getSsn();
        this.w2s = taxReturn.getW2s().stream().map(W2Dto::new).toList();
        this.deductions = taxReturn.getDeductions().stream().map(TaxReturnDeductionDto::new).toList();
        this.otherIncome = OtherIncomeMapper.toDto(taxReturn.getOtherIncome());
        this.taxCredit = TaxReturnCreditMapper.toDto(taxReturn.getTaxCredit());
        this.totalIncome = taxReturn.getTotalIncome();
        this.adjustedGrossIncome = taxReturn.getAdjustedGrossIncome();
        this.taxableIncome = taxReturn.getTaxableIncome();
        this.fedTaxWithheld = taxReturn.getFedTaxWithheld();
        this.stateTaxWithheld = taxReturn.getStateTaxWithheld();
        this.socialSecurityTaxWithheld = taxReturn.getSocialSecurityTaxWithheld();
        this.medicareTaxWithheld = taxReturn.getMedicareTaxWithheld();
        this.totalCredits = taxReturn.getTotalCredits();
        this.federalRefund = taxReturn.getFederalRefund();
        this.stateRefund = taxReturn.getStateRefund();
    }

    public TaxReturn mapToEntity() {
        TaxReturn taxReturn = new TaxReturn();
        taxReturn.setId(id);
        taxReturn.setYear(year);
        taxReturn.setUserId(userId);
        taxReturn.setFilingStatus(filingStatus.getValue());
        taxReturn.setFirstName(firstName);
        taxReturn.setLastName(lastName);
        taxReturn.setEmail(email);
        taxReturn.setPhoneNumber(phoneNumber);
        taxReturn.setAddress(address);
        taxReturn.setCity(city);
        taxReturn.setState(state);
        taxReturn.setZip(zip);
        if (dateOfBirth != null) {
            taxReturn.setDateOfBirth(LocalDate.parse(dateOfBirth));
        }
        taxReturn.setSsn(ssn);
        taxReturn.setW2s(w2s.stream().map(W2Dto::mapToEntity).toList());
        taxReturn.setDeductions(deductions.stream().map(TaxReturnDeductionDto::mapToEntity).toList());
        taxReturn.setOtherIncome(OtherIncomeMapper.toEntity(otherIncome));
        taxReturn.setTaxCredit(TaxReturnCreditMapper.toEntity(taxCredit));
        taxReturn.setTotalIncome(totalIncome);
        taxReturn.setAdjustedGrossIncome(adjustedGrossIncome);
        taxReturn.setTaxableIncome(taxableIncome);
        taxReturn.setFedTaxWithheld(fedTaxWithheld);
        taxReturn.setStateTaxWithheld(stateTaxWithheld);
        taxReturn.setSocialSecurityTaxWithheld(socialSecurityTaxWithheld);
        taxReturn.setMedicareTaxWithheld(medicareTaxWithheld);
        taxReturn.setTotalCredits(totalCredits);
        taxReturn.setFederalRefund(federalRefund);
        taxReturn.setStateRefund(stateRefund);

        return taxReturn;
    }
}
