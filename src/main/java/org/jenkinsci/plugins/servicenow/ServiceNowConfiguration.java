package org.jenkinsci.plugins.servicenow;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Run;
import jenkins.plugins.http_request.HttpRequestStep;
import org.apache.commons.codec.binary.Base64;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServiceNowConfiguration extends AbstractDescribableImpl<ServiceNowConfiguration> {

    private String instance;
    private String username;
    private String password;
    private String credentialId;

    @DataBoundConstructor
    public ServiceNowConfiguration(String instance) {
        this.instance = instance;
    }

    public String getInstance() {
        return instance;
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

    public Map<String, String> getAuthorizationHeader() {
        if (username == null) {
            return null;
        }
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Basic " +
                Base64.encodeBase64(
                        String.join(":", username, password).getBytes()
                ).toString());
        return header;
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