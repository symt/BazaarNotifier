package dev.meyi.bn.json.resp;

import java.util.List;

public class BazaarItem {
  public String product_id;

  public List<BazaarSubItem> sell_summary;
  public List<BazaarSubItem> buy_summary;

  public BazaarItemSummary quick_status;

  static class BazaarSubItem {
    public int amount;
    public double pricePerUnit;
    public int orders;
  }

  static class BazaarItemSummary {
    public String productId;
    public double sellPrice;
    public long sellVolume;
    public long sellMovingWeek;
    public long sellOrders;
    public double buyPrice;
    public long buyVolume;
    public long buyMovingWeek;
    public long buyOrders;
  }
}


