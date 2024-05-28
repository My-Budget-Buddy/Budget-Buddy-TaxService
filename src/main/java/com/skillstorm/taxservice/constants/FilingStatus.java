package com.skillstorm.taxservice.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum FilingStatus {

    SINGLE(1, "Single"), MARRIED_FILING_JOINTLY(2, "Married: Filing Jointly"),
    MARRIED_FILING_SEPARATELY(3, "Married: Filing Separately"), HEAD_OF_HOUSEHOLD(4, "Head of Household"), WIDOW(5, "Widow");

    // Convert String to FilingStatus. Used to translate JSON from request to Java object:
    public static FilingStatus fromString(String status) {
        return switch (status) {
            case "MARRIED: FILING JOINTLY" -> MARRIED_FILING_JOINTLY;
            case "MARRIED: FILING SEPARATELY" -> MARRIED_FILING_SEPARATELY;
            case "HEAD OF HOUSEHOLD" -> HEAD_OF_HOUSEHOLD;
            case "WIDOW" -> WIDOW;
            default -> SINGLE;
        };
    }

    // Convert int to FilingStatus. Used to translate integer from database to Java object:
    public static FilingStatus fromValue(int status) {
        return switch (status) {
            case 2 -> MARRIED_FILING_JOINTLY;
            case 3 -> MARRIED_FILING_SEPARATELY;
            case 4 -> HEAD_OF_HOUSEHOLD;
            case 5 -> WIDOW;
            default -> SINGLE;
        };
    }

    // View list of all Filing Statuses so that front end can display them in a dropdown:
    public static List<String> getFilingStatuses() {
        return Arrays.stream(FilingStatus.values()).map(Enum::toString).toList();
    }

    private final int value;
    private final String label;

    FilingStatus(int value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
