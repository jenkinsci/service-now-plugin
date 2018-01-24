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

    @Extension
    public static class DescriptorImpl extends Descriptor<ServiceNowItem> {
        @Override
        public String getDisplayName() {
            return "ServiceNow Item";
        }
    }
}