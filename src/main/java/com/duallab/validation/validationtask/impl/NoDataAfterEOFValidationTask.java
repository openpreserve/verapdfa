package com.duallab.validation.validationtask.impl;

import com.duallab.utils.PFConstants;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.error.PDFStructureError;
import com.duallab.validation.error.PDFValidationError;
import com.duallab.validation.validationtask.BaseValidationTask;

public class NoDataAfterEOFValidationTask extends BaseValidationTask {

    //this buffer contains last EOF marker
    private byte[] buffer;
    //starting position of last EOF marker in buffer (expected: 0 <= lastEOFMarkerOffset < buffer.length)
    private int lastEOFMarkerOffset;

    public NoDataAfterEOFValidationTask(ValidationConfig config) {
        assert (config.getBuffer().length != 0 && lastEOFMarkerOffset < config.getBufOff()): "For end of file validation pdf buffer and valid bufOffset shall be passed in config";
        buffer = config.getBuffer();
        lastEOFMarkerOffset = config.getBufOff();
    }

    public void validate() throws Exception {
        int expectedFileEnd = lastEOFMarkerOffset + PFConstants.EOF_MARKER.length;
        // allowed file ending: %%EOF[LF|CR|CRLF]
        boolean eofError = false;
        if (expectedFileEnd < buffer.length) {
            if (buffer.length - expectedFileEnd > 2) {
                eofError = true;
            } else if (buffer.length - expectedFileEnd == 2) {
                if (buffer[expectedFileEnd] != PFConstants.CR || buffer[expectedFileEnd + 1] != PFConstants.LF) {
                    eofError = true;
                }
            } else {
                if (buffer[expectedFileEnd] != PFConstants.LF && buffer[expectedFileEnd] != PFConstants.CR) {
                    eofError = true;
                }
            }
        } else if (expectedFileEnd > buffer.length) {
            PDFStructureError error = new PDFStructureError("PDF syntax error");
            errors.add(error);
        }

        if (eofError) {
            PDFValidationError error = new PDFValidationError("No data shall follow the last end-of-file marker except a single optional end-of-line marker. (6.1.3)");
            errors.add(error);
        }
    }
}