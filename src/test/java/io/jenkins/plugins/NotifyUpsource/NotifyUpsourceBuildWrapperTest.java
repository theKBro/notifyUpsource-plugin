package io.jenkins.plugins.NotifyUpsource;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;

public class NotifyUpsourceBuildWrapperTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String url = "testUrl";
/*
    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setScm(new SingleFileSCM("greeting.txt", "hello"));
        project.getBuildWrappersList().add(new NotifyUpsourceBuildWrapper(url));
        project = jenkins.configRoundtrip(project);
       // jenkins.assertEqualDataBoundBeans(new NotifyUpsourceBuildWrapper(url), project.getBuildWrappersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setScm(new SingleFileSCM("greeting.txt", "hello"));
        NotifyUpsourceBuildWrapper builder = new NotifyUpsourceBuildWrapper(url);
        project.getBuildWrappersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        // jenkins.assertLogContains("Hello, " + name, build);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n"
                + "  notifyUpsource('testserver.com'){\n"
                + "   echo hiii\n"
                + "  }\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "hiii";
        jenkins.assertLogContains(expectedString, completedBuild);
    }
*/
}