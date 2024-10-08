package dev.meyi.bn.json.calculation;

public class SuggestionCalculation {
  public String itemName;
  public String conversion;
  public double estimatedProfit;

  public SuggestionCalculation(String itemName, String conversion, double estimatedProfit) {
    this.itemName = itemName;
    this.conversion = conversion;
    this.estimatedProfit = estimatedProfit;
  }
}
