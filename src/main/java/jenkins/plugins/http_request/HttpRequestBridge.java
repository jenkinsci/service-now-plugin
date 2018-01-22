package jenkins.plugins.http_request;

import hudson.model.TaskListener;
import org.jenkinsci.plugins.servicenow.ServiceNowPluginException;

public class HttpRequestBridge {
    static HttpRequestExecution from(HttpRequestStep step,
                                     TaskListener taskListener,
                                     HttpRequestStep.Execution execution) {
        return HttpRequestExecution.from(step, taskListener, execution);
    }

    public static Object call(HttpRequestStep.Execution httpRequestExecution) {
        try {
            return httpRequestExecution.run();
        } catch (Exception e) {
            throw new ServiceNowPluginException("Failed to run http request");
        }
    }
}
