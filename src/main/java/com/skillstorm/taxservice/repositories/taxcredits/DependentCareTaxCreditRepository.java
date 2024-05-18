package com.skillstorm.taxservice.repositories.taxcredits;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillstorm.taxservice.models.taxcredits.DependentCareTaxCredit;

@Repository
public interface DependentCareTaxCreditRepository extends JpaRepository<DependentCareTaxCredit, Integer> {
  
}
