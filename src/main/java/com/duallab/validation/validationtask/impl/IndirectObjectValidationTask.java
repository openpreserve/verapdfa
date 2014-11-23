package com.duallab.validation.validationtask.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.utils.PFConstants;
import com.duallab.validation.PDFValidationError;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.validationtask.BaseValidationTask;

public class IndirectObjectValidationTask extends BaseValidationTask {

    private List<PDFValidationError> errors = new ArrayList<>();
    private PushBackInputStream pdfSource;

    public List<PDFValidationError> validate(ValidationConfig validationConfig) throws Exception {
        //this field will contain current offset in the pdf source stream at the beginning of validation.
        final long startOffset = validationConfig.getStream().getOffset();
        this.pdfSource = validationConfig.getStream();

        //multiple eol markers can precede object number but we need to check only one
        skipEOLs(pdfSource);
        long curPosition = pdfSource.getOffset();
        pdfSource.seek(curPosition-1);
        if (!isEOL(pdfSource.read())) {
            PDFValidationError error = new PDFValidationError("The object number shall each be preceded by an EOL marker. (6.1.8)");
            errors.add(error);
        }

        //read object number
        readLong(pdfSource, startOffset);
        if (!isWhitespace(pdfSource.read()) || (pdfSource.peek() == PFConstants.SPACE)) {
            PDFValidationError error = new PDFValidationError("The object number and generation number shall be separated by a single white-space character. (6.1.8)");
            errors.add(error);
        }
        //read object generation
        readLong(pdfSource, startOffset);
        if (!isWhitespace(pdfSource.read()) || (pdfSource.peek() == PFConstants.SPACE)) {
            PDFValidationError error = new PDFValidationError("The generation number and obj keyword shall be separated by a single white-space character. (6.1.8)");
            errors.add(error);
        }
        skipSpaces(pdfSource);
        String objKeyword = readUntilEOL(pdfSource);
        if (!objKeyword.equals(PFConstants.OBJ_KEYWORD)) {
            //reset current stream position
            this.pdfSource.seek(startOffset);
            throw new IOException("obj keyword is expected after object generation");
        }

        if (!isEOL(pdfSource.peek())) {
            PDFValidationError error = new PDFValidationError("The obj keyword shall be followed by an EOL marker. (6.1.8)");
            errors.add(error);
        }

        //reset current stream position
        this.pdfSource.seek(startOffset);
        return errors;
    }

}
