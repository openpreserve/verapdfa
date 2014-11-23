package com.duallab.validation.validationtask.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.utils.PFConstants;
import com.duallab.validation.PDFValidationError;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.validationtask.BaseValidationTask;

public class StreamObjectValidationTask extends BaseValidationTask {

    private List<PDFValidationError> errors = new ArrayList<>();
    private COSDictionary streamDictionary;
    private PushBackInputStream pdfSource;

    public List<PDFValidationError> validate(ValidationConfig validationConfig) throws Exception {
        //this field will contain current offset in the pdf source stream at the beginning of validation.
        final long startOffset = validationConfig.getStream().getOffset();
        this.streamDictionary = (COSDictionary) validationConfig.getParsedObjects().get(0);
        this.pdfSource = validationConfig.getStream();

        validateStreamObjectDictionary();

        //there can be whitespaces between stream dictionary and "stream" keyword
        skipSpaces(pdfSource);
        //this line will contain "stream" keyword, already checked in NonSequentialPDFParser.parseObjectsDynamically()
        readUntilEOL(pdfSource);

        int nextByte = pdfSource.read();
        if (nextByte == PFConstants.LF) {
            if (isEOL(pdfSource.peek())) {
                PDFValidationError error = new PDFValidationError("stream keyword shall be followed by a CRLF or a single LF character (6.1.7)");
                errors.add(error);
            }
        } else {
            if (pdfSource.peek() != PFConstants.LF) {
                PDFValidationError error = new PDFValidationError("stream keyword shall not be followed by a single CR character (6.1.7)");
                errors.add(error);
            } else {
                pdfSource.read();
            }
        }

        //if there's multiple eol markers after "stream" keyword we still want to validate length
        skipSpaces(pdfSource);

        COSNumber streamLength = (COSNumber) streamDictionary.getItem(COSName.LENGTH);
        if (streamLength == null) {
            //reset current stream position
            this.pdfSource.seek(startOffset);
            throw new IOException("Missing length for stream.");
        }

        long actualStreamLength = 0;
        int streamByte;
        while (true) {
            streamByte = pdfSource.read();
            if (isEOL(streamByte) || streamByte == -1) {
                break;
            }
            actualStreamLength++;
            if (actualStreamLength > streamLength.longValue()) {
                break;
            }
        }

        if (actualStreamLength != streamLength.longValue()) {
            PDFValidationError error = new PDFValidationError("Actual stream length differs from defined in stream dictionary (6.1.7)");
            errors.add(error);
        } else {
            if (!isEOL(pdfSource.peek())) {
                PDFValidationError error = new PDFValidationError("endstream keyword shall be preceded by EOL marker (6.1.7)");
                errors.add(error);
            }
        }
        //the presence of endstream keyword is required by pdf parser, so we won't check it

        //reset current stream position
        this.pdfSource.seek(startOffset);

        return errors;
    }

    private void validateStreamObjectDictionary() {
        if (streamDictionary.containsKey(COSName.F) ||
                streamDictionary.containsKey(COSName.F_FILTER) ||
                streamDictionary.containsKey(COSName.F_DECODE_PARMS)) {
            PDFValidationError error = new PDFValidationError("A stream object dictionary shall not contain the F, FFilter, or FDecodeParams keys (6.1.7)");
            errors.add(error);
        }
    }

}
