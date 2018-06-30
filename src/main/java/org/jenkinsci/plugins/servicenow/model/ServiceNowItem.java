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
    private String query;

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

    public String getQuery() {
        return query;
    }

    @DataBoundSetter
    public void setQuery(String query) {
        this.query = query;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ServiceNowItem> {
        @Override
        public String getDisplayName() {
            return "ServiceNow Item";
        }
    }
}