package org.jenkinsci.plugins.servicenow.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class ServiceNowItem extends AbstractDescribableImpl<ServiceNowItem> {

    private String table;
    private String sysId;
    private String body;
    private String cTask;
    private String filename;
    private String taskDescription;

    @DataBoundConstructor
    public ServiceNowItem(String table, String sysId) {
        this.table = table;
        this.sysId = sysId;
    }

    public String getTable() {
        return table;
    }

    public String getSysId() {
        return sysId;
    }

    @DataBoundSetter
    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    @DataBoundSetter
    public void setcTask(String cTask) {
        this.cTask = cTask;
    }

    public String getcTask() {
        return cTask;
    }

    @DataBoundSetter
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    @DataBoundSetter
    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ServiceNowItem> {
        @Override
        public String getDisplayName() {
            return "ServiceNow Item";
        }
    }
}