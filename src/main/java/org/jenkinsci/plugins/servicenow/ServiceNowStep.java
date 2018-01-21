package org.jenkinsci.plugins.servicenow;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collections;
import java.util.Set;

public class ServiceNowStep extends Step {

    @DataBoundConstructor
    public ServiceNowStep() {
    }


    @Override
    public StepExecution start(StepContext context) throws Exception {
        return null;
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "serviceNow";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }
    }
}
