package com.skillstorm.taxservice.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@NoArgsConstructor
@Entity
@Table(name = "taxreturn_credit",
  uniqueConstraints = @UniqueConstraint(columnNames = {"tax_return_id"}))
public class TaxReturnCredit {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @OneToOne
  @JsonIgnore
  @JoinColumn(name = "tax_return_id", nullable = false, unique = true)
  private TaxReturn taxReturn;

  // Number of user's dependents that qualify for child tax credit
  @Column(name = "num_dependents")
  private int numDependents;

  // Number of user's qualifying dependents who qualify for aotc credit 
  @Column(name = "num_dependents_aotc")
  private int numDependentsAotc;

  // Number of user's dependents under 13 who qualify for child care expenses credit
  @Column(name = "num_dependents_under_13")
  private int numChildren;

  // Amount paid in child care expenses
  @Column(name = "child_care_expenses")
  private BigDecimal childCareExpenses;

  // Amount paid in educational expenses regarding aotc (college) credit
  @Column(name = "education_expenses")
  private BigDecimal educationExpenses;

  // Amount paid in education expenses regarding llc education credit
  @Column(name = "llc_education_expenses")
  private BigDecimal llcEducationExpenses;

  // Amount contributed to IRA accounts
  @Column(name = "ira_contributions")
  private BigDecimal iraContributions;

  // Whether the user can be claimed as a dependent on another's tax return
  @Column(name = "claimed_as_dependent")
  private boolean claimedAsDependent;

  // Whether the user or one of their dependents can claim LLC education credit if they don't qualify for aotc
  @Column(name = "llc_credit")
  private boolean claimLlcCredit;

}
