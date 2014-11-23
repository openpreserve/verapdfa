package com.duallab.validation.validationtask.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.utils.PFConstants;
import com.duallab.validation.PDFValidationError;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.validationtask.BaseValidationTask;

public class XrefTableValidationTask extends BaseValidationTask {

    private List<PDFValidationError> errors = new ArrayList<>();
    private PushBackInputStream pdfSource;

    public List<PDFValidationError> validate (ValidationConfig validationConfig) throws Exception {
        //this field will contain current offset in the pdf source stream at the beginning of validation.
        final long startOffset = validationConfig.getStream().getOffset();
        this.pdfSource = validationConfig.getStream();

        //presence of "xref" keyword was already checked in PDFParser.parseXrefTable
        boolean wrongEOL = false;
        int nextByte = pdfSource.read();
        if (!isEOL(nextByte)) {
            wrongEOL = true;
            pdfSource.unread(nextByte);
        } else {
            if (nextByte == PFConstants.LF && isEOL(pdfSource.peek())) {
                wrongEOL = true;
            } else if (nextByte == PFConstants.CR) {
                nextByte = pdfSource.read();
                if (nextByte == PFConstants.LF) {
                    if (isEOL(pdfSource.peek())) {
                        wrongEOL = true;
                    }
                } else if (nextByte == PFConstants.CR) {
                    wrongEOL = true;
                } else {
                    pdfSource.unread(nextByte);
                }
            }
        }
        if (wrongEOL) {
            PDFValidationError error = new PDFValidationError("The xref keyword and the cross reference subsection header shall be separated by a single EOL marker (6.1.4)");
            errors.add(error);
        }

        while (true) {
            long firstObjectId = readLong(pdfSource, startOffset);
            if ((pdfSource.read() != PFConstants.SPACE) || (pdfSource.peek() == PFConstants.SPACE)) {
                PDFValidationError error = new PDFValidationError("In a cross reference subsection header the starting object number and the range shall be separated by a single SPACE character (6.1.4)");
                errors.add(error);
            }
            long range = readLong(pdfSource, startOffset);
            skipSpaces(pdfSource);
            for (int i = 0; i < range; i++) {
                readLine(pdfSource);
            }
            nextByte = pdfSource.peek();
            if (nextByte < '0' || nextByte > '9') {
                break;
            }
        }

        //reset current stream position
        this.pdfSource.seek(startOffset);

        return errors;
    }

}
