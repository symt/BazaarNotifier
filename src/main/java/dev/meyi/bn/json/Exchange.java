package dev.meyi.bn.json;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.Order.OrderType;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import java.util.Map;

public class Exchange {

  private final String productId;
  private final double pricePerUnit;
  private final OrderType type;
  private final Map<String, Integer> craftingResources;
  private int amount;

  public Exchange(OrderType type, String productId, double pricePerUnit, int amount) {
    this.type = type;
    this.productId = productId;
    this.pricePerUnit = pricePerUnit;
    this.amount = amount;

    this.craftingResources = BazaarNotifier.craftingRecipeMap.get(productId).material;
  }

  public String getProductId() {
    return productId;
  }

  public Map<String, Integer> getCraftingResources() {
    return craftingResources;
  }

  public double getPricePerUnit() {
    return pricePerUnit;
  }

  public int getAmount() {
    return amount;
  }

  public void removeAmount(int remove) {
    this.amount -= remove;
  }

  public void addAmount(int add) {
    this.amount += add;
  }

  public OrderType getType() {
    return type;
  }

  public boolean matchesOrder(Exchange exchange) {
    return this.type != exchange.type && this.productId.equals(exchange.productId);
  }

  public boolean canCraft() {
    return type == OrderType.SELL && BazaarNotifier.craftingRecipeMap.containsKey(productId);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Exchange) && this.pricePerUnit == ((Exchange) obj).pricePerUnit
        && this.productId.equals(((Exchange) obj).productId);
  }
}
