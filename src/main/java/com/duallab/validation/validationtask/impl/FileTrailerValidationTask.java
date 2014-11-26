package com.duallab.validation.validationtask.impl;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.XrefTrailerResolver;
import org.apache.pdfbox.preflight.utils.COSUtils;

import com.duallab.utils.PFConstants;
import com.duallab.validation.ValidationConfig;
import com.duallab.validation.error.PDFStructureError;
import com.duallab.validation.error.PDFValidationError;
import com.duallab.validation.validationtask.BaseValidationTask;

public class FileTrailerValidationTask extends BaseValidationTask {

    private COSDocument document;
    //xref resolver contains all parsed trailers and provides convenient getFirstTrailer() and getLastTrailer() methods
    private XrefTrailerResolver xrefTrailerResolver;

    public FileTrailerValidationTask(ValidationConfig config) {
        assert (config.getXrefTrailerResolver() != null &&
                config.getParsedObjects().size() == 1 &&
                config.getParsedObjects().get(0) instanceof COSDocument):
                "For file trailer validation parsed document and xref trailer resolver shall be passed in config";
        document = (COSDocument) config.getParsedObjects().get(0);
        xrefTrailerResolver = config.getXrefTrailerResolver();
    }

    public void validate() throws Exception {
        //using xrefTrailerResolver as the workaround instead of isLinearizedPDF for the moment
        //for some reason pdfbox doesn't include linearization dictionary in parsed document
        //so in order to use isLinearizedPDF modification of some methods is required (like in PreflightParser)
        if (!xrefTrailerResolver.getFirstTrailer().equals(xrefTrailerResolver.getLastTrailer())) {
            checkLinearizedPDFTrailerDictionaries();
        } else {
            checkLastTrailerDictionary(document.getTrailer());
        }
    }

    //searching for at least on dictionary containing "Linearized" keyword
    //this means we deal with linearized pdf file
    //this method is temporary commented because pdfbox doesn't store linearization dictionary in parsed document
    /*private boolean isLinearizedPDF() {
        List<COSObject> cosObjects = document.getObjects();
        for (COSObject cosObject : cosObjects) {
            COSBase curObj = cosObject.getObject();
            if (curObj instanceof COSDictionary) {
                //trailer dictionary in linearized pdf file shall contain "Linearized" keyword
                if (((COSDictionary) curObj).keySet().contains(COSName.getPDFName(PFConstants.LINEARIZED_KEYWORD))) {
                    return true;
                }
            }
        }
        return false;
    }*/

    //now only for linearized pdf version 1.4 and below
    private void checkLinearizedPDFTrailerDictionaries() throws Exception {
        COSDictionary firstTrailerDictionary = xrefTrailerResolver.getFirstTrailer();
        COSDictionary lastTrailerDictionary = xrefTrailerResolver.getLastTrailer();
        if (firstTrailerDictionary != null && lastTrailerDictionary != null) {
            checkLastTrailerDictionary(lastTrailerDictionary);
            checkIdInLinearizedPDFTrailerDictionaries(firstTrailerDictionary, lastTrailerDictionary);
        } else {
            PDFStructureError error = new PDFStructureError("Corrupted linearized pdf file");
            errors.add(error);
        }
    }

    private void checkLastTrailerDictionary(COSDictionary trailer) {
        boolean idPresent = false;
        boolean encryptPresent = false;
        for (Object key : trailer.keySet()) {
            if (key instanceof COSName) {
                String name = ((COSName) key).getName();
                if (name.equals(PFConstants.TRAILER_KEY_ENCRYPT)) {
                    encryptPresent = true;
                }
                if (name.equals(PFConstants.TRAILER_KEY_ID)) {
                    idPresent = true;
                }
                if (idPresent && encryptPresent) {
                    break;
                }
            }
        }
        if (encryptPresent) {
            PDFValidationError error = new PDFValidationError("The keyword Encrypt shall not be used in the trailer dictionary (6.1.3)");
            errors.add(error);
        }
        if (!idPresent) {
            PDFValidationError error = new PDFValidationError("The file trailer dictionary shall contain the ID keyword (6.1.3)");
            errors.add(error);
        }
    }

    private void checkIdInLinearizedPDFTrailerDictionaries(COSDictionary firstTrailerDictionary, COSDictionary lastTrailerDictionary) {
        COSBase firstId = firstTrailerDictionary.getItem(COSName.ID);
        COSBase lastId = lastTrailerDictionary.getItem(COSName.ID);

        if (firstId == null || lastId == null) {
            PDFValidationError error = new PDFValidationError("In a linearized file the ID keyword shall be present in both the first page trailer and the last trailer dictionaries (6.1.3)");
            errors.add(error);
        } else {
            COSArray firstIdArray = COSUtils.getAsArray(firstId, document);
            COSArray lastIdArray = COSUtils.getAsArray(lastId, document);

            //The value of ID entry shall be an array of two byte strings. (pdf spec 14.4)
            if (firstIdArray.size() != 2 || lastIdArray.size() != 2) {
                PDFStructureError error = new PDFStructureError("Invalid ID");
                errors.add(error);
            } else if (!firstIdArray.get(0).equals(lastIdArray.get(0)) || !firstIdArray.get(1).equals(lastIdArray.get(1))) {
                PDFValidationError error = new PDFValidationError("In a linearized file the ID values in first and last trailer dictionaries must be equal (6.1.3)");
                errors.add(error);
            }
        }
    }
}