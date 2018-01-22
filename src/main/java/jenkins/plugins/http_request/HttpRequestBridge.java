package jenkins.plugins.http_request;

import org.jenkinsci.plugins.servicenow.ServiceNowPluginException;

public class HttpRequestBridge {
    public static Object call(HttpRequestStep.Execution httpRequestExecution) {
        try {
            return httpRequestExecution.run();
        } catch (Exception e) {
            throw new ServiceNowPluginException("Failed to run http request");
        }
    }
}
