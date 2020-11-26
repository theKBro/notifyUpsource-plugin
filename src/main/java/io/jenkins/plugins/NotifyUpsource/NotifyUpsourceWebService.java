package io.jenkins.plugins.NotifyUpsource;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.SubversionTagAction;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMRevisionAction;
import org.apache.commons.validator.routines.UrlValidator;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NotifyUpsourceWebService {

    private final static Logger Log = Logger.getLogger(NotifyUpsourceWebService.class.getName());

    public static Boolean isUrlValid(String url) {
        UrlValidator urlValidator = new UrlValidator();
        return urlValidator.isValid(url);
    }

    public static Boolean isUpsourceServerReachable(String serverUrl, String projectId) throws MalformedURLException { //TODO: Prüfung ob Projekt verfügbar ist, funktioniert noch nicht
        // check url is accessible -> warning
        URL url1 = new URL(serverUrl);
        URL url = new URL(url1.getProtocol(), url1.getHost(), url1.getPort(), url1.getPath() + "/" + projectId, null);
        int responseCode;
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            responseCode = con.getResponseCode();
        } catch (Exception ex) {
            return false;
        }
        return HttpURLConnection.HTTP_OK == responseCode;
    }

    private final String upsourceServer;
    private final String upsourceProjectId;
    private final String upsourcePassword;
    private final String jProjectUrl;
    private final String jProjectName;

    private final ArrayList<String> revisionsInProgress;

    public NotifyUpsourceWebService(String upsourceServer, String upsourceProjectId, String upsourcePassword, String jProjectUrl, String jProjectName) {
        this.upsourceServer = upsourceServer;
        this.upsourceProjectId = upsourceProjectId;
        this.upsourcePassword = upsourcePassword;
        this.jProjectUrl = jProjectUrl;
        this.jProjectName = jProjectName;

        revisionsInProgress = new ArrayList<>();
    }

    public void buildStarted(String revision) {
        sendHttpRequest(revision, "in_progress");
        revisionsInProgress.add(revision);
    }

    public void buildFinished(boolean buildResult) {
        String buildResultString;
        for (String revision : revisionsInProgress) {
            if (buildResult) {
                buildResultString = "success";
            } else {
                buildResultString = "failed";
            }

            sendHttpRequest(revision, buildResultString);
        }
    }

    private void sendHttpRequest(String revision, String state) {
        String json = "{" +
                "\"key\": \"" + jProjectName + "\", " +
                "\"name\": \"" + jProjectName + "\", " +
                "\"state\": \"" + state + "\", " +
                "\"url\": \"" + jProjectUrl + "\", " +
                "\"project\": \"" + upsourceProjectId + "\", " +
                "\"revision\": \"" + revision + "\"" +
                "}";

        try {
            URL urlHttpRequest = new URL(upsourceServer);
            HttpURLConnection connection = (HttpURLConnection) urlHttpRequest.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Authorization", "Bearer perm:" + upsourcePassword);
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStream output = connection.getOutputStream();
            output.write(json.getBytes(StandardCharsets.UTF_8));

            int responseCode = connection.getResponseCode();
            Log.info(String.valueOf(responseCode));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void debugSCMdata(Run<?, ?> build) {
        if (build instanceof WorkflowRun) {
            WorkflowRun workflowBuild = (WorkflowRun) build;
            for (ChangeLogSet<? extends ChangeLogSet.Entry> changeSet : workflowBuild.getChangeSets()) {
                Log.info(changeSet.getBrowser().toString());
            }
        } else if (build instanceof FreeStyleBuild) {
            FreeStyleBuild freeStyleBuild = (FreeStyleBuild) build;
            for (ChangeLogSet<? extends ChangeLogSet.Entry> changeSet : freeStyleBuild.getChangeSets()) {
                Log.info(changeSet.getBrowser().toString());
            }
        } //else {
        // throw new Exception("build is not of type '" + WorkflowRun.class.toString() + " | " + FreeStyleBuild.class.toString() + "'");
        //}


        // final SCMSource s = SCMSource.SourceByItem.findSource(build));
        // if (s instanceof GitLabSCMSource) {
        //   return (GitLabSCMSource) s;
        // }
        // git -> build.getAction(BuildData) ->  buildsByBranchName -> value -> revision ->
        // svn ??

        // https://ssl.barneyb.com/svn/barneyb/bicycle_dashboard/trunk/src/app/vo/

        // The action is only injected for Multibranch Pipeline jobs
        // The action is not injected for Pipeline (single branch) jobs
        List<SCMRevisionAction> scmActions = build.getActions(SCMRevisionAction.class);
        for (SCMRevisionAction scmAction : scmActions) {
            final SCMRevision revision = scmAction.getRevision();
            Log.info("revision: '" + revision.toString() + "'");
        }


        /*List<AbstractScmTagAction> scmTagActions = build.getActions(AbstractScmTagAction.class);
        for (AbstractScmTagAction scmTagAction : scmTagActions) {
            //  GitTagAction -> no revision...
            //  SubversionTagAction
        if(scmTagAction instanceof SubversionTagAction){
          SubversionTagAction svnTag = (SubversionTagAction)scmTagAction;
        }
            Log.info("revision: 'AbstractScmTagAction'");
        }*/

        List<SubversionTagAction> svnTagActions = build.getActions(SubversionTagAction.class);
        for (SubversionTagAction svnTagAction : svnTagActions) {
            for (SubversionTagAction.TagInfo info : svnTagAction.getTagInfo()) {
                //  GitTagAction -> no revision...
                //  SubversionTagAction
          /*if(scmTagAction instanceof SubversionTagAction){
            SubversionTagAction svnTag = (SubversionTagAction)scmTagAction;
          }*/
                Log.info(info.toString());
            }
            Log.info("revision: 'AbstractScmTagAction'");
        }

        // TODO: only in normal project
        /*List<SCMRevisionState> scmRevisionStates = build.getActions(SCMRevisionState.class);
        for (SCMRevisionState scmRevisionState : scmRevisionStates) {
        if(scmRevisionState instanceof SVNRevisionState){
        }else if(scmRevisionState instanceof GitRe)
            Log.info("revision: 'scmRevisionState'");
        }*/

        Run.Summary status = build.getBuildStatusSummary();
        Log.info("status: '" + status.toString() + "'");
        Result res = build.getResult();
        if (res != null)
            Log.info("res: '" + res.toString() + "'");
    }
}
