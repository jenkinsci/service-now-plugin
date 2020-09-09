package org.jenkinsci.plugins.servicenow.workflow;

import hudson.model.Item;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import javax.annotation.Nonnull;
import java.io.IOException;

public abstract class AbstractItemProviderExecution<T> extends SynchronousNonBlockingStepExecution<T> { 
     
    private static final long serialVersionUID = 4542334758577818951L;

    protected AbstractItemProviderExecution(@Nonnull StepContext context) {
        super(context);
    }

    Item getProject() throws IOException, InterruptedException {
        return getContext().get(Run.class).getParent();
    }
}
