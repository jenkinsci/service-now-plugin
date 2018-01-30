package org.jenkinsci.plugins.servicenow.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.apache.commons.codec.binary.Base64;
import org.jenkinsci.plugins.servicenow.ServiceNowPluginException;
import org.jenkinsci.plugins.servicenow.util.ServiceNowCTasks;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ServiceNowConfiguration extends AbstractDescribableImpl<ServiceNowConfiguration> {

    private static final String PRODUCER_URI = "api/sn_sc/servicecatalog/items";
    private static final String TABLE_API = "api/now/table";
    private static final String ATTACHMENT_API = "api/now/attachment/file";


    private String instance;
    private String credentialId;
    private String producerId;

    @DataBoundConstructor
    public ServiceNowConfiguration(String instance) {
        this.instance = instance;
    }

    public String getInstance() {
        return instance;
    }

    public String getProducerId() {
        return producerId;
    }

    @DataBoundSetter
    public void setProducerId(String producerId) {
        this.producerId = producerId;
    }

    public String getCredentialId() {
        return credentialId;
    }

    @DataBoundSetter
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getAttachmentUrl(ServiceNowItem serviceNowItem) {
        return getBaseUrl(getInstance())+"/"+ATTACHMENT_API+"?file_name="+serviceNowItem.getFilename()+"&table_name="+serviceNowItem.getTable()+"&table_sys_id="+serviceNowItem.getSysId();
    }

    public String getCTasksUrl(ServiceNowItem serviceNowItem) throws UnsupportedEncodingException {
        return getBaseUrl(getInstance())+"/"+TABLE_API+"/change_task?change_request="+ serviceNowItem.getSysId()+"&short_description="+ URLEncoder.encode(ServiceNowCTasks.valueOf(serviceNowItem.getcTask()).getDescription(), "UTF-8");
    }

    public String getCurrentStateUrl(String sysId) {
        return getBaseUrl(getInstance())+"/"+TABLE_API+"/change_request/"+ sysId+"?sysparm_fields=state";
    }

    public String getPatchUrl(ServiceNowItem serviceNowItem) {
        return getBaseUrl(getInstance())+"/"+TABLE_API+"/"+ serviceNowItem.getTable()+"/"+ serviceNowItem.getSysId();
    }

    public String getProducerRequestUrl() {
        return getBaseUrl(getInstance())+"/"+PRODUCER_URI+"/"+getProducerId()+"/submit_producer";
    }

    public String getBaseUrl(String instance) {
        return "https://" + instance + ".service-now.com";
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ServiceNowConfiguration> {
        @Override
        public String getDisplayName() {
            return "ServiceNow Configuration";
        }
    }
}