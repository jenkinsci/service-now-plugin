package org.jenkinsci.plugins.servicenow.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class VaultConfiguration extends AbstractDescribableImpl<VaultConfiguration> {

    private final String url;
    private final String path;

    @DataBoundConstructor
    public VaultConfiguration(String url, String path) {
        this.url = url;
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<VaultConfiguration> {
        @Override
        public String getDisplayName() {
            return "Vault Configuration";
        }
    }
}