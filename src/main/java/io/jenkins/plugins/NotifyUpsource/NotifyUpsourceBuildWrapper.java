package io.jenkins.plugins.NotifyUpsource;

import java.io.IOException;
// import javax.servlet.ServletException;
import java.util.logging.Logger;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
// import org.kohsuke.stapler.DataBoundSetter;
// import org.kohsuke.stapler.QueryParameter;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
// import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildWrapper;

public class NotifyUpsourceBuildWrapper extends SimpleBuildWrapper {

    private final static Logger Log = Logger.getLogger(NotifyUpsourceBuildWrapper.class.getName());

    private final String upsourceServer;

    public String getUpsourceServer() {
        return upsourceServer;
    }

    // @DataBoundSetter
    // public void setUpsourceServer(String upsourceServer) {
    // this.upsourceServer = upsourceServer;
    // }

    @DataBoundConstructor
    public NotifyUpsourceBuildWrapper(String upsourceServer) {
        Log.info("notifyUpsource ctor");
        this.upsourceServer = upsourceServer;
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener,
            EnvVars initialEnvironment) throws IOException, InterruptedException {
        Log.info("notifyUpsource setup");
    }

    @Symbol("notifyUpsource")
    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        public static final String defaultUpsourceServer = "https://upsource.local.myComp.com";
    /*
        get from project
        [string] $JProjectName   #the Jenkins Project Name      | JenkinsProject
        [string] $JProjectUrl    #the Jenkins Project Url       | http://jenkins/view/abc/job/abc_Unittests/job/trunk/ 

        get from build
        [ValidateSet("in_progress","failed","success")]
        [string] $JBuildState    #the Jenkins Build state       | success
        [decimal[]] $Revisions   #the Revision(s)               | 213,214 or 213

        get from gui
        [string] $UpProjectID    #the Upsource Project ID       | abc
    */

        // public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
        //         throws IOException, ServletException {
        //     if (value.length() == 0)
        //         return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingName());
        //     if (value.length() < 4)
        //         return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_tooShort());
        //     if (!useFrench && value.matches(".*[éáàç].*")) {
        //         return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_reallyFrench());
        //     }
        //     return FormValidation.ok();
        // }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            // Log.info("notifyUpsource isApplicable");
            return true;
        }

        @Override
        public String getDisplayName() {
            return "NotifyUpsource Descriptor";
        }
    }

}
