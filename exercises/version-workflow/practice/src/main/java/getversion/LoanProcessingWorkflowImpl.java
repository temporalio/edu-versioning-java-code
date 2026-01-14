package getversion;

import getversion.model.ChargeInput;
import getversion.model.CustomerInfo;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.SearchAttributeKey;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;

public class LoanProcessingWorkflowImpl implements LoanProcessingWorkflow {

  public static final Logger logger = Workflow.getLogger(LoanProcessingWorkflowImpl.class);
  public static final SearchAttributeKey<List<String>> TEMPORAL_CHANGE_VERSION = SearchAttributeKey.forKeywordList("TemporalChangeVersion");

  ActivityOptions options = ActivityOptions.newBuilder()
      .setStartToCloseTimeout(Duration.ofSeconds(5))
      .build();

  private final LoanProcessingActivities activities = 
      Workflow.newActivityStub(LoanProcessingActivities.class, options);

  public String loanProcessingWorkflow(CustomerInfo info) {

    String customerId = info.getCustomerID();
    int amount = info.getAmount();
    String email = info.getEmailAddress();
    int numberOfPeriods = info.getNumberOfPeriods();

    int totalPaid = 0;

    String confirmation = activities.sendThankYouToCustomer(info);

    for (int period = 1; period <= numberOfPeriods; period++) {

      ChargeInput chargeInput = new ChargeInput(customerId, amount, period, numberOfPeriods);

      activities.chargeCustomer(chargeInput);

      totalPaid += chargeInput.getAmount();
      logger.info("Payment complete for period: {} Total Paid: {}", period, totalPaid);

      // using 3 seconds instead of 30 days for faster results
      Workflow.sleep(Duration.ofSeconds(3));
    }

    return String.format("Loan for customer %s has been fully paid (total=%d)", customerId,
        totalPaid);
  }
}
