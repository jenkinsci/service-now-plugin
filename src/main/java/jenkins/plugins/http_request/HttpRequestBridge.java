package jenkins.plugins.http_request;

import hudson.model.TaskListener;

public class HttpRequestBridge {
    static HttpRequestExecution from(HttpRequestStep step,
                                     TaskListener taskListener,
                                     HttpRequestStep.Execution execution) {
        return HttpRequestExecution.from(step, taskListener, execution)
    }
}
