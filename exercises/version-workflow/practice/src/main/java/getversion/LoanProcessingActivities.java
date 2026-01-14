package getversion;

import getversion.model.ChargeInput;
import getversion.model.CustomerInfo;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface LoanProcessingActivities {

  public String chargeCustomer(ChargeInput input);

  public String sendThankYouToCustomer(CustomerInfo input);

}
