package com.duallab.validation.error;

public class PDFStructureError extends PDFError {

    private String message;

    public PDFStructureError() {
    }

    public PDFStructureError(String message) {
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
