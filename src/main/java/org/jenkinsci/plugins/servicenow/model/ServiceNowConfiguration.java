package org.jenkinsci.plugins.servicenow.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

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
    private boolean useProxy;
    private String httpProxyHost;
    private int httpProxyPort;

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

    public boolean isUseProxy() {
        return useProxy;
    }

    @DataBoundSetter
    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    @DataBoundSetter
    public void setHttpProxyHost(String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
    }

    public int getHttpProxyPort() {
        return httpProxyPort;
    }

    @DataBoundSetter
    public void setHttpProxyPort(int httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }
    
    public String getAttachmentUrl(ServiceNowItem serviceNowItem) {
        return getBaseUrl()+"/"+ATTACHMENT_API+"?file_name="+serviceNowItem.getFilename()+"&table_name="+serviceNowItem.getTable()+"&table_sys_id="+serviceNowItem.getSysId();
    }

    public String getCTasksUrl(ServiceNowItem serviceNowItem) throws UnsupportedEncodingException {
        String base = getBaseUrl()+"/"+TABLE_API+"/change_task?change_request="+ serviceNowItem.getSysId();
        if (serviceNowItem.getcTask() != null) {
            base += "&sysparm_query=short_descriptionLIKE" + URLEncoder.encode(serviceNowItem.getcTask(), "UTF-8");
        } else if (serviceNowItem.getQuery() != null) {
            base += "&sysparm_query=" + URLEncoder.encode(serviceNowItem.getQuery());
        }
        return base;
    }

    public String getCurrentStateUrl(String sysId) {
        return getBaseUrl()+"/"+TABLE_API+"/change_request/"+ sysId+"?sysparm_fields=state";
    }

    public String getPatchUrl(ServiceNowItem serviceNowItem) {
        return getBaseUrl()+"/"+TABLE_API+"/"+ serviceNowItem.getTable()+"/"+ serviceNowItem.getSysId();
    }

    public String getProducerRequestUrl() {
        return getBaseUrl()+"/"+PRODUCER_URI+"/"+getProducerId()+"/submit_producer";
    }

    public String getBaseUrl() {
        return "https://" + getInstance() + ".service-now.com";
    }


    @Extension
    public static class DescriptorImpl extends Descriptor<ServiceNowConfiguration> {
        @Override
        public String getDisplayName() {
            return "ServiceNow Configuration";
        }
    }
}