package com.skillstorm.taxservice.repositories;

import com.skillstorm.taxservice.models.TaxReturnDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxReturnDeductionRepository extends JpaRepository<TaxReturnDeduction, Integer> {

    // Delete all TaxReturns associated with a given tax return:
    void deleteAllByTaxReturnId(int taxReturnId);

    // Find all TaxReturns associated with a given tax return:
    List<TaxReturnDeduction> findAllByTaxReturnId(int taxReturnId);
}
