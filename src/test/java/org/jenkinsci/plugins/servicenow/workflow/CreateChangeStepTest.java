package org.jenkinsci.plugins.servicenow.workflow;

import org.jenkinsci.plugins.servicenow.ServiceNowTestBase;
import org.jenkinsci.plugins.servicenow.model.ServiceNowConfiguration;
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Test;
import org.junit.runners.model.Statement;

public class CreateChangeStepTest extends ServiceNowTestBase {

    @Test
    public void test() {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
//                WorkflowJob j = story.j.jenkins.createProject(WorkflowJob.class, "simpleProvision");
//                j.setDefinition(new CpsFlowDefinition("serviceNow_createChange serviceNowConfiguration: [instance: 'exampledev', producerId: 'ls98y3khifs8oih3kjshihksjd'], credentialsId: 'jenkins-vault', vaultConfiguration: [url: 'https://vault.example.com:8200', path: 'secret/for/service_now/']", true));
//                WorkflowRun r = story.j.buildAndAssertSuccess(j);
//                story.j.assertLogContains("serviceNow_createChange", r);

                CreateChangeStep step1 = new CreateChangeStep(new ServiceNowConfiguration("exampledev"), "example-creds");
                step1.vaultConfiguration = new VaultConfiguration("example-vault", "path/to/secret");

                CreateChangeStep step2 = new StepConfigTester(story.j).configRoundTrip(step1);
                story.j.assertEqualDataBoundBeans(step1, step2);
            }
        });
    }
}
