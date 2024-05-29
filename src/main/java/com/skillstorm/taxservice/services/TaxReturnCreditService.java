package com.skillstorm.taxservice.services;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.skillstorm.taxservice.dtos.TaxReturnCreditDto;
import com.skillstorm.taxservice.exceptions.NotFoundException;
import com.skillstorm.taxservice.models.TaxReturn;
import com.skillstorm.taxservice.models.TaxReturnCredit;
import com.skillstorm.taxservice.repositories.TaxReturnCreditRepository;
import com.skillstorm.taxservice.repositories.TaxReturnRepository;
import com.skillstorm.taxservice.utilities.mappers.TaxReturnCreditMapper;

@Service
public class TaxReturnCreditService {
  
  private final TaxReturnCreditRepository taxReturnCreditRepository;
  private final TaxReturnRepository taxReturnRepository;
  private final Environment environment;

  public TaxReturnCreditService(TaxReturnCreditRepository taxReturnCreditRepository,
                                TaxReturnRepository taxReturnRepository,
                                Environment environment) {
    this.taxReturnCreditRepository = taxReturnCreditRepository;
    this.taxReturnRepository = taxReturnRepository;
    this.environment = environment;
  }


  public TaxReturnCreditDto findById(int id) {
    TaxReturnCredit existingTaxCreditReturn = taxReturnCreditRepository.findById(id)
      .orElseThrow(() -> new NotFoundException("tax return credit not found with id: " + id));

    return TaxReturnCreditMapper.toDto(existingTaxCreditReturn);
  }

  public TaxReturnCreditDto findByTaxReturnId(int taxReturnId) {
    TaxReturnCredit existingTaxCreditReturn = taxReturnCreditRepository.findByTaxReturnId(taxReturnId)
      .orElseThrow(() -> new NotFoundException(environment.getProperty("taxreturncredit.not.found") + taxReturnId));

    return TaxReturnCreditMapper.toDto(existingTaxCreditReturn);
  }

  public TaxReturnCreditDto createTaxReturnCredit(TaxReturnCreditDto taxReturnCreditDto) {
    TaxReturn existingTaxReturn = taxReturnRepository.findById(taxReturnCreditDto.getTaxReturnId())
      .orElseThrow(() -> new IllegalArgumentException("no tax return exists with id: " + taxReturnCreditDto.getTaxReturnId()));

    TaxReturnCredit newTaxReturnCredit = TaxReturnCreditMapper.toEntity(taxReturnCreditDto);
    newTaxReturnCredit.setTaxReturn(existingTaxReturn);
    newTaxReturnCredit = taxReturnCreditRepository.save(newTaxReturnCredit);
    return TaxReturnCreditMapper.toDto(newTaxReturnCredit);
  }

  public TaxReturnCreditDto updateTaxReturnCredit(TaxReturnCreditDto taxReturnCreditDto) {
    TaxReturnCredit existingTaxReturnCredit = taxReturnCreditRepository.findByTaxReturnId(taxReturnCreditDto.getTaxReturnId())
      .orElseThrow(() -> new NotFoundException(environment.getProperty("taxreturncredit.not.found") + taxReturnCreditDto.getTaxReturnId()));
    existingTaxReturnCredit = TaxReturnCreditMapper.updateEntity(existingTaxReturnCredit, taxReturnCreditDto);
    existingTaxReturnCredit = taxReturnCreditRepository.save(existingTaxReturnCredit);
    return TaxReturnCreditMapper.toDto(existingTaxReturnCredit);
  }

  public void deleteTaxReturnCredit(int id) {
    if(!taxReturnCreditRepository.existsById(id)) {
      throw new NotFoundException(environment.getProperty("taxreturncredit.not.found") + id);
    }
    taxReturnCreditRepository.deleteById(id);
  }
}
