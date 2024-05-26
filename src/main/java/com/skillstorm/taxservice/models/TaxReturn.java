package com.skillstorm.taxservice.models;

import jakarta.persistence.*;
import lombok.Data;

import com.skillstorm.taxservice.constants.State;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "tax_return",
    uniqueConstraints = @UniqueConstraint(columnNames = {"year", "user_id"}))
public class TaxReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int year;

    @Column(name = "filing_status")
    private int filingStatus;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String address;

    private String city;

    @Enumerated(EnumType.ORDINAL)
    private State state;

    private String zip;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String ssn;

    @OneToMany(mappedBy = "taxReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<W2> w2s;

    @OneToMany(mappedBy = "taxReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaxReturnDeduction> deductions;

    @OneToOne(mappedBy = "taxReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private OtherIncome otherIncome;

    @OneToOne(mappedBy = "taxReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private TaxReturnCredit taxCredit;

    @Column(name = "total_income")
    private BigDecimal totalIncome;

    @Column(name = "adjusted_gross_income")
    private BigDecimal adjustedGrossIncome;

    @Column(name = "taxable_income")
    private BigDecimal taxableIncome;

    @Column(name = "fed_tax_withheld")
    private BigDecimal fedTaxWithheld;

    @Column(name = "state_tax_withheld")
    private BigDecimal stateTaxWithheld;

    @Column(name = "social_security_tax_withheld")
    private BigDecimal socialSecurityTaxWithheld;

    @Column(name = "medicare_tax_withheld")
    private BigDecimal medicareTaxWithheld;

    @Column(name = "total_credits")
    private BigDecimal totalCredits;

    @Column(name = "federal_refund")
    private BigDecimal federalRefund;

    @Column(name = "state_refund")
    private BigDecimal stateRefund;

    public TaxReturn() {
        w2s = List.of();
        deductions = List.of();
    }

    public TaxReturn(int id) {
        this();
        this.id = id;
    }
}