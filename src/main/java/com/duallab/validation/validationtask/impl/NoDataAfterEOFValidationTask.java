package com.duallab.validation.validationtask.impl;

import java.util.ArrayList;
import java.util.List;

import com.duallab.utils.PFConstants;
import com.duallab.validation.PDFValidationError;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.validationtask.BaseValidationTask;

public class NoDataAfterEOFValidationTask extends BaseValidationTask {

    private List<PDFValidationError> errors = new ArrayList<>();

    public List<PDFValidationError> validate(ValidationConfig validationConfig) throws Exception {
        byte[] buffer = validationConfig.getBuffer();
        int bufOff = validationConfig.getBufOff();

        int expectedEOFOffset = bufOff + PFConstants.EOF_MARKER.length;
        if (expectedEOFOffset != buffer.length) {
            if ((buffer.length - expectedEOFOffset) > 2 || !(buffer[expectedEOFOffset] == PFConstants.LF
                    || buffer[expectedEOFOffset] == PFConstants.CR || buffer[expectedEOFOffset + 1] == PFConstants.LF)) {
                PDFValidationError error = new PDFValidationError("No data shall follow the last end-of-file marker except a single optional end-of-line marker. (6.1.3)");
                errors.add(error);
            }
        }

        return errors;
    }
}