package com.skillstorm.taxservice.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.skillstorm.taxservice.exceptions.NotFoundException;
import com.skillstorm.taxservice.models.FilingStatus;
import com.skillstorm.taxservice.repositories.FilingStatusRepository;

@Service
public class FilingStatusService {
  
  private FilingStatusRepository filingStatusRepository;

  public FilingStatusService(FilingStatusRepository filingStatusRepository) {
    this.filingStatusRepository = filingStatusRepository;
  }

  // Get filing status by the status string
  public FilingStatus findByStatus(String status) {
    Optional<FilingStatus> filingStatus = filingStatusRepository.findByStatus(status);

    if (filingStatus.isPresent()) {
      return filingStatus.get();
    } else {
      throw new NotFoundException("Filing status: " + status + " not found");
    }
  }

  public FilingStatus findById(int id) {
    return filingStatusRepository.findById(id)
      .orElseThrow(() -> new NotFoundException("No filing status found with ID: " + id));
  }
}
