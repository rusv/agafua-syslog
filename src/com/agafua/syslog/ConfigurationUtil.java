package com.agafua.syslog;

import java.util.logging.*;

/**
 * The configuration utility bits
 */
public class ConfigurationUtil {

    
    static String getStringPropertyOfLogHandlerClass(
            Class handlerClass,
            String propertyName) {
        String propertyFQName = handlerClass.getName() + "." + propertyName;
        return LogManager.getLogManager().getProperty(propertyFQName);
    }
    
}
