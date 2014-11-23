package com.duallab.validation.validationtask.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.utils.PFConstants;
import com.duallab.validation.PDFValidationError;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.validationtask.BaseValidationTask;

public class HeaderValidationTask extends BaseValidationTask {

    private List<PDFValidationError> errors = new ArrayList<>();
    private PushBackInputStream pdfSource;

    public List<PDFValidationError> validate(ValidationConfig validationConfig) throws Exception {
        //this field will contain current offset in the pdf source stream at the beginning of validation.
        final long startOffset = validationConfig.getStream().getOffset();
        this.pdfSource = validationConfig.getStream();

        //header must start at offset 0 of pdf stream (6.1.2)
        this.pdfSource.seek(0);
        String firstLine = readLine(pdfSource);
        //TODO: discuss this validation logic
        if (firstLine == null || !(firstLine.matches("%PDF-1\\.[1-7]") || firstLine.matches("%PDF-2.0"))) {
            PDFValidationError error = new PDFValidationError("Invalid header : first line of pdf file must contain valid pdf header (6.1.2)");
            errors.add(error);
        }

        String secondLine = readLine(pdfSource);
        if (secondLine.charAt(0) != PFConstants.COMMENT) {
            PDFValidationError error = new PDFValidationError("Invalid header : second line must start with a % symbol (6.1.2)");
            errors.add(error);
        }
        if (secondLine.length() < 5) {
            PDFValidationError error = new PDFValidationError("Invalid header : second line must contain at least 5 bytes (6.1.2)");
            errors.add(error);
        } else {
            for (int i = 1; i < 5; i++) {
                if (secondLine.charAt(i) <= 127) {
                    PDFValidationError error = new PDFValidationError("Invalid header : second line comment bytes values must be greater than 127 (6.1.2)");
                    errors.add(error);
                    break;
                }
            }
        }

        //reset current stream position
        this.pdfSource.seek(startOffset);

        return errors;
    }

}