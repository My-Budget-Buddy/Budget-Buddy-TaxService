package com.skillstorm.taxservice.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "w2")
public class W2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_return_id")
    private TaxReturn taxReturn;

    @Column(name = "years")
    private int year;

    @Column(name = "user_id")
    private int userId;

    private String employer;

    private BigDecimal wages = BigDecimal.ZERO;

    private int state;

    @Column(name = "federal_income_tax_withheld")
    private BigDecimal federalIncomeTaxWithheld;

    @Column(name = "state_income_tax_withheld")
    private BigDecimal stateIncomeTaxWithheld;

    @Column(name = "social_security_tax_withheld")
    private BigDecimal socialSecurityTaxWithheld;

    @Column(name = "medicare_tax_withheld")
    private BigDecimal medicareTaxWithheld;

    @Column(name = "image_key")
    private String imageKey;

    @Override
    public String toString() {
        return "W2{" +
                "id=" + id +
                ", taxReturn=" + taxReturn.getId() +
                ", year=" + year +
                ", userId=" + userId +
                ", employer='" + employer + '\'' +
                ", wages=" + wages +
                ", state=" + state +
                ", federalIncomeTaxWithheld=" + federalIncomeTaxWithheld +
                ", stateIncomeTaxWithheld=" + stateIncomeTaxWithheld +
                ", socialSecurityTaxWithheld=" + socialSecurityTaxWithheld +
                ", medicareTaxWithheld=" + medicareTaxWithheld +
                ", imageKey='" + imageKey + '\'' +
                '}';
    }
}
