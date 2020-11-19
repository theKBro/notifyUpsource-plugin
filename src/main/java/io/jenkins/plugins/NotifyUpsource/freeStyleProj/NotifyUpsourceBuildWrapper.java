package io.jenkins.plugins.NotifyUpsource.freeStyleProj;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.*;
import hudson.model.*;
import hudson.model.Run.Summary;
import hudson.remoting.Channel;
import hudson.security.ACL;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.NotifyUpsource.CredentialHelper;
import io.jenkins.plugins.NotifyUpsource.NotifyUpsourceWebService;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildWrapper;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Logger;

import static io.jenkins.plugins.NotifyUpsource.NotifyUpsourceWebService.debugSCMdata;

// pipeline context requires serializable

public class NotifyUpsourceBuildWrapper extends SimpleBuildWrapper implements Serializable {

    private final static Logger Log = Logger.getLogger(NotifyUpsourceBuildWrapper.class.getName());

    /**
     * The credentials Id
     *
     * @serial
     */
    private String credentialsId;

    /**
     * The project Id
     *
     * @serial
     */
    private String projectId;

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getProjectId() {
        return projectId;
    }


    @DataBoundConstructor
    public NotifyUpsourceBuildWrapper(String credentialsId, String projectId) {
        Log.info("notifyUpsource ctor");
        this.credentialsId = credentialsId;
        this.projectId = projectId;
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        // public static final String defaultUpsourceServer =
        // "https://upsource.local.myComp.com";

        /*
         * get from project [string] $JProjectName #the Jenkins Project Name |
         * JenkinsProject [string] $JProjectUrl #the Jenkins Project Url |
         * http://jenkins/view/abc/job/abc_Unittests/job/trunk/
         *
         * get from build [ValidateSet("in_progress","failed","success")] [string]
         * $JBuildState #the Jenkins Build state | success [decimal[]] $Revisions #the
         * Revision(s) | 213,214 or 213
         *
         * get from gui [string] $UpProjectID #the Upsource Project ID | abc
         */

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
            // item is the project
            StandardListBoxModel result = new StandardListBoxModel();
            return result
                    // .includeEmptyValue() // we use the username as url -> credentials are
                    // mandatory!
                    .includeAs(ACL.SYSTEM, Jenkins.get(), UsernamePasswordCredentialsImpl.class)
                    .includeCurrentValue(credentialsId);
        }

        public FormValidation doCheckProjectId(@QueryParameter String value, @QueryParameter String credentialsId)
                throws MalformedURLException {
            Log.info("doCheckProjectId: value:'" + value + "' credentialsId:'" + credentialsId + "'");
            if (value.length() == 0) {
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingName());
            }
            if (credentialsId.length() == 0) {
                return FormValidation.warning("no url available because no credentials are set");
            }

            UsernamePasswordCredentialsImpl credentials = CredentialHelper.findCredentials(credentialsId);
            if (credentials == null) {
                return FormValidation.error("Cannot find currently selected credentials");
            }
            String Url = CredentialHelper.getUrlFromCredentials(credentials);
            if (!NotifyUpsourceWebService.isUpsourceServerReachable(Url, value)) {
                return FormValidation
                        .warning("Could not contact '" + Url + "' with project: '" + value + "'");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckCredentialsId(/* @AncestorInPath Item item, */@QueryParameter String value)
                throws MalformedURLException {
            Log.info("doCheckProjectId: value:'" + value + "'");
            // item is the project
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Credentials are mandatory!");
            }
            UsernamePasswordCredentialsImpl credentials = CredentialHelper.findCredentials(value);
            if (credentials == null) {
                return FormValidation.error("Cannot find currently selected credentials");
            }
            // check the credentials here
            // check if username is a valid url -> error
            String Url = CredentialHelper.getUrlFromCredentials(credentials);
            if (!NotifyUpsourceWebService.isUrlValid(Url)) {
                return FormValidation.warning("Username of passed credential '" + Url
                        + "' is not a valid url! And is used as Upsource url for the usage of this plugin!");
            }

            if (!NotifyUpsourceWebService.isUpsourceServerReachable(Url, "")) {
                return FormValidation.warning("could not contact " + Url.toString());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            Log.info("notifyUpsource isApplicable");
            return true;//TODO: only allow this for item with instanceof FreeStyleProject;
        }

        @Override
        public String getDisplayName() {
            return "NotifyUpsource Plugin";
        }
    }

    // region buildwrapper

    @Override
    public boolean requiresWorkspace() {
        return false;
    } // TODO: is it more easy if workspace is required?

    @Override
    protected boolean runPreCheckout() {
        return true;
    } // not needed if workspace is required

    @Override
    public Launcher decorateLauncher(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException, Run.RunnerAbortedException {
        return new ScmListeningLauncher(super.decorateLauncher(build, launcher, listener));
        // TODO only works for abstract builds...
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener,
                      EnvVars initialEnvironment) throws IOException, InterruptedException {
        Log.info("notifyUpsource setup with workspace");
        setUp(context, build, listener, initialEnvironment);
    }

    public void setUp(Context context, Run<?, ?> build, TaskListener listener, EnvVars initialEnvironment)
            throws IOException, InterruptedException {
        Log.info("notifyUpsource setup without workspace");
        Summary status = build.getBuildStatusSummary();
        Log.info("status: '" + status.toString() + "'");
        Result res = build.getResult();
        if (res != null)
            Log.info("res: '" + res.toString() + "'");
        context.setDisposer(new NotifyUpsourceDisposer());
    }

    //endregion

    public static class ScmListeningLauncher extends Launcher.DecoratedLauncher {

        public ScmListeningLauncher(Launcher inner) {
            super(inner);
        }

        @Override
        public Proc launch(ProcStarter starter) throws IOException {
            return super.launch(starter);
        }

        @Override
        public Channel launchChannel(String[] cmd, OutputStream out, FilePath workDir, Map<String, String> envVars)
                throws IOException, InterruptedException {
            return super.launchChannel(cmd, out, workDir, envVars);
        }

        @Override
        public void kill(Map<String, String> modelEnvVars) throws IOException, InterruptedException {
            super.kill(modelEnvVars);
        }

        @Override
        public Proc launch(String[] cmd, boolean[] mask, String[] env, InputStream in, OutputStream out, FilePath workDir)
                throws IOException {
            return super.launch(cmd, mask, env, in, out, workDir);
        }

        @Override
        public Proc launch(String[] cmd, String[] env, InputStream in, OutputStream out, FilePath workDir)
                throws IOException {
            return super.launch(cmd, env, in, out, workDir);
        }

        @Override
        public String toString() {
            return super.toString() + "; decorated";
        }

    }

    public static class NotifyUpsourceDisposer extends SimpleBuildWrapper.Disposer {
        private static final long serialVersionUID = 147517200825533249L;

        @Override
        public void tearDown(Run<?, ?> build, TaskListener listener) throws IOException, InterruptedException {
            Log.info("notifyUpsource teardown");
            debugSCMdata(build);
        }
    }

}
