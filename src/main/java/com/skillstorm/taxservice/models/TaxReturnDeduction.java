package com.skillstorm.taxservice.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(name = "taxreturn_deduction",
        uniqueConstraints = @UniqueConstraint(columnNames = {"taxreturn_id", "deduction_id"}))
public class TaxReturnDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxreturn_id")
    private TaxReturn taxReturn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "deduction_id")
    private Deduction deduction;

    @Column(name = "amount_spent")
    private BigDecimal amountSpent;
}
