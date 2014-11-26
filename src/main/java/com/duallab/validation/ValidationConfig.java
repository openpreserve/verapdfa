package com.duallab.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.io.PushBackInputStream;
import org.apache.pdfbox.pdfparser.XrefTrailerResolver;

import com.duallab.validation.validationtask.ValidationTaskType;

public class ValidationConfig {

    private ValidationTaskType type;

    // Used by : NoDataAfterEOFValidationTask
    private byte[] buffer;
    // Used by : NoDataAfterEOFValidationTask
    private int bufOff;
    // Used by : HeaderValidationTask, StreamObjectValidationTask,
    // Used by : IndirectObjectValidationTask, XrefTableValidationTask
    private PushBackInputStream stream;
    // Used by : FileTrailerValidationTask, StreamObjectValidationTask
    private List<COSBase> parsedObjects;
    // Used by : FileTrailerValidationTask
    private XrefTrailerResolver xrefTrailerResolver;

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

    public ValidationConfig(COSBase parsedObject, ValidationTaskType type) {
        this.type = type;
        addParsedObject(parsedObject);
    }

    public ValidationConfig(COSBase parsedObject, XrefTrailerResolver xrefTrailerResolver, ValidationTaskType type) {
        this.type = type;
        this.xrefTrailerResolver = xrefTrailerResolver;
        addParsedObject(parsedObject);
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

    public ValidationTaskType getType() {
        return type;
    }

    public void setType(ValidationTaskType type) {
        this.type = type;
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

    public List<COSBase> getParsedObjects() {
        return parsedObjects;
    }

    public void setParsedObjects(List<COSBase> parsedObjects) {
        this.parsedObjects = parsedObjects;
    }

    public XrefTrailerResolver getXrefTrailerResolver() {
        return xrefTrailerResolver;
    }

    public void setXrefTrailerResolver(XrefTrailerResolver xrefTrailerResolver) {
        this.xrefTrailerResolver = xrefTrailerResolver;
    }
}
