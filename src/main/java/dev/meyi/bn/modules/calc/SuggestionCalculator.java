package dev.meyi.bn.modules.calc;


import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.resp.BazaarItem;
import dev.meyi.bn.modules.SuggestionModule;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class SuggestionCalculator {

  public static void basic() {
    try {
      if (BazaarNotifier.validApiKey || BazaarNotifier.apiKeyDisabled) {
        List<String[]> list = new LinkedList<>();
        for (Map.Entry<String, BazaarItem> entry : BazaarNotifier.bazaarDataRaw.products
            .entrySet()) {
          String key = entry.getKey();
          BazaarItem product = BazaarNotifier.bazaarDataRaw.products.get(key);

          if (!BazaarNotifier.bazaarConv.containsKey(key)) {
            BazaarNotifier.bazaarConv.put(key, key);
          }
          String productId = BazaarNotifier.bazaarConv.get(key);
          double sellPrice = (product.buy_summary.size() > 0 && product.sell_summary.size() > 0) ?
              product.buy_summary.get(0).pricePerUnit : 0;
          double buyPrice = (product.sell_summary.size() > 0 && product.buy_summary.size() > 0) ?
              product.sell_summary.get(0).pricePerUnit : 0;
          long sellMovingWeek = product.quick_status.buyMovingWeek;
          long buyMovingWeek = product.quick_status.sellMovingWeek;

          double diff = sellPrice * .99d - buyPrice;
          double profitFlowPerMinute =
              (sellMovingWeek + buyMovingWeek == 0) ? 0 :
                  ((sellMovingWeek * buyMovingWeek) / (10080d * (sellMovingWeek + buyMovingWeek)))
                      * diff;
          list.add(new String[]{productId, Double.toString(profitFlowPerMinute)});
        }
        list.sort(Comparator.comparingDouble(o -> Double.parseDouble(o[1])));
        Collections.reverse(list);
        SuggestionModule.list = list;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static double calculateEP(BazaarItem item) {
    double sellPrice = (item.buy_summary.size() > 0 && item.sell_summary.size() > 0) ?
        item.buy_summary.get(0).pricePerUnit : 0;
    double buyPrice = (item.sell_summary.size() > 0 && item.buy_summary.size() > 0) ?
        item.sell_summary.get(0).pricePerUnit : 0;
    long sellMovingWeek = item.quick_status.sellMovingWeek;
    long buyMovingWeek = item.quick_status.buyMovingWeek;
    return (buyMovingWeek + sellMovingWeek == 0) ? 0 : ((buyMovingWeek * sellMovingWeek) /
        (10080d * (buyMovingWeek + sellMovingWeek))) * (sellPrice * .99d - buyPrice);
  }

  public static String setSuggestionLength(int length) {
    if (length > BazaarNotifier.bazaarDataRaw.products.size()) {
      return length + " is too long";
    } else {
      BazaarNotifier.config.suggestionListLength = length;
      return "Item list size set to " + length;
    }
  }


}
