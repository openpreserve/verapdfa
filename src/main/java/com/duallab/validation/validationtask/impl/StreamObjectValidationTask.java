package com.duallab.validation.validationtask.impl;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.utils.PFConstants;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.error.PDFStructureError;
import com.duallab.validation.error.PDFValidationError;
import com.duallab.validation.validationtask.BaseValidationTask;

public class StreamObjectValidationTask extends BaseValidationTask {

    private COSDictionary streamDictionary;
    private PushBackInputStream pdfSource;
    //this field will contain current offset in the pdf source stream at the beginning of validation.
    private long startOffset;

    public StreamObjectValidationTask(ValidationConfig config) {
        assert (config.getStream() != null &&
                config.getParsedObjects().size() == 1 &&
                config.getParsedObjects().get(0) instanceof COSDictionary):
                "For stream object validation pdf stream and parsed stream dictionary shall be passed in config";
        pdfSource = config.getStream();
        startOffset = config.getStream().getOffset();
        streamDictionary = (COSDictionary) config.getParsedObjects().get(0);
    }

    public void validate() throws Exception {
        //we don't want to validate xref streams during pdf/a-1 validation because they didn't exist at the moment pdf/a-1 spec was created (in pdf v1.4 or below)
        if (streamDictionary.containsKey(COSName.TYPE) && streamDictionary.getNameAsString(COSName.TYPE).equals(PFConstants.XREF_KEYWORD)) {
            return;
        }

        validateStreamObjectDictionary();

        //there can be whitespaces between stream dictionary and "stream" keyword
        skipSpaces(pdfSource);
        //this line will contain "stream" keyword, already checked in NonSequentialPDFParser.parseObjectsDynamically()
        readUntilWhitespace(pdfSource);

        boolean wrongEOL = false;
        int nextByte = pdfSource.read();
        if (isByteEOL(nextByte)) {
            if (nextByte == PFConstants.LF) {
                if (isByteEOL(pdfSource.peek())) {
                    wrongEOL = true;
                }
            } else {
                if (pdfSource.peek() != PFConstants.LF) {
                    wrongEOL = true;
                } else {
                    pdfSource.read();
                    if (isByteEOL(pdfSource.peek())) {
                        wrongEOL = true;
                    }
                }
            }
        } else {
            wrongEOL = true;
        }

        if (wrongEOL) {
            PDFValidationError error = new PDFValidationError("stream keyword shall be followed by a CRLF or a single LF character (6.1.7)");
            errors.add(error);
        }

        COSNumber streamLength = (COSNumber) streamDictionary.getItem(COSName.LENGTH);
        if (streamLength == null) {
            PDFStructureError error = new PDFStructureError("Missing length for stream.");
            errors.add(error);
            return;
        }

        //skip to expected stream end
        pdfSource.seek(pdfSource.getOffset() + streamLength.longValue());
        boolean incorrectStreamLength = false;
        //allowed stream ending: (LR|CR|CRLF)endstream
        if (!verifySingleReadEOLMarker(pdfSource)) {
            incorrectStreamLength = true;
        } else {
            //read endstream keyword
            if (!readUntilWhitespace(pdfSource).equals(PFConstants.ENDSTREAM_KEYWORD)) {
                incorrectStreamLength = true;
            }
        }

        if (incorrectStreamLength) {
            PDFValidationError error = new PDFValidationError("Actual stream length differs from defined in stream dictionary (6.1.7)");
            errors.add(error);
        }
        //the presence of endstream keyword is required by pdf parser, so we won't check it
    }

    public void cleanup() throws Exception{
        //reset stream position
        pdfSource.seek(startOffset);
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
