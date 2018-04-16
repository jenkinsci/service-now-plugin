package org.jenkinsci.plugins.servicenow;

import com.github.paweladamski.httpclientmock.HttpClientMock;
import org.jenkinsci.plugins.servicenow.credentials.CredentialsLocatorStrategy;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.servicenow.workflow.CreateChangeStep;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class CreateChangeStepTest extends AbstractStepTest {

    protected CreateChangeStep.Execution execution;
    private CredentialsLocatorStrategy locatorStrategy = Mockito.mock(CredentialsLocatorStrategy.class);

    @Before
    public void setup() throws Exception {
        init();
        CreateChangeStep step = new CreateChangeStep(baseServiceNowConfig(), "abc123");
        execution = (CreateChangeStep.Execution) step.start(jenkinsStepContext);
        execution.setClientBuilder(httpClientBuilder);
        execution.setCredentialsLocatorStrategy(locatorStrategy);
    }

    @Test
    public void validateHTTPCallForCreateChange() throws Exception {
        String expectedUrl = "https://test.service-now.com/api/sn_sc/servicecatalog/items/abc999/submit_producer";
        HttpClientMock mockHttpClient = new HttpClientMock();
        when(httpClientBuilder.build()).thenReturn(mockHttpClient);
        mockHttpClient.onPost(expectedUrl)
                .doReturnStatus(200)
                .doReturnJSON("{\"response\":\"id\"}");
        execution.run();
        mockHttpClient.verify().post(expectedUrl).called();
    }
}
