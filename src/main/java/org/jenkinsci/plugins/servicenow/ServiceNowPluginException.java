package org.jenkinsci.plugins.servicenow;

public class ServiceNowPluginException extends RuntimeException {
    public ServiceNowPluginException(String message) {
        super(message);
    }
}
