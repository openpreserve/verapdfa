package com.duallab.validation.validationtask.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.utils.PFConstants;
import com.duallab.validation.PDFValidationError;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.validationtask.BaseValidationTask;

public class HeaderValidationTask extends BaseValidationTask {

    private PushBackInputStream pdfSource;

    public List<PDFValidationError> validate(ValidationConfig validationConfig) throws Exception {
        List<PDFValidationError> errors = new ArrayList<>();

        //this field will contain current offset in the pdf source stream at the beginning of validation.
        final long startOffset = validationConfig.getStream().getOffset();
        this.pdfSource = validationConfig.getStream();


        //header must start at offset 0 of pdf stream (6.1.2)
        this.pdfSource.seek(0);
        String firstLine = readLine();
        //TODO: discuss this validation logic
        if (firstLine == null || !(firstLine.matches("%PDF-1\\.[1-7]") || firstLine.matches("%PDF-2.0"))) {
            PDFValidationError error = new PDFValidationError("Invalid header : first line of pdf file must contain valid pdf header (6.1.2)");
            errors.add(error);
        }

        String secondLine = readLine();
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

    protected String readLine() throws IOException {
        if (pdfSource.isEOF()) {
            throw new IOException( "Error: End-of-File, expected line");
        }

        StringBuilder buffer = new StringBuilder();

        int c;
        while ((c = pdfSource.read()) != -1) {
            if (isEOL(c)) {
                if (c == PFConstants.CR && isEOL()) {
                    pdfSource.read();
                }
                break;
            }
            buffer.append( (char)c );
        }
        return buffer.toString();
    }

    protected boolean isEOL() throws IOException {
        return isEOL(pdfSource.peek());
    }

    protected boolean isEOL(int c) {
        return c == PFConstants.LF || c == PFConstants.CR;
    }
}