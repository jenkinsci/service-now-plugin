package org.jenkinsci.plugins.servicenow.credentials;

import com.cloudbees.plugins.credentials.Credentials;
import hudson.model.Item;
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration;

public interface CredentialsLocatorStrategy {
    Credentials getCredentials(String baseUrl, String credentialsId,
                               VaultConfiguration vaultConfiguration, Item project);
}
