package org.jenkinsci.plugins.servicenow;

import hudson.model.Item;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.jenkinsci.plugins.servicenow.credentials.CredentialsLocatorStrategy;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.servicenow.model.ServiceNowItem;
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceNowExecutionTest {

    private static final String PRODUCER_ID = "producer123";
    private static final String CREDS_ID = "creds-id";
    private static final String CHANGE_SYS_ID = "change-sys-id-123";
    ServiceNowConfiguration serviceNowConfiguration;
    ServiceNowItem serviceNowItem;
    String credentialsId = CREDS_ID;
    VaultConfiguration vaultConfiguration;
    Item project;
    HttpClientBuilder clientBuilder;
    CredentialsLocatorStrategy locatorStrategy;
    CloseableHttpClient client;
    ServiceNowExecution execution;
    ArgumentCaptor<HttpRequestBase> baseArgumentCaptor =
            ArgumentCaptor.forClass(HttpRequestBase.class);

    @Before
    public void setup() {
        serviceNowConfiguration = new ServiceNowConfiguration("test-instance");
        serviceNowConfiguration.setProducerId(PRODUCER_ID);
        serviceNowItem = new ServiceNowItem("change_request", CHANGE_SYS_ID);
        vaultConfiguration = new VaultConfiguration("https://test:8200", "vault/path");
        project = Mockito.mock(Item.class);
        locatorStrategy = Mockito.mock(CredentialsLocatorStrategy.class);
        clientBuilder = Mockito.mock(HttpClientBuilder.class);
        execution = new ServiceNowExecution(serviceNowConfiguration, serviceNowItem,
                credentialsId, vaultConfiguration, project,
                clientBuilder, locatorStrategy);
        client = Mockito.mock(CloseableHttpClient.class);
    }

    @Test
    public void createChangeCallsProducerEndpointWithProducerId() throws Exception {
        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
        when(clientBuilder.build()).thenReturn(client);
        when(client.execute(any(HttpRequestBase.class), any(HttpContext.class)))
                .thenReturn(response);
        CloseableHttpResponse changeResponse = execution.createChange();
        assertEquals(response, changeResponse);
        verify(client, Mockito.times(1))
                .execute(baseArgumentCaptor.capture(), any(HttpContext.class));
        HttpPost post = (HttpPost) baseArgumentCaptor.getValue();
        assertEquals("https://test-instance.service-now.com/api/sn_sc/servicecatalog/items/producer123/submit_producer",
                post.getURI().toString());
    }
}
