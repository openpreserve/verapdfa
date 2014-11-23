package com.duallab.validation.validationtask.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.persistence.util.COSObjectKey;
import org.apache.pdfbox.preflight.utils.COSUtils;

import com.duallab.utils.PFConstants;
import com.duallab.validation.PDFValidationError;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.validationtask.BaseValidationTask;

public class FileTrailerValidationTask extends BaseValidationTask {

    private List<PDFValidationError> errors = new ArrayList<>();
    private COSDocument document;

    public List<PDFValidationError> validate(ValidationConfig validationConfig) throws Exception {
        this.document = (COSDocument) validationConfig.getParsedObjects().get(0);
        COSDictionary linearizedDictionary = getLinearizedDictionary(document);
        if (linearizedDictionary != null) {
            checkLinearizedDictionary(document);
        } else {
            checkLastTrailerDictionary(document.getTrailer());
        }

        return errors;
    }

    private COSDictionary getLinearizedDictionary(COSDocument document) {
        List<COSObject> cosObjects = document.getObjects();
        for (COSObject cosObject : cosObjects) {
            COSBase curObj = cosObject.getObject();
            if (curObj instanceof COSDictionary) {
                //trailer dictionary in linearized pdf file shall contain "Linearized" keyword
                if (((COSDictionary) curObj).keySet().contains(COSName.getPDFName(PFConstants.TRAILER_KEY_LINEARIZED))) {
                    return (COSDictionary) curObj;
                }
            }
        }
        return null;
    }

    private void checkLinearizedDictionary(COSDocument document) throws Exception {
        List<COSObject> xrefObjects = document.getObjectsByType(COSName.XREF);

        long minOffset = Long.MAX_VALUE;
        long maxOffset = Long.MIN_VALUE;
        COSDictionary firstTrailerDictionary = null;
        COSDictionary lastTrailerDictionary = null;

        // Search First and Last trailers according to offset position.
        for (COSObject co : xrefObjects) {
            long offset = document.getXrefTable().get(new COSObjectKey(co));
            if (offset < minOffset) {
                minOffset = offset;
                firstTrailerDictionary = (COSDictionary) co.getObject();
            }

            if (offset > maxOffset) {
                maxOffset = offset;
                lastTrailerDictionary = (COSDictionary) co.getObject();
            }
        }
        checkLastTrailerDictionary(lastTrailerDictionary);
        checkIdInLinearizedPDFDictionaries(firstTrailerDictionary, lastTrailerDictionary);
    }

    private void checkLastTrailerDictionary(COSDictionary trailer) {
        boolean idPresent = false;
        for (Object key : trailer.keySet()) {
            if (key instanceof  COSName) {
                String name = ((COSName) key).getName();
                if (name.equals(PFConstants.TRAILER_KEY_ENCRYPT)) {
                    PDFValidationError error = new PDFValidationError("Invalid trailer : the keyword Encrypt shall not be used in the trailer dictionary (6.1.3)");
                    errors.add(error);
                }
                if (name.equals(PFConstants.TRAILER_KEY_ID)) {
                    idPresent = true;
                }
            }
        }
        if (!idPresent) {
            PDFValidationError error = new PDFValidationError("Invalid trailer : the file trailer dictionary shall contain the ID keyword (6.1.3)");
            errors.add(error);
        }
    }

    private void checkIdInLinearizedPDFDictionaries(COSDictionary firstTrailer, COSDictionary lastTrailer) {
        COSBase firstId = firstTrailer.getItem(COSName.ID);
        COSBase lastId = lastTrailer.getItem(COSName.ID);

        boolean bothIdPresents = true;
        if (firstId == null || lastId == null) {
            PDFValidationError error = new PDFValidationError("In a linearized file the ID keyword shall be present in both the first page trailer and the last trailer dictionaries (6.1.3)");
            errors.add(error);
            bothIdPresents = false;
        }

        if (bothIdPresents) {
            COSArray firstIdArray = COSUtils.getAsArray(firstId, document);
            COSArray lastIdArray = COSUtils.getAsArray(lastId, document);

            //The value of ID entry shall be an array of two byte strings. (pdf spec 14.4)
            if (!firstIdArray.get(0).equals(lastIdArray.get(0)) || firstIdArray.get(1).equals(lastIdArray.get(1))) {
                PDFValidationError error = new PDFValidationError("In a linearized file the ID values in first and last trailer dictionaries must be equal (6.1.3)");
                errors.add(error);
            }
        }
    }
}