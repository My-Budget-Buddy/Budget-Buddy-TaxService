package com.skillstorm.taxservice.services;

import com.skillstorm.taxservice.dtos.DeductionDto;
import com.skillstorm.taxservice.exceptions.NotFoundException;
import com.skillstorm.taxservice.repositories.DeductionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeductionService {

    private final DeductionRepository deductionRepository;

    @Autowired
    public DeductionService(DeductionRepository deductionRepository) {
        this.deductionRepository = deductionRepository;
    }

    // Find Deduction by ID:
    public DeductionDto findById(int id) {
        return new DeductionDto(deductionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("deduction.not.found")));
    }

    // Find list of all Above the Line Deductions:
    public List<DeductionDto> findAll() {
        return deductionRepository.findAll().stream().map(DeductionDto::new).toList();
    }
}
