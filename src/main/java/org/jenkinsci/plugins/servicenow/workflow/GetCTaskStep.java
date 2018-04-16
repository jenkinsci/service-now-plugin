package org.jenkinsci.plugins.servicenow.workflow;

import hudson.Extension;
import hudson.model.Run;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.jenkinsci.plugins.servicenow.ResponseContentSupplier;
import org.jenkinsci.plugins.servicenow.ServiceNowExecution;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.servicenow.model.ServiceNowItem;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
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
            return Collections.singleton(Run.class);
        }
    }

    public static final class Execution extends AbstractItemProviderExecution<ResponseContentSupplier> {

        private transient GetCTaskStep step;

        Execution(@Nonnull StepContext context, @Nonnull GetCTaskStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected ResponseContentSupplier run() throws Exception {
            ServiceNowExecution exec = ServiceNowExecution.from(step, getProject());

            CloseableHttpResponse response = exec.getCTask();
            return new ResponseContentSupplier(ResponseContentSupplier.ResponseHandle.STRING, response);
        }

        private static final long serialVersionUID = 1L;

    }

}
