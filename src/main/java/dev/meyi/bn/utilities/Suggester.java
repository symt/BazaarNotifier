package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.config.Configuration;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

public class Suggester {

  public static void basic() {
    try {
      if (BazaarNotifier.validApiKey || BazaarNotifier.apiKeyDisabled) {
        JSONObject bazaarData = BazaarNotifier.bazaarDataRaw;
        Iterator<String> bazaarKeys = bazaarData.keys();
        JSONArray bazaarDataFormatted = new JSONArray();

        while (bazaarKeys.hasNext()) {
          String key = bazaarKeys.next();
          JSONObject product = bazaarData.getJSONObject(key);

          if (!BazaarNotifier.bazaarConversions.has(key)) {
            BazaarNotifier.bazaarConversions.put(key, key);
            BazaarNotifier.bazaarConversionsReversed.put(key, key);
          }
          JSONObject currentProduct = new JSONObject()
              .put("productId", BazaarNotifier.bazaarConversions.getString(key))
              .put("sellOfferPrice",
                  (product.getJSONArray("buy_summary").length() > 0
                      && product.getJSONArray("sell_summary").length() > 0) ?
                      product.getJSONArray("buy_summary").getJSONObject(0)
                          .getDouble("pricePerUnit") : 0)
              .put("buyOrderPrice",
                  (product.getJSONArray("sell_summary").length() > 0
                      && product.getJSONArray("buy_summary").length() > 0) ?
                      product.getJSONArray("sell_summary").getJSONObject(0)
                          .getDouble("pricePerUnit") : 0)
              .put("sellCount",
                  product.getJSONObject("quick_status")
                      .getLong("buyMovingWeek"))
              .put("buyCount",
                  product.getJSONObject("quick_status")
                      .getLong("sellMovingWeek"));

          double diff = currentProduct.getDouble("sellOfferPrice") * .99d - currentProduct
              .getDouble("buyOrderPrice");
          double profitFlowPerMinute =
              (currentProduct.getLong("sellCount") + currentProduct.getLong("buyCount") == 0) ? 0 :
                  ((currentProduct.getLong("sellCount") * currentProduct.getLong("buyCount")) / (
                      10080d
                          * (currentProduct.getLong("sellCount") + currentProduct
                          .getLong("buyCount"))))
                      * diff;
          bazaarDataFormatted.put(currentProduct.put("profitFlowPerMinute", profitFlowPerMinute));
        }

        bazaarDataFormatted = Utils.sortJSONArray(bazaarDataFormatted, "profitFlowPerMinute");

        JSONObject bazaarCache = new JSONObject();
        bazaarDataFormatted.forEach((data) -> {
          JSONObject jsonData = (JSONObject) data;
          bazaarCache.put(jsonData.getString("productId").toLowerCase(), data);
        });

        BazaarNotifier.bazaarCache = bazaarCache;
        BazaarNotifier.bazaarDataFormatted = bazaarDataFormatted;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String setSuggestionLength(int length) {
    if (length > BazaarNotifier.bazaarDataRaw.length()) {
      return length + " is too long";
    } else {
      Configuration.suggestionListLength = length;
      return "Item list size set to " + length;
    }
  }


}
