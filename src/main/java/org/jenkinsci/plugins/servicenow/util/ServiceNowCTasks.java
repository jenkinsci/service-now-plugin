package org.jenkinsci.plugins.servicenow.util;

public enum ServiceNowCTasks {
    IMPLEMENT("Implement"),
    UAT_TESTING("UAT Testing Approval"),
    POST_IMPL_TESTING("Post implementation testing");

    private final String description;

    ServiceNowCTasks(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
