package com.duallab.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.io.PushBackInputStream;

import com.duallab.validation.validationtask.ValidationTaskType;

public class ValidationConfig {

    private byte[] buffer;
    private int bufOff;
    private PushBackInputStream stream;
    private ValidationTaskType type;
    private List<COSBase> parsedObjects;

    public ValidationConfig() {
    }

    public ValidationConfig(byte[] buffer, int bufOff, ValidationTaskType type) {
        this.buffer = buffer;
        this.bufOff = bufOff;
        this.type = type;
    }

    public ValidationConfig(PushBackInputStream stream, ValidationTaskType type) {
        this.stream = stream;
        this.type = type;
    }

    public ValidationConfig(PushBackInputStream stream, COSBase parsedObject, ValidationTaskType type) {
        this.stream = stream;
        this.type = type;
        addParsedObject(parsedObject);
    }

    public void addParsedObject(COSBase cosBase) {
        if (this.parsedObjects == null) {
            parsedObjects = new ArrayList<>();
        }

        parsedObjects.add(cosBase);
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public int getBufOff() {
        return bufOff;
    }

    public void setBufOff(int bufOff) {
        this.bufOff = bufOff;
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
