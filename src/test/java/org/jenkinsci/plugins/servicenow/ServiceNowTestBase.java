package org.jenkinsci.plugins.servicenow;

import org.apache.commons.lang.SystemUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.RestartableJenkinsRule;

public class ServiceNowTestBase {

    @Before
    public void before() {
        // FIXME on CI windows nodes don't have Docker4Windows
        Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);
    }


    String dockerHost = SystemUtils.IS_OS_WINDOWS ? "tcp://localhost:2375" : "unix:///var/run/docker.sock";

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();
}