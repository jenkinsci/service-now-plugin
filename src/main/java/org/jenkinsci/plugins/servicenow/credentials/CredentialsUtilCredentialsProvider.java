package org.jenkinsci.plugins.servicenow.credentials;

import com.cloudbees.plugins.credentials.Credentials;
import hudson.model.Item;
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration;
import org.jenkinsci.plugins.servicenow.util.CredentialsUtil;

public class CredentialsUtilCredentialsProvider implements CredentialsLocatorStrategy {

    @Override
    public Credentials getCredentials(String baseUrl, String credentialsId, VaultConfiguration vaultConfiguration, Item project) {
        return CredentialsUtil.findCredentials(baseUrl, credentialsId, vaultConfiguration, project);
    }
}
