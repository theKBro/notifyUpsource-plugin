package io.jenkins.plugins.NotifyUpsource.pipeline;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class NotifyUpsourceStepTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testScriptedPipeline() throws Exception {
        // String agentLabel = "my-agent";
        // jenkins.createOnlineSlave(Label.get(agentLabel));
        // WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        // String pipelineScript
        //         = "node {\n"
        //         + "  notifyUpsource('credentialsId','projectId'){\n"
        //         + "   echo hiii\n"
        //         + "  }\n"
        //         + "}";
        // job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        // WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        // String expectedString = "hiii";
        // jenkins.assertLogContains(expectedString, completedBuild);
    }

}