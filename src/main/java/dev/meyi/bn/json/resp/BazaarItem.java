package dev.meyi.bn.json.resp;

import java.util.List;

public class BazaarItem {

  public String product_id;

  public List<BazaarSubItem> sell_summary;
  public List<BazaarSubItem> buy_summary;

  public BazaarItemSummary quick_status;

  public BazaarItem(String product_id, List<BazaarSubItem> sell_summary,
      List<BazaarSubItem> buy_summary,
      BazaarItemSummary quick_status) {
    this.product_id = product_id;
    this.sell_summary = sell_summary;
    this.buy_summary = buy_summary;
    this.quick_status = quick_status;
  }

  public static class BazaarSubItem {

    public int amount;
    public double pricePerUnit;
    public int orders;

    public BazaarSubItem(int amount, double pricePerUnit, int orders) {
      this.amount = amount;
      this.pricePerUnit = pricePerUnit;
      this.orders = orders;
    }

    public double getPriceWithTax(){
      return pricePerUnit * 0.9875d;
    }
  }

  public static class BazaarItemSummary {

    public String productId;
    public double sellPrice;
    public long sellVolume;
    public long sellMovingWeek;
    public long sellOrders;
    public double buyPrice;
    public long buyVolume;
    public long buyMovingWeek;
    public long buyOrders;

    public BazaarItemSummary(String productId, double sellPrice, long sellVolume,
        long sellMovingWeek, long sellOrders,
        double buyPrice, long buyVolume, long buyMovingWeek, long buyOrders) {
      this.productId = productId;
      this.sellPrice = sellPrice;
      this.sellVolume = sellVolume;
      this.sellMovingWeek = sellMovingWeek;
      this.sellOrders = sellOrders;
      this.buyPrice = buyPrice;
      this.buyVolume = buyVolume;
      this.buyMovingWeek = buyMovingWeek;
      this.buyOrders = buyOrders;
    }

  }
}


