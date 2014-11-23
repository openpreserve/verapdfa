package com.duallab.validation.validationtask;

import com.duallab.validation.validationtask.impl.FileTrailerValidationTask;
import com.duallab.validation.validationtask.impl.HeaderValidationTask;
import com.duallab.validation.validationtask.impl.IndirectObjectValidationTask;
import com.duallab.validation.validationtask.impl.NoDataAfterEOFValidationTask;
import com.duallab.validation.validationtask.impl.StreamObjectValidationTask;
import com.duallab.validation.validationtask.impl.XrefTableValidationTask;

public enum ValidationTaskType {

    HEADER_VALIDATION_TASK(HeaderValidationTask.class),
    FILE_TRAILER_VALIDATION_TASK(FileTrailerValidationTask.class),
    NO_DATA_AFTER_EOF_VALIDATION_TASK(NoDataAfterEOFValidationTask.class),
    XREF_TABLE_VALIDATION_TASK(XrefTableValidationTask.class),
    STREAM_OBJECT_VALIDATION_TASK(StreamObjectValidationTask.class),
    INDIRECT_OBJECT_VALIDATION_TASK(IndirectObjectValidationTask.class);

    private Class<? extends ValidationTask> taskClass;

    ValidationTaskType(Class<? extends ValidationTask> taskClass) {
        this.taskClass = taskClass;
    }

    public Class<? extends ValidationTask> getTaskClass() {
        return taskClass;
    }
}
