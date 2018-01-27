package org.jenkinsci.plugins.servicenow.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Run;
import jenkins.plugins.http_request.ResponseHandle;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.jenkinsci.plugins.servicenow.ResponseContentSupplier;
import org.jenkinsci.plugins.servicenow.ServiceNowExecution;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.servicenow.model.ServiceNowItem;
import org.jenkinsci.plugins.servicenow.model.StateResult;
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration;
import org.jenkinsci.plugins.servicenow.util.ServiceNowStates;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class GetCTaskStep extends AbstractServiceNowStep {

    @DataBoundConstructor
    public GetCTaskStep(ServiceNowConfiguration serviceNowConfiguration, String credentialsId, ServiceNowItem serviceNowItem) {
        super(serviceNowConfiguration, credentialsId, serviceNowItem);
    }

    @Override
    public StepExecution start(StepContext context) {
        return new Execution(context, this);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "serviceNow_getCTask";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }
    }

    public static final class Execution extends SynchronousNonBlockingStepExecution<ResponseContentSupplier> {

        private transient GetCTaskStep step;

        Execution(@Nonnull StepContext context, @Nonnull GetCTaskStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected ResponseContentSupplier run() throws Exception {
            ServiceNowExecution exec = ServiceNowExecution.from(step, getProject());

            CloseableHttpResponse response = exec.getCTask();
            return new ResponseContentSupplier(ResponseHandle.STRING, response);
        }

        Item getProject() throws IOException, InterruptedException {
            return getContext().get(Run.class).getParent();
        }

        private static final long serialVersionUID = 1L;

    }

}