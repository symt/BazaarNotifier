package dev.meyi.bn.json;


import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.resp.BazaarItem;
import dev.meyi.bn.utilities.RenderUtils;

public class Order {

  public String product;
  public int startAmount;
  public double pricePerUnit;
  public String priceString;
  public OrderStatus orderStatus = OrderStatus.BEST;
  public double orderValue;
  public OrderType type;
  private int amountRemaining;

  public Order(String product, int startAmount, double pricePerUnit, String priceString,
      OrderType type) {
    this.product = product;
    this.startAmount = startAmount;
    amountRemaining = startAmount;
    this.pricePerUnit = pricePerUnit;
    this.priceString = priceString;
    this.type = type;
    orderValue = startAmount * pricePerUnit;
  }

  public Order(String product, OrderType type, double pricePerUnit, int startAmount) {
    this.product = product;
    this.type = type;
    this.pricePerUnit = pricePerUnit;
    this.startAmount = startAmount;
  }

  public int getAmountRemaining() {
    return amountRemaining;
  }

  public void setAmountRemaining(int amountRemaining) {
    this.amountRemaining = amountRemaining;
    orderValue = amountRemaining * pricePerUnit;
  }

  public boolean matches(Order other) {
    return other.type == this.type && other.product.equals(this.product)
        && other.startAmount == this.startAmount &&
        other.pricePerUnit == this.pricePerUnit;
  }

  public String getProductId() {
    return BazaarNotifier.bazaarConv.inverse().get(product);
  }

  public void updateStatus() {
    OrderStatus newOrderStatus = null;
    if (!(BazaarNotifier.activeBazaar && (BazaarNotifier.validApiKey
        || BazaarNotifier.apiKeyDisabled))) {
      return;
    }
    if (OrderType.BUY.equals(this.type)) {
      if (BazaarNotifier.bazaarDataRaw.products.get(getProductId()).sell_summary.size() == 0) {
        orderStatus = OrderStatus.SEARCHING;
        return;
      }
      BazaarItem.BazaarSubItem bazaarSubItem = BazaarNotifier.bazaarDataRaw.products
          .get(getProductId()).sell_summary.get(0);
      if (this.pricePerUnit < bazaarSubItem.pricePerUnit) {
        newOrderStatus = OrderStatus.OUTDATED;
      } else if (this.pricePerUnit == bazaarSubItem.pricePerUnit
          && this.startAmount >= bazaarSubItem.amount
          && bazaarSubItem.orders == 1) { //&& this.amountRemaining <= bazaarSubItem.amount
        newOrderStatus = OrderStatus.BEST;
      } else if (this.pricePerUnit > bazaarSubItem.pricePerUnit) {
        newOrderStatus = OrderStatus.SEARCHING;
      } else if (pricePerUnit == bazaarSubItem.pricePerUnit && bazaarSubItem.orders == 1) {
        newOrderStatus = OrderStatus.SEARCHING;
      } else if (this.pricePerUnit == bazaarSubItem.pricePerUnit && bazaarSubItem.orders > 1) {
        newOrderStatus = OrderStatus.MATCHED;
      }
    } else {
      if (BazaarNotifier.bazaarDataRaw.products.get(getProductId()).buy_summary.size() == 0) {
        newOrderStatus = OrderStatus.SEARCHING;
      }
      BazaarItem.BazaarSubItem bazaarSubItem = BazaarNotifier.bazaarDataRaw.products
          .get(getProductId()).buy_summary.get(0);
      if (this.pricePerUnit > bazaarSubItem.pricePerUnit) {
        newOrderStatus = OrderStatus.OUTDATED;
      } else if (this.pricePerUnit == bazaarSubItem.pricePerUnit
          && this.startAmount >= bazaarSubItem.amount && bazaarSubItem.orders == 1) {
        newOrderStatus = OrderStatus.BEST;
      } else if (this.pricePerUnit < bazaarSubItem.pricePerUnit) {
        newOrderStatus = OrderStatus.SEARCHING;
      } else if (pricePerUnit == bazaarSubItem.pricePerUnit && bazaarSubItem.orders == 1) {
        newOrderStatus = OrderStatus.SEARCHING;
      } else if (this.pricePerUnit == bazaarSubItem.pricePerUnit && bazaarSubItem.orders > 1) {
        newOrderStatus = OrderStatus.MATCHED;
      }
    }
    if (this.orderStatus != newOrderStatus) {
      if (OrderStatus.BEST.equals(newOrderStatus) && this.orderStatus != OrderStatus.SEARCHING) {
        RenderUtils.chatNotification(this, "REVIVED");
      } else if (OrderStatus.MATCHED.equals(newOrderStatus)) {
        RenderUtils.chatNotification(this, "MATCHED");
      } else if (OrderStatus.OUTDATED.equals(newOrderStatus)) {
        RenderUtils.chatNotification(this, "OUTDATED");
      }
      this.orderStatus = newOrderStatus;
    }
  }

  public enum OrderStatus {BEST, MATCHED, OUTDATED, SEARCHING}

  public enum OrderType {
    BUY("Buy Order"),
    SELL("Sell Offer");
    public final String longName;

    OrderType(String longName) {
      this.longName = longName;
    }
  }

}