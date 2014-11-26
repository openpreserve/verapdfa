package com.duallab.validation.validationtask;

import java.util.List;

import com.duallab.validation.error.PDFError;

public interface ValidationTask {

    void validate() throws Exception;

    void cleanup() throws Exception;

    List<PDFError> getErrors();

}
