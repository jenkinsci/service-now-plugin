package org.jenkinsci.plugins.servicenow.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import hudson.model.Run;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.jenkinsci.plugins.servicenow.ResponseContentSupplier;
import org.jenkinsci.plugins.servicenow.ServiceNowExecution;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.servicenow.model.ServiceNowItem;
import org.jenkinsci.plugins.servicenow.model.StateResult;
import org.jenkinsci.plugins.servicenow.util.ServiceNowStates;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public class GetChangeStateStep extends AbstractServiceNowStep {

    @DataBoundConstructor
    public GetChangeStateStep(ServiceNowConfiguration serviceNowConfiguration, String credentialsId, ServiceNowItem serviceNowItem) {
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
            return "serviceNow_getChangeState";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(Run.class);
        }
    }

    public static final class Execution extends AbstractItemProviderExecution<String> {

        private transient GetChangeStateStep step;

        Execution(@Nonnull StepContext context, @Nonnull GetChangeStateStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected String run() throws Exception {
            ServiceNowExecution exec = ServiceNowExecution.from(step, getProject());

            CloseableHttpResponse response = exec.getChangeState();
            ResponseContentSupplier responseContent = new ResponseContentSupplier(ResponseContentSupplier.ResponseHandle.STRING, response);
            ObjectMapper mapper = new ObjectMapper();
            StateResult stateResult = mapper.readValue(responseContent.getContent(), StateResult.class);
            return ServiceNowStates.getState(stateResult.getState()).name();
        }

        private static final long serialVersionUID = 1L;

    }

}
