package org.jenkinsci.plugins.servicenow;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ReflectionUtils;
import jenkins.plugins.http_request.HttpMode;
import jenkins.plugins.http_request.HttpRequestBridge;
import jenkins.plugins.http_request.HttpRequestExecution;
import jenkins.plugins.http_request.HttpRequestStep;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Field;
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
    private String credentialId;

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

    public String getCredentialId() {
        return credentialId;
    }

    @DataBoundSetter
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
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

    public static final class Execution extends SynchronousNonBlockingStepExecution<Object> {

        private transient ServiceNowStep step;

        @StepContextParameter
        private transient Run<?, ?> run;
        @StepContextParameter
        private transient TaskListener listener;

        protected Execution(@Nonnull StepContext context, @Nonnull ServiceNowStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected Object run() throws Exception {
            HttpRequestStep.Execution httpRequestExecution =
                    new HttpRequestStep.Execution();
            setFieldValue(getExecutionField("listener"),
                    httpRequestExecution, listener);
            setFieldValue(getExecutionField("run"),
                    httpRequestExecution, run);
            setFieldValue(getExecutionField("step"),
                    httpRequestExecution, HttpRequestStepBuilder.from(step));
            return HttpRequestBridge.call(httpRequestExecution);
        }

        private Field getExecutionField(String name) {
            Class execClass = HttpRequestStep.Execution.class;
            try {
                return execClass.getDeclaredField(name);
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
