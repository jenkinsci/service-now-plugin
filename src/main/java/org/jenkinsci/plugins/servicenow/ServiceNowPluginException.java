package org.jenkinsci.plugins.servicenow;

public class ServiceNowPluginException extends RuntimeException {
    public ServiceNowPluginException(String message) {
        super(message);
    }

    public ServiceNowPluginException(String message, Throwable e) {
        super(message, e);
    }

}
