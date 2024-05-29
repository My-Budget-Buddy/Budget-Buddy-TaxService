package com.skillstorm.taxservice.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillstorm.taxservice.models.TaxReturnCredit;

@Repository
public interface TaxReturnCreditRepository extends JpaRepository<TaxReturnCredit, Integer> {

   Optional<TaxReturnCredit> findByTaxReturnId(int id);

}
