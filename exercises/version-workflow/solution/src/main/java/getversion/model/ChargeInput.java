package getversion.model;

public class ChargeInput {
  private String customerId;
  private int amount;
  private int periodNumber;
  private int numberOfPeriods;

  public ChargeInput() {}

  public ChargeInput(String customerId, int amount, int periodNumber, int numberOfPeriods) {
    this.customerId = customerId;
    this.amount = amount;
    this.periodNumber = periodNumber;
    this.numberOfPeriods = numberOfPeriods;
  }

  public String getCustomerID() {
    return customerId;
  }

  public void setCustomerID(String customerId) {
    this.customerId = customerId;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public int getPeriodNumber() {
    return periodNumber;
  }

  public void setPeriodNumber(int periodNumber) {
    this.periodNumber = periodNumber;
  }

  public int getNumberOfPeriods() {
    return numberOfPeriods;
  }

  public void setNumberOfPeriods(int numberOfPeriods) {
    this.numberOfPeriods = numberOfPeriods;
  }
}
