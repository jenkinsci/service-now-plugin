package org.jenkinsci.plugins.servicenow;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.ReflectionUtils;
import jenkins.plugins.http_request.HttpMode;
import jenkins.plugins.http_request.HttpRequestStep;
import jenkins.plugins.http_request.ResponseHandle;
import jenkins.plugins.http_request.auth.Authenticator;
import jenkins.plugins.http_request.auth.CredentialBasicAuthentication;
import jenkins.plugins.http_request.util.HttpClientUtil;
import jenkins.plugins.http_request.util.RequestAction;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServiceNowStep extends Step {

    private HttpMode httpMode = HttpMode.GET;
    private final String table;
    private Map<String, String> query;
    private String sysId;
    private List<String> fields;
    private Integer limit;
    private Integer offset;
    private final ServiceNowConfiguration configuration;
    private Object body;

    @DataBoundConstructor
    public ServiceNowStep(ServiceNowConfiguration configuration, String table) {
        this.configuration = configuration;
        this.table = table;
    }

    public HttpMode getHttpMode() {
        return httpMode;
    }

    @DataBoundSetter
    public void setHttpMode(HttpMode httpMode) {
        this.httpMode = httpMode;
    }

    public String getTable() {
        return table;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    @DataBoundSetter
    public void setQuery(Map<String, String> query) {
        this.query = query;
    }

    public String getSysId() {
        return sysId;
    }

    @DataBoundSetter
    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public List<String> getFields() {
        return fields;
    }

    @DataBoundSetter
    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public Integer getLimit() {
        return limit;
    }

    @DataBoundSetter
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    @DataBoundSetter
    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public ServiceNowConfiguration getConfiguration() {
        return configuration;
    }

    public Object getBody() {
        return body;
    }

    @DataBoundSetter
    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        validate();
        return new Execution(context, this);
    }

    private void validate() {
        configuration.validate();
        if (httpMode != HttpMode.GET) {
            if (sysId == null) {
                throw new ServiceNowPluginException("You must specify a sysId value for all non-GET requests");
            }
        }

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

    public static final class Execution extends SynchronousNonBlockingStepExecution<ResponseContentSupplier> {

        private transient ServiceNowStep step;

        protected Execution(@Nonnull StepContext context, @Nonnull ServiceNowStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected ResponseContentSupplier run() throws Exception {
            HttpRequestStep requestStep = HttpRequestStepBuilder.from(step);
            HttpClientBuilder clientBuilder = HttpClientBuilder.create().useSystemProperties();
            HttpClientUtil clientUtil = new HttpClientUtil();
            HttpRequestBase httpRequestBase = clientUtil.createRequestBase(new RequestAction(new URL(requestStep.getUrl()),
                    requestStep.getHttpMode(), requestStep.getRequestBody(), null, requestStep.getCustomHeaders()));
            HttpContext httpContext = new BasicHttpContext();
            CloseableHttpClient client = auth(requestStep, clientBuilder, httpContext, httpRequestBase, getContext().get(TaskListener.class).getLogger());
            HttpResponse response = clientUtil.execute(client, httpContext, httpRequestBase, getContext().get(TaskListener.class).getLogger());
            return new ResponseContentSupplier(ResponseHandle.STRING, response);
        }

        private CloseableHttpClient auth(HttpRequestStep requestStep, HttpClientBuilder clientBuilder,
                                         HttpContext httpContext, HttpRequestBase httpRequestBase,
                                         PrintStream logger) throws IOException, InterruptedException {
            if(requestStep.getAuthentication() != null) {
                Authenticator auth = findCredentials(requestStep);
                if(auth != null) {
                    return auth.authenticate(clientBuilder, httpContext, httpRequestBase, logger);
                }
            }
            return clientBuilder.build();
        }

        private Authenticator findCredentials(HttpRequestStep requestStep) throws IOException, InterruptedException {
            StandardUsernamePasswordCredentials credential = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(
                            StandardUsernamePasswordCredentials.class,
                            getContext().get(Run.class).getParent(), ACL.SYSTEM,
                            URIRequirementBuilder.fromUri(requestStep.getUrl()).build()),
                    CredentialsMatchers.withId(requestStep.getAuthentication()));
            if (credential != null) {
                return new CredentialBasicAuthentication(credential);
            } else {
                return null;
            }
        }

        private Field getExecutionField(Class clazz, String name) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                throw new ServiceNowPluginException("Failed to initialize http request", e);
            }
        }

        private void setFieldValue(Field field, Object target, Object value) {
            field.setAccessible(true);
            ReflectionUtils.setField(field, target, value);
        }
        private static final long serialVersionUID = 1L;

    }

}
