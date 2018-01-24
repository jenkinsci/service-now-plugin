package org.jenkinsci.plugins.servicenow.model;

import java.util.Map;

public class StateResult {
    private Map<String, Integer> result;

    public Map<String, Integer> getResult() {
        return result;
    }

    public void setResult(Map<String, Integer> result) {
        this.result = result;
    }

    public int getState() {
        return this.result.get("state");
    }
}
