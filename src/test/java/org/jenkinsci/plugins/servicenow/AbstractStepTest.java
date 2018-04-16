package org.jenkinsci.plugins.servicenow;

import hudson.model.Job;
import hudson.model.Run;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public abstract class AbstractStepTest {
    protected StepContext jenkinsStepContext = Mockito.mock(StepContext.class);
    protected HttpClientBuilder httpClientBuilder = Mockito.mock(HttpClientBuilder.class);
    protected Run mockRun = Mockito.mock(Run.class);
    protected Job mockJob = Mockito.mock(Job.class);

    protected void init() throws Exception {
        when(jenkinsStepContext.get(Run.class)).thenReturn(mockRun);
        when(mockRun.getParent()).thenReturn(mockJob);
    }

    protected ServiceNowConfiguration baseServiceNowConfig() {
        ServiceNowConfiguration config = new ServiceNowConfiguration("test");
        config.setProducerId("abc999");
        return config;
    }
}
