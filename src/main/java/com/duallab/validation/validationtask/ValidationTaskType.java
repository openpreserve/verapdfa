package com.duallab.validation.validationtask;

import com.duallab.validation.validationtask.impl.HeaderValidationTask;

public enum ValidationTaskType {

    HEADER_VALIDATION_TASK(HeaderValidationTask.class);

    private Class<? extends ValidationTask> taskClass;

    ValidationTaskType(Class<? extends ValidationTask> taskClass) {
        this.taskClass = taskClass;
    }

    public Class<? extends ValidationTask> getTaskClass() {
        return taskClass;
    }
}
