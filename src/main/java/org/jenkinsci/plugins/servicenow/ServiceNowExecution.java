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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
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
import org.jenkinsci.plugins.servicenow.model.ServiceNowItem;
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration;
import org.jenkinsci.plugins.servicenow.util.ServiceNowCTasks;
import org.jenkinsci.plugins.servicenow.workflow.AbstractServiceNowStep;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class ServiceNowExecution {

    private static final String PRODUCER_URI = "api/sn_sc/servicecatalog/items";
    private static final String TABLE_API = "api/now/table";
    private static final String ATTACHMENT_API = "api/now/attachment/file";

    private final Credentials credentials;
    private final ServiceNowConfiguration serviceNowConfiguration;
    private final VaultConfiguration vaultConfiguration;
    private final ServiceNowItem serviceNowItem;

    public static ServiceNowExecution from(AbstractServiceNowStep step, Item project) {
        return new ServiceNowExecution(step.getServiceNowConfiguration(), step.getServiceNowItem(), step.getCredentialsId(), step.vaultConfiguration, project);
    }

    private ServiceNowExecution(ServiceNowConfiguration serviceNowConfiguration, ServiceNowItem serviceNowItem, String credentialsId, VaultConfiguration vaultConfiguration, Item project) {
        this.serviceNowConfiguration = serviceNowConfiguration;
        this.vaultConfiguration = vaultConfiguration;
        this.serviceNowItem = serviceNowItem;
        this.credentials = findCredentials(getPatchUrl(), credentialsId, vaultConfiguration, project);
    }

    public CloseableHttpResponse createChange() throws IOException {
        HttpPost requestBase = new HttpPost(getProducerRequestUrl());
        requestBase.setHeaders(new Header[]{getContentTypeHeader("application/json")});
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse updateChange() throws IOException {
        HttpPatch requestBase = new HttpPatch(getPatchUrl());
        requestBase.setHeaders(new Header[]{getContentTypeHeader("application/json")});
        requestBase.setEntity(buildEntity(serviceNowItem.getBody()));
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse getChangeState() throws IOException {
        HttpGet requestBase = new HttpGet(getCurrentStateUrl());
        requestBase.setHeaders(new Header[]{getContentTypeHeader("application/json")});
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse getCTask() throws IOException {
        HttpGet requestBase = new HttpGet(getCTasksUrl());
        requestBase.setHeaders(new Header[]{getContentTypeHeader("application/json")});
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse attachFile() throws IOException {
        HttpPost requestBase = new HttpPost(getAttachmentUrl());
        requestBase.setHeaders(new Header[]{getContentTypeHeader("text/plain")});
        requestBase.setEntity(buildEntity(serviceNowItem.getBody()));
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse attachZip(InputStream zipStream) throws IOException {
        HttpPost requestBase = new HttpPost(getAttachmentUrl());
        requestBase.setHeaders(new Header[]{getContentTypeHeader("application/zip")});
        requestBase.setEntity(buildZipEntity(zipStream));
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

    private InputStreamEntity buildZipEntity(InputStream zipStream) {
        return new InputStreamEntity(zipStream, ContentType.create("application/zip"));
    }

    private Header getContentTypeHeader(String contentType) {
        return new BasicHeader("Content-Type", contentType);
    }

    private String getAttachmentUrl() {
        return getBaseUrl(serviceNowConfiguration.getInstance())+"/"+ATTACHMENT_API+"?file_name="+serviceNowItem.getFilename()+"&table_name="+serviceNowItem.getTable()+"&table_sys_id="+serviceNowItem.getSysId();
    }

    private String getCTasksUrl() throws UnsupportedEncodingException {
        return getBaseUrl(serviceNowConfiguration.getInstance())+"/"+TABLE_API+"/change_task?change_request="+ serviceNowItem.getSysId()+"&short_description="+ URLEncoder.encode(ServiceNowCTasks.valueOf(serviceNowItem.getcTask()).getDescription(), "UTF-8");
    }

    private String getCurrentStateUrl() {
        return getBaseUrl(serviceNowConfiguration.getInstance())+"/"+TABLE_API+"/change_request/"+ serviceNowItem.getSysId()+"?sysparm_fields=state";
    }

    private String getPatchUrl() {
        return getBaseUrl(serviceNowConfiguration.getInstance())+"/"+TABLE_API+"/"+ serviceNowItem.getTable()+"/"+ serviceNowItem.getSysId();
    }

    private String getProducerRequestUrl() {
        return getBaseUrl(serviceNowConfiguration.getInstance())+"/"+PRODUCER_URI+"/"+serviceNowConfiguration.getProducerId()+"/submit_producer";
    }

    private String getBaseUrl(String instance) {
        return "https://" + instance + ".service-now.com";
    }

}
