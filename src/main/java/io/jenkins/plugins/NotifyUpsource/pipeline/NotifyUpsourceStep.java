package io.jenkins.plugins.NotifyUpsource.pipeline;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Launcher;
import hudson.LauncherDecorator;
import hudson.model.Job;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Set;
import java.util.logging.Logger;

import static io.jenkins.plugins.NotifyUpsource.NotifyUpsourceWebService.debugSCMdata;

public class NotifyUpsourceStep extends Step {

  private final static Logger Log = Logger.getLogger(NotifyUpsourceStep.class.getName());

  private final String credentialsId;
  private final String projectId;

  
  @DataBoundConstructor
  public NotifyUpsourceStep(String credentialsId, String projectId) {
    Log.info("notifyUpsource ctor");
    this.credentialsId = credentialsId;
    this.projectId = projectId;
  }

  @Override
  public StepExecution start(StepContext context) throws Exception {
    return new ExecutionImpl(context,credentialsId, projectId);
  }

  /** Execution for {@link NotifyUpsourceStep}. */
  private static class ExecutionImpl extends AbstractStepExecutionImpl {
    private String credentialsId;
    private String projectId;
    ExecutionImpl(StepContext context,String credentialsId, String projectId) {
      super(context);
      Log.info("notifyUpsource execution impl");
      this.credentialsId = credentialsId;
      this.projectId = projectId;

    }


    private static final long serialVersionUID = 1L;

    /** {@inheritDoc} */
    @Override
    public boolean start() throws Exception {
      StepContext context = getContext();
      Run run = context.get(Run.class);
      BodyInvoker invoker = context.newBodyInvoker()
          .withContext(BodyInvoker.mergeLauncherDecorators(context.get(LauncherDecorator.class), new Decorator(run)))
          .withCallback(BodyExecutionCallback.wrap(context));
      invoker.start();
      return false;
    }

    /** {@inheritDoc} */
    @Override
    public void stop(@NonNull Throwable cause) throws Exception {
      getContext().onFailure(cause);
    }
  }

  private static class Decorator extends LauncherDecorator implements Serializable {
    private static final long serialVersionUID = 1;

    private Run<?, ?> build; // FIXME: not serializable

    public Decorator(Run<?,?> build){
      this.build = build;
    }

    @Override
    public Launcher decorate(Launcher launcher, Node node) {
      debugSCMdata(build);
      Log.info("so we decorade the launcer");
      return launcher;
    }
}

  /** Descriptor for {@link NotifyUpsourceStep}. */
  //@Extension(dynamicLoadable = YesNoMaybe.YES, optional = true)
  @Extension
  public static class DescriptorImpl extends StepDescriptor {

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
      return "NotifyUpsourceStep displayName";
    }

    /** {@inheritDoc} */
    @Override
    public String getFunctionName() {
      return "notifyUpsource";
    }

    /** {@inheritDoc} */
    @Override
    public boolean takesImplicitBlockArgument() {
      return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelpFile() {
      return getDescriptorFullUrl() + "/help";
    }

    /**
     * Serve the help file.
     */
    @Override
    public void doHelp(StaplerRequest request, StaplerResponse response) throws IOException {
      response.setContentType("text/html;charset=UTF-8");
      PrintWriter writer = response.getWriter();
      writer.println("NotifyUpsourceStep Description");
      writer.flush();
    }
    
    @Override
    public Set<? extends Class<?>> getRequiredContext() {
      return ImmutableSet.of(TaskListener.class,Run.class);
    }

  }

}
