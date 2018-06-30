package org.jenkinsci.plugins.servicenow.util;

public enum ServiceNowStates {
    NEW(-5),
    SCHEDULED(-2),
    IMPLEMENT(-1),
    REVIEW(0),
    IN_PROGRESS(2),
    COMPLETE(3);

    private final int stateInteger;

    ServiceNowStates(int stateInteger) {
        this.stateInteger = stateInteger;
    }

    public int getStateInteger() {
        return this.stateInteger;
    }

    public static ServiceNowStates getState(int i) {
        for (ServiceNowStates state : ServiceNowStates.values()) {
            if (state.getStateInteger() == i) {
                return state;
            }
        }
        return null;
    }

    public static ServiceNowStates getState(String stateName) {
        return ServiceNowStates.valueOf(stateName);
    }
}
