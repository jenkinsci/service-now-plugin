package org.jenkinsci.plugins.servicenow;

import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.servicenow.workflow.CreateChangeStep;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.Test;
import org.mockito.Mockito;

public class CreateChangeStepTest {

    private StepContext jenkinsStepContext = Mockito.mock(StepContext.class);

    @Test
    public void validateHTTPCallForCreateChange() {
        CreateChangeStep step = new CreateChangeStep(new ServiceNowConfiguration("test"), "abc123");
        step.start(jenkinsStepContext);
    }
}
