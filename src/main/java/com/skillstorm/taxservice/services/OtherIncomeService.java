package com.skillstorm.taxservice.services;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.skillstorm.taxservice.dtos.OtherIncomeDto;
import com.skillstorm.taxservice.exceptions.NotFoundException;
import com.skillstorm.taxservice.models.OtherIncome;
import com.skillstorm.taxservice.repositories.OtherIncomeRepository;
import com.skillstorm.taxservice.repositories.TaxReturnRepository;
import com.skillstorm.taxservice.utilities.mappers.OtherIncomeMapper;

@Service
public class OtherIncomeService {
  
  private final OtherIncomeRepository otherIncomeRepository;
  private final TaxReturnRepository taxReturnRepository;
  private final Environment env;

  public OtherIncomeService(OtherIncomeRepository otherIncomeRepository, 
                            TaxReturnRepository taxReturnRepository,
                            Environment env) {
    this.otherIncomeRepository = otherIncomeRepository;
    this.taxReturnRepository = taxReturnRepository;
    this.env = env;
  }

  public OtherIncomeDto findById(int id) {
    OtherIncome existingOtherIncome = otherIncomeRepository.findById(id)
      .orElseThrow(() -> new NotFoundException("other income not found with id: " + id));

    return OtherIncomeMapper.toDto(existingOtherIncome);
  }

  public OtherIncomeDto findByTaxReturnId(int taxReturnId) {
    OtherIncome existingOtherIncome = otherIncomeRepository.findByTaxReturnId(taxReturnId)
    .orElseThrow(() -> new NotFoundException(env.getProperty("otherincome.not.found") + taxReturnId));

    return OtherIncomeMapper.toDto(existingOtherIncome);
  }

  public OtherIncomeDto createOtherIncome(OtherIncomeDto otherIncomeDto) {
    taxReturnRepository.findById(otherIncomeDto.getTaxReturnId())
      .orElseThrow(() -> new IllegalArgumentException("No existing tax return with ID: " + otherIncomeDto.getTaxReturnId()));
    OtherIncome newOtherIncome = OtherIncomeMapper.toEntity(otherIncomeDto);
    newOtherIncome = otherIncomeRepository.save(newOtherIncome);

    return OtherIncomeMapper.toDto(newOtherIncome);
  }

  public OtherIncomeDto updateOtherIncome(OtherIncomeDto otherIncomeDto) {
    OtherIncome existingOtherIncome = otherIncomeRepository.findByTaxReturnId(otherIncomeDto.getTaxReturnId())
      .orElseThrow(() -> new NotFoundException(env.getProperty("otherincome.not.found") + otherIncomeDto.getTaxReturnId()));
    existingOtherIncome = OtherIncomeMapper.updateEntity(existingOtherIncome, otherIncomeDto);
    existingOtherIncome = otherIncomeRepository.save(existingOtherIncome);

    return OtherIncomeMapper.toDto(existingOtherIncome);
  }

  public void deleteOtherIncome(OtherIncomeDto otherIncomeDto) {
    OtherIncome existingOtherIncome = otherIncomeRepository.findByTaxReturnId(otherIncomeDto.getTaxReturnId())
      .orElseThrow(() -> new NotFoundException(env.getProperty("otherincome.not.found") + otherIncomeDto.getTaxReturnId()));
    otherIncomeRepository.delete(existingOtherIncome);
  }

  public BigDecimal sumOtherIncome(OtherIncomeDto otherIncomeDto) {
    BigDecimal sum = BigDecimal.ZERO;
        
    // Get all declared fields of the class
    Field[] fields = otherIncomeDto.getClass().getDeclaredFields();
    
    for (Field field : fields) {
        // Check if the field is of type BigDecimal
        if (field.getType().equals(BigDecimal.class)) {
            field.setAccessible(true); // Make private fields accessible
            try {
              // Sum valid values
              BigDecimal value = (BigDecimal) field.get(otherIncomeDto);
              if (value != null) {
                  sum = sum.add(value);
              }
            } catch (IllegalAccessException e) {
              e.printStackTrace();
            }
        }
    }
    
    return sum;
  }
}
