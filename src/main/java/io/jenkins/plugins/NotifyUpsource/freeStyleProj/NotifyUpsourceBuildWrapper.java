package io.jenkins.plugins.NotifyUpsource.freeStyleProj;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.security.ACL;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.NotifyUpsource.CredentialHelper;
import io.jenkins.plugins.NotifyUpsource.NotifyUpsourceWebService;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildWrapper;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.logging.Logger;


// pipeline context requires serializable

public class NotifyUpsourceBuildWrapper extends SimpleBuildWrapper implements Serializable {

    //TODO: Change Url
    final String urlJenkins = "http://localhost:8080/jenkins/"; //Schrägstrich am Ende des Links ist wichtig

    private final static Logger Log = Logger.getLogger(NotifyUpsourceBuildWrapper.class.getName());

    /**
     * The credentials Id
     *
     * @serial
     */
    private final String credentialsId;

    /**
     * The project Id
     *
     * @serial
     */
    private final String projectId;

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

        public ListBoxModel doFillCredentialsIdItems(@QueryParameter String credentialsId) {
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
                return FormValidation.warning("could not contact " + Url);
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            Log.info("notifyUpsource isApplicable");
            return true;//TODO: only allow this for item with instanceof FreeStyleProject;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "NotifyUpsource Plugin";
        }
    }

    // region buildwrapper

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener,
                      EnvVars initialEnvironment) {
        Log.info("notifyUpsource setup with workspace");
        setUp(context, build, listener, initialEnvironment);
    }

    public void setUp(Context context, Run<?, ?> build, TaskListener listener, EnvVars initialEnvironment) {
        Log.info("notifyUpsource setup without workspace");

        UsernamePasswordCredentialsImpl credentials = CredentialHelper.findCredentials(credentialsId);
        String upsourceServer = CredentialHelper.getUrlFromCredentials(credentials);

        String upsourcePassword = credentials.getPassword().getPlainText(); //TODO: Ist das so gut? Mehr Infos hier: https://javadoc.jenkins-ci.org/hudson/util/Secret.html

        String jProjectUrl = urlJenkins + build.getUrl();   //TODO: Passt das so?

        String jProjectName = initialEnvironment.get("JOB_NAME");

        String svnRevision = initialEnvironment.get("SVN_REVISION");

        /*
        String buildUrl = initialEnvironment.get("BUILD_URL");      //Liefert null zurück -> prüfen
        Log.info(buildUrl);
         */

        NotifyUpsourceWebService upsourceConnection = new NotifyUpsourceWebService(upsourceServer + "/~buildStatus", projectId, upsourcePassword, jProjectUrl, jProjectName);
        context.setDisposer(new NotifyUpsourceDisposer(upsourceConnection));

        upsourceConnection.buildStarted(svnRevision);

        /*
        List<SubversionTagAction> svnTagActions = build.getActions(SubversionTagAction.class);
        for (SubversionTagAction svnTagAction : svnTagActions) {
            for (SubversionTagAction.TagInfo info : svnTagAction.getTagInfo()) {
                upsourceConnection.buildStarted("186538");
            }
        }*/

    }

    //endregion

    public static class NotifyUpsourceDisposer extends SimpleBuildWrapper.Disposer {
        private static final long serialVersionUID = 147517200825533249L;

        private final NotifyUpsourceWebService upsourceConnection;

        public NotifyUpsourceDisposer(NotifyUpsourceWebService upsourceConnection) {
            this.upsourceConnection = upsourceConnection;
        }

        @Override
        public void tearDown(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
            Log.info("notifyUpsource teardown");
            //debugSCMdata(build);
            Result res = build.getResult();

            if (res != null) {
                upsourceConnection.buildFinished(res.isBetterOrEqualTo(Result.SUCCESS));
            }
            else {
                upsourceConnection.buildFinished(true);
            }
        }
    }
}
