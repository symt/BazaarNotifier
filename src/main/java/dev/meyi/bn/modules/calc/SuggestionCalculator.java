package dev.meyi.bn.modules.calc;


import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.calculation.SuggestionCalculation;
import dev.meyi.bn.json.resp.BazaarItem;
import dev.meyi.bn.modules.module.SuggestionModule;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class SuggestionCalculator {

  public static void basic() {
    try {
      List<SuggestionCalculation> list = new LinkedList<>();
      for (Map.Entry<String, BazaarItem> entry : BazaarNotifier.bazaarDataRaw.products
          .entrySet()) {
        String key = entry.getKey();
        BazaarItem product = BazaarNotifier.bazaarDataRaw.products.get(key);

        if (!BazaarNotifier.config.suggestionModule.suggestionShowEnchantments &&
            key.startsWith("ENCHANTMENT")) {
          continue;
        }

        if (!BazaarNotifier.itemConversionMap.containsKey(key)) {
          BazaarNotifier.itemConversionMap.put(key, key);
        }
        String conversion = BazaarNotifier.itemConversionMap.get(key);

        list.add(new SuggestionCalculation(key, conversion, calculateEP(product)));
      }
      list.sort(Comparator.comparingDouble(o -> o.estimatedProfit));
      Collections.reverse(list);
      SuggestionModule.suggestions = list;

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
}
