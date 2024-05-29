package com.skillstorm.taxservice.repositories;

import com.skillstorm.taxservice.models.TaxReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxReturnRepository extends JpaRepository<TaxReturn, Integer> {

    // Find all TaxReturns by userId:
    List<TaxReturn> findAllByUserId(int userId);

    // Find all TaxReturns by UserId for a given year. Should only return one:
    List<TaxReturn> findAllByUserIdAndYear(int userId, int year);

    // Delete all TaxReturns by userId:
    void deleteAllByUserId(int userId);
}
