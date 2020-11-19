package io.jenkins.plugins.NotifyUpsource;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.scm.AbstractScmTagAction;
import hudson.scm.ChangeLogSet;
import hudson.scm.SCMRevisionState;
import hudson.scm.SubversionTagAction;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMRevisionAction;
import org.apache.commons.validator.routines.UrlValidator;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

public class NotifyUpsourceWebService {

    private final static Logger Log = Logger.getLogger(NotifyUpsourceWebService.class.getName());

    public static Boolean isUrlValid(String url){
        UrlValidator urlValidator = new UrlValidator();
        return urlValidator.isValid(url);
    }

    public static Boolean isUpsourceServerReachable(String serverUrl, String projectId) throws MalformedURLException  {
        // check url is accessible -> warning
        URL url1 = new URL(serverUrl);
        URL url = new URL(url1.getProtocol(), url1.getHost(), url1.getPort(), url1.getPath() + "/" + projectId, null);
        int responseCode = 0;
        try {
          HttpURLConnection con = (HttpURLConnection) url.openConnection();
          con.setRequestMethod("HEAD");
          responseCode = con.getResponseCode();
        } catch (Exception ex) {
          return false;
        }
        if (HttpURLConnection.HTTP_OK != responseCode) {
          return false;
        }
        return true;
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


        List<AbstractScmTagAction> scmTagActions = build.getActions(AbstractScmTagAction.class);
        for (AbstractScmTagAction scmTagAction : scmTagActions) {
            //  GitTagAction -> no revision...
            //  SubversionTagAction
      /*  if(scmTagAction instanceof SubversionTagAction){
          SubversionTagAction svnTag = (SubversionTagAction)scmTagAction;
        }*/
            Log.info("revision: 'AbstractScmTagAction'");
        }

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
        List<SCMRevisionState> scmRevisionStates = build.getActions(SCMRevisionState.class);
        for (SCMRevisionState scmRevisionState : scmRevisionStates) {
    /*    if(scmRevisionState instanceof SVNRevisionState){


        }else if(scmRevisionState instanceof GitRe)
      */
            Log.info("revision: 'scmRevisionState'");
        }

        Run.Summary status = build.getBuildStatusSummary();
        Log.info("status: '" + status.toString() + "'");
        Result res = build.getResult();
        if (res != null)
            Log.info("res: '" + res.toString() + "'");
    }

}
