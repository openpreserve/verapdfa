package com.duallab.validation.error;

public class PDFValidationError extends PDFError {

    private String message;

    public PDFValidationError() {
    }

    public PDFValidationError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
