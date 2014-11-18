package com.duallab.validation;

import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.validation.validationtask.ValidationTaskType;

public class ValidationConfig {

    private PushBackInputStream stream;
    private ValidationTaskType type;
    private List<COSBase> parsedObjects;

    public ValidationConfig() {
    }

    public ValidationConfig(PushBackInputStream stream, ValidationTaskType type) {
        this.stream = stream;
        this.type = type;
    }

    public PushBackInputStream getStream() {
        return stream;
    }

    public void setStream(PushBackInputStream stream) {
        this.stream = stream;
    }

    public ValidationTaskType getType() {
        return type;
    }

    public void setType(ValidationTaskType type) {
        this.type = type;
    }

    public List<COSBase> getParsedObjects() {
        return parsedObjects;
    }

    public void setParsedObjects(List<COSBase> parsedObjects) {
        this.parsedObjects = parsedObjects;
    }
}
