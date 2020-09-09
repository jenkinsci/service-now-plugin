package org.jenkinsci.plugins.servicenow.workflow;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jenkinsci.plugins.servicenow.ResponseContentSupplier;
import org.jenkinsci.plugins.servicenow.ServiceNowExecution;
import org.jenkinsci.plugins.servicenow.credentials.CredentialsLocatorStrategy;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class CreateChangeStep extends AbstractServiceNowStep {
    
    @DataBoundConstructor
    public CreateChangeStep(ServiceNowConfiguration serviceNowConfiguration, String credentialsId) {
        super(serviceNowConfiguration, credentialsId, null);
    }

    @Override
    public StepExecution start(StepContext context) {
        return new Execution(context, this);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        
        @Override
        public String getFunctionName() {
            return Messages.CreateChangeStep_DescriptorImpl_DisplayName();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }
        
        public FormValidation doCheckInstance(@QueryParameter String value) {
            
            if (Util.fixEmptyAndTrim(value) == null) {
                return FormValidation.error(Messages.CreateChangeStep_DescriptorImpl_errors_missingInstance());
            }
 
            return FormValidation.ok();
        }
        
        public FormValidation doCheckCredentialsId(@QueryParameter String value) {
            
            if (Util.fixEmptyAndTrim(value) == null) {
                return FormValidation.error(Messages.CreateChangeStep_DescriptorImpl_errors_missingCredentialsId());
            }
         
            return FormValidation.ok();
        }

        public FormValidation doCheckProducerId(@QueryParameter String value) {
            
            if (Util.fixEmptyAndTrim(value) == null) {
                return FormValidation.error(Messages.CreateChangeStep_DescriptorImpl_errors_missingProducerId());
            }
         
            return FormValidation.ok();
        }
        
        public FormValidation doCheckHttpProxyHost(@QueryParameter String value, @QueryParameter boolean useProxy, @QueryParameter String httpProxyPort) {
            
            if (!useProxy) {
                return FormValidation.ok();
            }
            
            if (useProxy && Util.fixEmptyAndTrim(value) != null) {
                return FormValidation.error(Messages.CreateChangeStep_DescriptorImpl_errors_missingHttpProxyPort());
            }
         
            return FormValidation.ok();
        }
        
    }

    public static final class Execution extends SynchronousNonBlockingStepExecution<ResponseContentSupplier> {

        private transient HttpClientBuilder clientBuilder;
        private transient CreateChangeStep step;
        private transient CredentialsLocatorStrategy credentialsLocatorStrategy;

        Execution(@Nonnull StepContext context, @Nonnull CreateChangeStep step) {
            super(context);
            this.step = step;
        }

        public void setClientBuilder(HttpClientBuilder clientBuilder) {
            this.clientBuilder = clientBuilder;
        }

        public void setCredentialsLocatorStrategy(CredentialsLocatorStrategy credentialsLocatorStrategy) {
            this.credentialsLocatorStrategy = credentialsLocatorStrategy;
        }

        @Override
        public ResponseContentSupplier run() throws Exception {
            ServiceNowExecution exec = null;
            if (clientBuilder == null) {
                exec = ServiceNowExecution.from(step, getProject());
            } else {
                exec = ServiceNowExecution.from(step, getProject(), clientBuilder,
                        credentialsLocatorStrategy);
            }

            CloseableHttpResponse response = exec.createChange();
            return new ResponseContentSupplier(ResponseContentSupplier.ResponseHandle.STRING, response);
        }

        Item getProject() throws IOException, InterruptedException {
            return getContext().get(Run.class).getParent();
        }

        private static final long serialVersionUID = 1L;

    }

}
