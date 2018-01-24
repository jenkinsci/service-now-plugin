package org.jenkinsci.plugins.servicenow;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.datapipe.jenkins.vault.credentials.VaultAppRoleCredential;
import com.datapipe.jenkins.vault.credentials.VaultCredential;
import hudson.model.Item;
import hudson.security.ACL;
import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration;
import org.jenkinsci.plugins.servicenow.model.ServiceNowItem;
import org.jenkinsci.plugins.servicenow.util.ServiceNowCTasks;
import org.jenkinsci.plugins.servicenow.workflow.CreateChangeStep;
import org.jenkinsci.plugins.servicenow.workflow.GetChangeStateStep;
import org.jenkinsci.plugins.servicenow.workflow.UpdateChangeItemStep;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class ServiceNowExecution {

    private static final String PRODUCER_URI = "api/sn_sc/servicecatalog/items";
    private static final String TABLE_API = "api/now/table";

    private final Credentials credentials;
    private final ServiceNowConfiguration serviceNowConfiguration;
    private final VaultConfiguration vaultConfiguration;
    private final ServiceNowItem serviceNowItem;

    public static ServiceNowExecution from(CreateChangeStep step, CreateChangeStep.Execution execution) throws IOException, InterruptedException {
        Item project = execution.getProject();
        return new ServiceNowExecution(step.getServiceNowConfiguration(), step.getCredentialsId(), step.vaultConfiguration, project);
    }

    public static ServiceNowExecution from(UpdateChangeItemStep step, UpdateChangeItemStep.Execution execution) throws IOException, InterruptedException {
        Item project = execution.getProject();
        return new ServiceNowExecution(step.getServiceNowConfiguration(), step.getServiceNowItem(), step.getCredentialsId(), step.vaultConfiguration, project);
    }

    public static ServiceNowExecution from(GetChangeStateStep step, GetChangeStateStep.Execution execution) throws IOException, InterruptedException {
        Item project = execution.getProject();
        return new ServiceNowExecution(step.getServiceNowConfiguration(), step.getServiceNowItem(), step.getCredentialsId(), step.vaultConfiguration, project);
    }

    private ServiceNowExecution(ServiceNowConfiguration serviceNowConfiguration, String credentialsId, VaultConfiguration vaultConfiguration, Item project) {
        this.credentials = findCredentials(getProducerRequestUrl(serviceNowConfiguration), credentialsId, vaultConfiguration, project);
        this.serviceNowConfiguration = serviceNowConfiguration;
        this.vaultConfiguration = vaultConfiguration;
        this.serviceNowItem = null;
    }

    private ServiceNowExecution(ServiceNowConfiguration serviceNowConfiguration, ServiceNowItem serviceNowItem, String credentialsId, VaultConfiguration vaultConfiguration, Item project) {
        this.credentials = findCredentials(getPatchUrl(serviceNowConfiguration, serviceNowItem), credentialsId, vaultConfiguration, project);
        this.serviceNowConfiguration = serviceNowConfiguration;
        this.vaultConfiguration = vaultConfiguration;
        this.serviceNowItem = serviceNowItem;
    }

    public CloseableHttpResponse createChange() throws IOException {
        HttpPost requestBase = new HttpPost(getProducerRequestUrl(serviceNowConfiguration));
        requestBase.setHeaders(getHeaders());
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse updateChange() throws IOException {
        HttpPatch requestBase = new HttpPatch(getPatchUrl(serviceNowConfiguration, serviceNowItem));
        requestBase.setHeaders(getHeaders());
        requestBase.setEntity(buildEntity(serviceNowItem.getBody()));
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse getChangeState() throws IOException {
        HttpGet requestBase = new HttpGet(getCurrentStateUrl(serviceNowConfiguration, serviceNowItem));
        requestBase.setHeaders(getHeaders());
        return sendRequest(requestBase);
    }

    private CloseableHttpResponse sendRequest(HttpRequestBase requestBase) throws IOException {
        HttpClientBuilder clientBuilder = HttpClientBuilder.create().useSystemProperties();
        HttpContext httpContext = new BasicHttpContext();
        CloseableHttpClient httpClient = auth(requestBase, clientBuilder, httpContext);
        return httpClient.execute(requestBase, httpContext);
    }

    private CloseableHttpClient auth(HttpRequestBase requestBase, HttpClientBuilder clientBuilder, HttpContext httpContext) {
        if(credentials != null) {
            return authenticate(clientBuilder, requestBase, httpContext);
        }
        return clientBuilder.build();
    }

    private static Credentials findCredentials(String url, String credentialId, VaultConfiguration vaultConfiguration, Item project) {
        Credentials credentials = null;
        if(vaultConfiguration != null) {
            credentials = CredentialsMatchers.firstOrNull(
                    com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
                            VaultAppRoleCredential.class,
                            project.getParent(), ACL.SYSTEM,
                            URIRequirementBuilder.fromUri(url).build()),
                    CredentialsMatchers.withId(credentialId));
        }
        if(credentials == null) {
            credentials = CredentialsMatchers.firstOrNull(
                    com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
                            StandardUsernamePasswordCredentials.class,
                            project.getParent(), ACL.SYSTEM,
                            URIRequirementBuilder.fromUri(url).build()),
                    CredentialsMatchers.withId(credentialId));
        }

        return credentials;
    }

    private CloseableHttpClient authenticate(HttpClientBuilder clientBuilder, HttpRequestBase requestBase, HttpContext httpContext) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                new AuthScope(requestBase.getURI().getHost(), requestBase.getURI().getPort()),
                readCredentials());
        clientBuilder.setDefaultCredentialsProvider(provider);

        AuthCache authCache = new BasicAuthCache();
        authCache.put(URIUtils.extractHost(requestBase.getURI()), new BasicScheme());
        httpContext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);

        return clientBuilder.build();
    }

    private org.apache.http.auth.Credentials readCredentials() {
        org.apache.http.auth.Credentials creds = null;
        if(credentials instanceof StandardUsernamePasswordCredentials) {
            creds = new org.apache.http.auth.UsernamePasswordCredentials(((StandardUsernamePasswordCredentials)credentials).getUsername(), ((StandardUsernamePasswordCredentials)credentials).getPassword().getPlainText());
        }
        if(credentials instanceof VaultAppRoleCredential) {
            Map<String, String> vaultData = VaultService.readVaultData(vaultConfiguration, (VaultCredential) credentials);
            creds = new org.apache.http.auth.UsernamePasswordCredentials(vaultData.get("username"), vaultData.get("password"));
        }
        return creds;
    }

    private StringEntity buildEntity(String body) {
        return new StringEntity(body, ContentType.create("application/json"));
    }

    private Header[] getHeaders() {
        return new Header[]{new BasicHeader("Content-Type", "application/json")};
    }

    private String getCTasksUrl(ServiceNowConfiguration serviceNowConfiguration, ServiceNowItem serviceNowItem) throws UnsupportedEncodingException {
        return getBaseUrl(serviceNowConfiguration.getInstance())+"/"+TABLE_API+"/change_task?change_request="+ serviceNowItem.getSysId()+"&short_description="+ URLEncoder.encode(ServiceNowCTasks.valueOf(serviceNowItem.getcTask()).getDescription(), "UTF-8");
    }

    private String getCurrentStateUrl(ServiceNowConfiguration serviceNowConfiguration, ServiceNowItem serviceNowItem) {
        return getBaseUrl(serviceNowConfiguration.getInstance())+"/"+TABLE_API+"/change_request/"+ serviceNowItem.getSysId()+"?sysparm_fields=state";
    }

    private String getPatchUrl(ServiceNowConfiguration serviceNowConfiguration, ServiceNowItem serviceNowItem) {
        return getBaseUrl(serviceNowConfiguration.getInstance())+"/"+TABLE_API+"/"+ serviceNowItem.getTable()+"/"+ serviceNowItem.getSysId();
    }

    private String getProducerRequestUrl(ServiceNowConfiguration serviceNowConfiguration) {
        return getBaseUrl(serviceNowConfiguration.getInstance())+"/"+PRODUCER_URI+"/"+serviceNowConfiguration.getProducerId()+"/submit_producer";
    }

    private String getBaseUrl(String instance) {
        return "https://" + instance + ".service-now.com";
    }

}
