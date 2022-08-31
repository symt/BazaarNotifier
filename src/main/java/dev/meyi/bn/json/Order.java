package dev.meyi.bn.json;


public class Order {

  public String product;
  public int startAmount;
  public double pricePerUnit;
  public String priceString;
  public OrderType orderStatus = OrderType.BEST;
  public double orderValue;
  public String type;
  public String currentNotification;
  private int amountRemaining;

  public Order(String product, int startAmount, double pricePerUnit, String priceString,
      String type) {
    this.product = product;
    this.startAmount = startAmount;
    amountRemaining = startAmount;
    this.pricePerUnit = pricePerUnit;
    this.priceString = priceString;
    this.type = type;
    orderValue = startAmount * pricePerUnit;
  }

  public int getAmountRemaining() {
    return amountRemaining;
  }

  public void setAmountRemaining(int amountRemaining) {
    this.amountRemaining = amountRemaining;
    orderValue = amountRemaining * pricePerUnit;
  }

  public enum OrderType {BEST, MATCHED, OUTDATED}
}