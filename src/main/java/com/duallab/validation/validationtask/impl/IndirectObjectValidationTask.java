package com.duallab.validation.validationtask.impl;

import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.utils.PFConstants;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.error.PDFStructureError;
import com.duallab.validation.error.PDFValidationError;
import com.duallab.validation.validationtask.BaseValidationTask;

public class IndirectObjectValidationTask extends BaseValidationTask {

    private PushBackInputStream pdfSource;
    //this field will contain current offset in the pdf source stream at the beginning of validation.
    private long startOffset;

    public IndirectObjectValidationTask(ValidationConfig config) {
        assert (config.getStream() != null): "For indirect object validation pdf stream shall be passed in config";
        pdfSource = config.getStream();
        startOffset = config.getStream().getOffset();
    }

    public void validate() throws Exception {
        //multiple eol markers can precede object number but we need to check only one
        //TODO: decide what to do with incorrect xref table offsets
        skipEOLs(pdfSource);
        long curPosition = pdfSource.getOffset();
        pdfSource.seek(curPosition-1);
        if (!isByteEOL(pdfSource.read())) {
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
        String objKeyword = readUntilWhitespace(pdfSource);
        if (!objKeyword.equals(PFConstants.OBJ_KEYWORD)) {
            PDFStructureError error = new PDFStructureError("obj keyword is expected after object generation number");
            errors.add(error);
        }

        if (!isByteEOL(pdfSource.peek())) {
            PDFValidationError error = new PDFValidationError("The obj keyword shall be followed by an EOL marker. (6.1.8)");
            errors.add(error);
        }

        //TODO: decide how to check for "endobj" keyword
    }

    public void cleanup() throws Exception {
        //reset stream position
        pdfSource.seek(startOffset);
    }
}
