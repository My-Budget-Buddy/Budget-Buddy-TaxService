package com.skillstorm.taxservice.exceptions;

import lombok.Data;

@Data
public class ErrorMessage {

    private int errorCode;
    private String message;
}
