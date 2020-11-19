package io.jenkins.plugins.NotifyUpsource.freeStyleProj;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class NotifyUpsourceBuildWrapperTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String credentialsId = "credentialsId";
    final String projectId = "proj";

    @Test
    public void testConfigRoundtrip() throws Exception {
        // FreeStyleProject project = jenkins.createFreeStyleProject();
        // project.setScm(new SingleFileSCM("greeting.txt", "hello"));
        // project.getBuildWrappersList().add(new
        // NotifyUpsourceBuildWrapper(credentialsId,projectId));
        // project = jenkins.configRoundtrip(project);
        // jenkins.assertEqualDataBoundBeans(new NotifyUpsourceBuildWrapper(url),
        // project.getBuildWrappersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        // FreeStyleProject project = jenkins.createFreeStyleProject();
        // project.setScm(new SingleFileSCM("greeting.txt", "hello"));
        // NotifyUpsourceBuildWrapper builder = new
        // NotifyUpsourceBuildWrapper(credentialsId,projectId);
        // project.getBuildWrappersList().add(builder);

        // FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        // jenkins.assertLogContains("Hello, " + name, build);
    }
}