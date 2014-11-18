package com.duallab.validation.validationtask;

import java.util.List;

import com.duallab.validation.PDFValidationError;
import com.duallab.validation.ValidationConfig;

public interface ValidationTask {

    List<PDFValidationError> validate(ValidationConfig validationConfig) throws Exception;

}
