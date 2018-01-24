package org.jenkinsci.plugins.servicenow.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.apache.commons.codec.binary.Base64;
import org.jenkinsci.plugins.servicenow.ServiceNowPluginException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.UnsupportedEncodingException;

public class ServiceNowConfiguration extends AbstractDescribableImpl<ServiceNowConfiguration> {

    private String instance;
    private String username;
    private String password;
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

    public String getUsername() {
        return username;
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    @DataBoundSetter
    public void setPassword(String password) {
        this.password = password;
    }

    public String getCredentialId() {
        return credentialId;
    }

    @DataBoundSetter
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getAuthorizationHeader() {
        if (username == null) {
            return null;
        }
        try {
            return "Basic " + new String(Base64.encodeBase64(getAuthBytes()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceNowPluginException("Failed to encode username password to UTF-8");
        }
    }

    private byte[] getAuthBytes() {
        try {
            return String.join(":", username, password).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceNowPluginException("Failed to encode username password to UTF-8");
        }

    }

    public void validate() {
        if ((username == null || password == null) && credentialId == null) {
            throw new ServiceNowPluginException("You must authenticate via username/password or credentialId");
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ServiceNowConfiguration> {
        @Override
        public String getDisplayName() {
            return "ServiceNow Configuration";
        }
    }
}