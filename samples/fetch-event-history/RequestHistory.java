import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflowservice.v1.GetWorkflowExecutionHistoryRequest;
import io.temporal.api.workflowservice.v1.GetWorkflowExecutionHistoryResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import com.google.protobuf.util.JsonFormat;


public class RequestHistory {

  public static void main(String[] args) throws IOException {

    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

    WorkflowClient client = WorkflowClient.newInstance(service);

    WorkflowExecution execution = WorkflowExecution.newBuilder()
        .setWorkflowId("loan-processing-workflow-customer-a100")
        .build();

    GetWorkflowExecutionHistoryRequest request = GetWorkflowExecutionHistoryRequest.newBuilder()
        .setNamespace("default")
        .setExecution(execution)
        .build();
    GetWorkflowExecutionHistoryResponse result = service.blockingStub().getWorkflowExecutionHistory(request);

    String jsonHistory = JsonFormat.printer().print(result.getHistory());
   
    BufferedWriter writer = new BufferedWriter(new FileWriter("event_history.json"));
    writer.write(jsonHistory);
    writer.close();
  }

}
