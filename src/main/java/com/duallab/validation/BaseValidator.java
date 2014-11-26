package com.duallab.validation;

import com.duallab.logger.LogLevel;
import com.duallab.logger.Logger;
import com.duallab.validation.error.PDFError;
import com.duallab.validation.error.PDFStructureError;
import com.duallab.validation.error.PDFValidationError;
import com.duallab.validation.validationtask.ValidationTask;

public class BaseValidator implements Validator {

    private Logger logger;

    public BaseValidator(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void validate(ValidationConfig config) {
        try {
            ValidationTask validationTask = config.getType().getTaskClass().getConstructor(ValidationConfig.class).newInstance(config);
            try {
                validationTask.validate();
                if (validationTask.getErrors() != null) {
                    for (PDFError validationError : validationTask.getErrors()) {
                        if (validationError instanceof PDFValidationError) {
                            logger.log(LogLevel.VALIDATION_ERROR, validationError.toString());
                        } else if (validationError instanceof PDFStructureError) {
                            logger.log(LogLevel.PDF_STRUCTURE_ERROR, validationError.toString());
                        } else {
                            logger.log(LogLevel.INTERNAL_ERROR, validationError.toString());
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(LogLevel.INTERNAL_ERROR, e.getMessage() + " thrown by : " + validationTask.getClass().getSimpleName());
            } finally {
                //clean up
                validationTask.cleanup();
            }
        } catch (Exception e) {
            logger.log(LogLevel.INTERNAL_ERROR, "Couldn't create instance of validation task: " + e.getCause().getMessage());
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
