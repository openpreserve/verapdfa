package com.duallab.runner;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.pdfbox.pdfparser.NonSequentialPDFParser;

import com.duallab.logger.LogLevel;
import com.duallab.logger.Logger;
import com.duallab.logger.impl.CommandLineLogger;
import com.duallab.validation.BaseValidator;
import com.duallab.validation.Validator;

public class ValidationRunner {

    //pass path to pdf file as args param
    public static void main(String[] args) throws Exception {
        Logger logger = new CommandLineLogger();
        Validator validator = new BaseValidator(logger);
        InputStream pdfSource = new FileInputStream(args[0]);
        NonSequentialPDFParser pdfParser = new NonSequentialPDFParser(pdfSource);
        pdfParser.setValidator(validator);
        pdfParser.parse();
        logger.log(LogLevel.INFO, "Validation completed");
    }

}
