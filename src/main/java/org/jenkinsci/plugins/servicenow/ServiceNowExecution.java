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
import org.jenkinsci.plugins.servicenow.util.CredentialsUtil;
import org.jenkinsci.plugins.servicenow.util.ServiceNowCTasks;
import org.jenkinsci.plugins.servicenow.workflow.AbstractServiceNowStep;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class ServiceNowExecution {

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
        this.credentials = CredentialsUtil.findCredentials(serviceNowConfiguration.getPatchUrl(serviceNowItem), credentialsId, vaultConfiguration, project);
    }

    public CloseableHttpResponse createChange() throws IOException {
        HttpPost requestBase = new HttpPost(serviceNowConfiguration.getProducerRequestUrl());
        requestBase.setHeaders(new Header[]{getContentTypeHeader("application/json")});
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse updateChange() throws IOException {
        HttpPatch requestBase = new HttpPatch(serviceNowConfiguration.getPatchUrl(serviceNowItem));
        requestBase.setHeaders(new Header[]{getContentTypeHeader("application/json")});
        requestBase.setEntity(buildEntity(serviceNowItem.getBody()));
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse getChangeState() throws IOException {
        HttpGet requestBase = new HttpGet(serviceNowConfiguration.getCurrentStateUrl(serviceNowItem.getSysId()));
        requestBase.setHeaders(new Header[]{getContentTypeHeader("application/json")});
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse getCTask() throws IOException {
        HttpGet requestBase = new HttpGet(serviceNowConfiguration.getCTasksUrl(serviceNowItem));
        requestBase.setHeaders(new Header[]{getContentTypeHeader("application/json")});
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse attachFile() throws IOException {
        HttpPost requestBase = new HttpPost(serviceNowConfiguration.getAttachmentUrl(serviceNowItem));
        requestBase.setHeaders(new Header[]{getContentTypeHeader("text/plain")});
        requestBase.setEntity(buildEntity(serviceNowItem.getBody()));
        return sendRequest(requestBase);
    }

    public CloseableHttpResponse attachZip(InputStream zipStream) throws IOException {
        HttpPost requestBase = new HttpPost(serviceNowConfiguration.getAttachmentUrl(serviceNowItem));
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

    private CloseableHttpClient authenticate(HttpClientBuilder clientBuilder, HttpRequestBase requestBase, HttpContext httpContext) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                new AuthScope(requestBase.getURI().getHost(), requestBase.getURI().getPort()),
                CredentialsUtil.readCredentials(credentials, vaultConfiguration));
        clientBuilder.setDefaultCredentialsProvider(provider);

        AuthCache authCache = new BasicAuthCache();
        authCache.put(URIUtils.extractHost(requestBase.getURI()), new BasicScheme());
        httpContext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);

        return clientBuilder.build();
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
}
