package getversion;

import getversion.model.CustomerInfo;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface LoanProcessingWorkflow {

  @WorkflowMethod
  public String loanProcessingWorkflow(CustomerInfo info);

}
