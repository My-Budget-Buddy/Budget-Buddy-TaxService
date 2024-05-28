package com.skillstorm.taxservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skillstorm.taxservice.constants.State;
import com.skillstorm.taxservice.models.TaxReturn;
import com.skillstorm.taxservice.models.W2;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class W2Dto {

    private int id;

    @Min(value = 1, message = "{taxreturn.id.min}")
    private int taxReturnId;

    @Min(value = 2015, message = "{year.must}")
    private int year;

    private int userId;

    @NotNull(message = "{w2.employer.must}")
    @NotEmpty(message = "{w2.employer.must}")
    private String employer;

    @NotNull(message = "{w2.state.must}")
    private State state;

    @Min(value = 0, message = "{w2.wages.min}")
    private BigDecimal wages;

    @Min(value = 0, message = "{w2.federalIncomeTaxWithheld.min}")
    private BigDecimal federalIncomeTaxWithheld;

    @Min(value = 0, message = "{w2.stateIncomeTaxWithheld.min}")
    private BigDecimal stateIncomeTaxWithheld;

    @Min(value = 0, message = "{w2.socialSecurityTaxWithheld.min}")
    private BigDecimal socialSecurityTaxWithheld;

    @Min(value = 0, message = "{w2.medicareTaxWithheld.min}")
    private BigDecimal medicareTaxWithheld;

    private String imageKey;

    public W2Dto() {
        // Default values to avoid null pointers in tax return calculations:
        this.wages = BigDecimal.ZERO.setScale(2);
        this.federalIncomeTaxWithheld = BigDecimal.ZERO.setScale(2);
        this.stateIncomeTaxWithheld = BigDecimal.ZERO.setScale(2);
        this.socialSecurityTaxWithheld = BigDecimal.ZERO.setScale(2);
        this.medicareTaxWithheld = BigDecimal.ZERO.setScale(2);
    }

    public W2Dto(W2 w2) {
        this();
        this.id = w2.getId();
        this.taxReturnId = w2.getTaxReturn().getId();
        this.year = w2.getYear();
        this.userId = w2.getUserId();
        this.employer = w2.getEmployer();
        this.state = State.fromValue(w2.getState());
        this.wages = w2.getWages();
        this.federalIncomeTaxWithheld = w2.getFederalIncomeTaxWithheld();
        this.stateIncomeTaxWithheld = w2.getStateIncomeTaxWithheld();
        this.socialSecurityTaxWithheld = w2.getSocialSecurityTaxWithheld();
        this.medicareTaxWithheld = w2.getMedicareTaxWithheld();
        this.imageKey = w2.getImageKey();
    }

    @JsonIgnore
    public W2 mapToEntity() {
        W2 w2 = new W2();
        w2.setId(id);
        w2.setTaxReturn(new TaxReturn(taxReturnId));
        w2.setYear(year);
        w2.setUserId(userId);
        w2.setEmployer(employer);
        if(state != null) {
            w2.setState(state.getValue());
        }
        w2.setWages(wages.setScale(2, BigDecimal.ROUND_HALF_UP));
        w2.setFederalIncomeTaxWithheld(federalIncomeTaxWithheld.setScale(2, BigDecimal.ROUND_HALF_UP));
        w2.setStateIncomeTaxWithheld(stateIncomeTaxWithheld.setScale(2, BigDecimal.ROUND_HALF_UP));
        w2.setSocialSecurityTaxWithheld(socialSecurityTaxWithheld.setScale(2, BigDecimal.ROUND_HALF_UP));
        w2.setMedicareTaxWithheld(medicareTaxWithheld.setScale(2, BigDecimal.ROUND_HALF_UP));
        w2.setImageKey(imageKey);
        return w2;
    }

    @Override
    public String toString() {
        return "W2Dto{" +
                "id=" + id +
                ", taxReturnId=" + taxReturnId +
                ", year=" + year +
                ", userId=" + userId +
                ", employer='" + employer + '\'' +
                ", state=" + state +
                ", wages=" + wages +
                ", federalIncomeTaxWithheld=" + federalIncomeTaxWithheld +
                ", stateIncomeTaxWithheld=" + stateIncomeTaxWithheld +
                ", socialSecurityTaxWithheld=" + socialSecurityTaxWithheld +
                ", medicareTaxWithheld=" + medicareTaxWithheld +
                ", imageKey='" + imageKey + '\'' +
                '}';
    }
}
