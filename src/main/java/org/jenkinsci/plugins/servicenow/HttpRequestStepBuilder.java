package org.jenkinsci.plugins.servicenow;

import jenkins.plugins.http_request.HttpMode;
import jenkins.plugins.http_request.HttpRequestStep;
import jenkins.plugins.http_request.util.HttpRequestNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class HttpRequestStepBuilder {

    public static HttpRequestStep from(ServiceNowStep serviceNowStep) {
        String url = getBaseUrl(serviceNowStep.getConfiguration().getInstance());
        url += serviceNowStep.getTable();
        if (serviceNowStep.getSysId() != null) {
            url += "/" + serviceNowStep.getSysId();
        }
        url = addRequestParameters(url, serviceNowStep);
        HttpRequestStep step = new HttpRequestStep(url);
        HttpMode mode = serviceNowStep.getHttpMode();
        step.setHttpMode(mode);
        if (mode != HttpMode.GET) {
            step.setRequestBody(serviceNowStep.getBody().toString());
        }
        if (serviceNowStep.getConfiguration().getCredentialId() != null) {
            step.setAuthentication(serviceNowStep.getConfiguration().getCredentialId());
        }
        step.setCustomHeaders(getHeaders(serviceNowStep));
        return step;
    }

    private static List<HttpRequestNameValuePair> getHeaders(ServiceNowStep serviceNowStep) {
        HttpRequestNameValuePair contentTypeHeader = new HttpRequestNameValuePair("Content-Type", "application/json");
        List<HttpRequestNameValuePair> headers = new ArrayList<>();
        headers.add(contentTypeHeader);
        if (serviceNowStep.getConfiguration().getUsername() != null) {
            HttpRequestNameValuePair authorizationHeader = new HttpRequestNameValuePair("Authorization", serviceNowStep.getConfiguration().getAuthorizationHeader());
            headers.add(authorizationHeader);
        }
        return headers;
    }

    private static String addRequestParameters(String url, ServiceNowStep step) {
        List<String> queries = new ArrayList<>();
        if (step.getLimit() != null) {
            queries.add("sysparm_limit=" + step.getLimit().toString());
        }
        if (step.getOffset() != null) {
            queries.add("sysparm_offset=" + step.getOffset().toString());
        }
        if (step.getFields() != null) {
            queries.add("sysparm_fields=" + String.join(",", step.getFields()));
        }
        step.getQuery().forEach((k, v) -> queries.add(String.join("=", k, encoded(v))));
        if (queries.size() != 0) {
            return url + "?" + String.join("&", queries);
        } else {
            return url;
        }
    }

    private static String encoded(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceNowPluginException("Failed to encode query string for URL");
        }
    }

    private static String getBaseUrl(String instance) {
        return "https://" + instance + "service-now.com/now/table/";
    }
}
