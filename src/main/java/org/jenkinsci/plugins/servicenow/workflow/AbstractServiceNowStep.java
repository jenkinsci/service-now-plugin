package org.jenkinsci.plugins.servicenow.workflow;

import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.servicenow.model.ServiceNowItem;
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.kohsuke.stapler.DataBoundSetter;

public abstract class AbstractServiceNowStep extends Step {

    private final ServiceNowConfiguration serviceNowConfiguration;
    private final String credentialsId;
    private final ServiceNowItem serviceNowItem;

    @DataBoundSetter
    public VaultConfiguration vaultConfiguration;


    AbstractServiceNowStep(ServiceNowConfiguration serviceNowConfiguration, String credentialsId, ServiceNowItem serviceNowItem) {
        this.serviceNowConfiguration = serviceNowConfiguration;
        if (credentialsId != null) {
            this.credentialsId = credentialsId;
        } else {
            this.credentialsId = serviceNowConfiguration.getCredentialId();
        }
        this.serviceNowItem = serviceNowItem;
    }

    public ServiceNowConfiguration getServiceNowConfiguration() {
        return serviceNowConfiguration;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public ServiceNowItem getServiceNowItem() {
        return serviceNowItem;
    }
}
