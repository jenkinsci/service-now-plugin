package org.jenkinsci.plugins.servicenow.util;

public enum FortifyResults {
    NO_PROBLEMS("Fortify scans have been run successfully against the code being deployed and have revealed no problems."),
    DEVIATION_IN_PLACE("Fortify scans have revealed problems but there are approved security deviations in place for the issues."),
    NOT_APPLICABLE("Fortify scans are not applicable to this change so have not been run.");

    private final String description;

    FortifyResults(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
