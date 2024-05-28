package com.skillstorm.taxservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "deduction")
public class Deduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    // For itemized deductions, the AGI limit is a percentage of the taxpayer's AGI
    // For standard deductions, the AGI limit is either a flat amount or the maximum
    // AGI the taxpayer can have to claim the deduction:
    @Column(name = "agi_limit")
    private BigDecimal agiLimit;

    private boolean itemized;

    public Deduction(int id) {
        super();
        this.id = id;
    }
}
