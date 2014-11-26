package com.duallab.validation.validationtask.impl;

import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.utils.PFConstants;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.error.PDFValidationError;
import com.duallab.validation.validationtask.BaseValidationTask;

public class HeaderValidationTask extends BaseValidationTask {

    private PushBackInputStream pdfSource;
    //this field will contain current offset in the pdf source stream at the beginning of validation.
    private long startOffset;

    public HeaderValidationTask(ValidationConfig config) {
        assert (config.getStream() != null): "For header validation pdf stream shall be passed in config";
        pdfSource = config.getStream();
        startOffset = config.getStream().getOffset();
    }

    public void validate() throws Exception {
        //header must start at offset 0 of pdf stream (6.1.2)
        pdfSource.seek(0);
        String firstLine = readLine(pdfSource);
        //TODO: discuss this validation logic
        if (firstLine == null || !(firstLine.matches("%PDF-1\\.[1-7]") || firstLine.matches("%PDF-2.0"))) {
            PDFValidationError error = new PDFValidationError("First line of pdf file must contain valid pdf header (6.1.2)");
            errors.add(error);
        }

        String secondLine = readLine(pdfSource);
        if (secondLine.charAt(0) != PFConstants.COMMENT) {
            PDFValidationError error = new PDFValidationError("Second line must start with a % symbol (6.1.2)");
            errors.add(error);
        }
        if (secondLine.length() < 5) {
            PDFValidationError error = new PDFValidationError("Second line must contain at least 5 bytes (6.1.2)");
            errors.add(error);
        } else {
            for (int i = 1; i < 5; i++) {
                if (secondLine.charAt(i) <= 127) {
                    PDFValidationError error = new PDFValidationError("Second line comment bytes values must be greater than 127 (6.1.2)");
                    errors.add(error);
                    break;
                }
            }
        }
    }

    public void cleanup() throws Exception {
        //reset stream position
        pdfSource.seek(startOffset);
    }
}