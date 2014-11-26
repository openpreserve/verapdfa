package com.duallab.validation.validationtask.impl;

import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.utils.PFConstants;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.error.PDFValidationError;
import com.duallab.validation.validationtask.BaseValidationTask;

public class XrefTableValidationTask extends BaseValidationTask {

    private PushBackInputStream pdfSource;
    //this field will contain current offset in the pdf source stream at the beginning of validation.
    private long startOffset;

    public XrefTableValidationTask(ValidationConfig config) {
        assert (config.getStream() != null): "For xref table validation pdf stream shall be passed in config";
        startOffset = config.getStream().getOffset();
        pdfSource = config.getStream();
    }

    public void validate() throws Exception {
        //presence of "xref" keyword was already checked in PDFParser.parseXrefTable
        //allowed string: xref(LF|CR|CRLF)~(LF|CR)
        if (!verifySingleReadEOLMarker(pdfSource)) {
            //if there's multiple eols after xref keyword we still want to validate table contents
            skipEOLs(pdfSource);
            PDFValidationError error = new PDFValidationError("The xref keyword and the cross reference subsection header shall be separated by a single EOL marker (6.1.4)");
            errors.add(error);
        }

        //TODO: Check pdf spec
        skipSpaces(pdfSource);
        int nextByte = pdfSource.peek();
        while (nextByte >= '0' && nextByte <= '9') {
            long firstObjectId = readLong(pdfSource, startOffset);
            if ((pdfSource.read() != PFConstants.SPACE) || (pdfSource.peek() == PFConstants.SPACE)) {
                PDFValidationError error = new PDFValidationError("In a cross reference subsection header the starting object number and the range shall be separated by a single SPACE character (6.1.4)");
                errors.add(error);
            }
            long range = readLong(pdfSource, startOffset);
            skipSpaces(pdfSource);
            //TODO: check pdf spec
            for (int i = 0; i < range; i++) {
                readLine(pdfSource);
            }
            nextByte = pdfSource.peek();
        }
    }

    public void cleanup() throws Exception {
        //reset stream position
        pdfSource.seek(startOffset);
    }
}
