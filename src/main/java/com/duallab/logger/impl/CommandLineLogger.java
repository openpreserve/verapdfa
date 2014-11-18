package com.duallab.logger.impl;

import com.duallab.logger.BaseLogger;
import com.duallab.logger.LogLevel;

public class CommandLineLogger extends BaseLogger {

    @Override
    public void log(LogLevel logLevel, String message) {
        System.out.println(logLevel.toString() + " : " + message);
    }

}
