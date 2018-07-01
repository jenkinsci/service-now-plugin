package org.jenkinsci.plugins.servicenow.workflow;

import hudson.Extension;
import hudson.model.Run;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.jenkinsci.plugins.servicenow.ResponseContentSupplier;
import org.jenkinsci.plugins.servicenow.ServiceNowExecution;
import org.jenkinsci.plugins.servicenow.ServiceNowPluginException;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.servicenow.model.ServiceNowItem;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UpdateChangeTaskStep extends AbstractServiceNowStep {

    @DataBoundConstructor
    public UpdateChangeTaskStep(ServiceNowConfiguration serviceNowConfiguration, String credentialsId, ServiceNowItem serviceNowItem) {
        super(serviceNowConfiguration, credentialsId, serviceNowItem);
    }

    @Override
    public StepExecution start(StepContext context) {
        return new Execution(context, this);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "serviceNow_updateTask";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(Run.class);
        }
    }

    public static final class Execution extends AbstractItemProviderExecution<ResponseContentSupplier> {

        private transient UpdateChangeTaskStep step;

        Execution(@Nonnull StepContext context, @Nonnull UpdateChangeTaskStep step) {
            super(context);
            this.step = step;
        }

        @Override
        protected ResponseContentSupplier run() throws Exception {
            if (step.getServiceNowItem().getSysId() == null) {
                throw new ServiceNowPluginException("Update change task requires change sys_id");
            }
            ServiceNowExecution exec = ServiceNowExecution.from(step, getProject());

            CloseableHttpResponse response = exec.getCTask();
            ResponseContentSupplier content = new ResponseContentSupplier(ResponseContentSupplier.ResponseHandle.STRING, response);
            Map<String, Object> responseContent = content.getContentMap();
            List<Map<String,Object>> resultList = ((List<Map<String,Object>>)responseContent.get("result"));
            if (resultList.size() == 0) {
                return new ResponseContentSupplier("{\"result\":[]}", 404);
            }
            ServiceNowItem item = step.getServiceNowItem();
            item.setTable("change_task");
            List<String> updated = new ArrayList<>();
            for (Map<String,Object> result : resultList) {
                String updateSysId = (String)result.get("sys_id");
                updated.add(updateSysId);
                item.setSysId(updateSysId);
                exec.updateItem(item);
                exec.updateChange();
            }
            String updateArray = String.join(",", updated);
            return new ResponseContentSupplier("{\"result\":[" + updateArray + "]}", 200);
        }

        private static final long serialVersionUID = 1L;

    }

}
