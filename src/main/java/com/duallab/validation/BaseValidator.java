package com.duallab.validation;

import java.util.List;

import com.duallab.logger.LogLevel;
import com.duallab.logger.Logger;
import com.duallab.validation.validationtask.ValidationTask;

public class BaseValidator implements Validator {

    private Logger logger;
    private Long startOffset;

    public BaseValidator(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void validate(ValidationConfig config) {
        try {
            ValidationTask validationTask = config.getType().getTaskClass().newInstance();
            try {
                List<PDFValidationError> validationErrors = validationTask.validate(config);
                for (PDFValidationError validationError : validationErrors) {
                    logger.log(LogLevel.VALIDATION_ERROR, validationError.toString());
                }
            } catch (Exception e) {
                logger.log(LogLevel.PDF_ERROR, e.getMessage() + validationTask.getClass().getSimpleName());
            }
        } catch (Exception e) {
            logger.log(LogLevel.INTERNAL_ERROR, e.getMessage());
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(Long startOffset) {
        this.startOffset = startOffset;
    }
}
