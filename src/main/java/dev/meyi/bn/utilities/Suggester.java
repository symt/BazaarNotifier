package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

public class Suggester {

  public static void basic() {
    JSONObject bazaarData = BazaarNotifier.bazaarDataRaw;
    Iterator<String> bazaarKeys = bazaarData.keys();
    JSONArray bazaarDataFormatted = new JSONArray();

    while (bazaarKeys.hasNext()) {
      String key = bazaarKeys.next();
      JSONObject product = bazaarData.getJSONObject(key);

      JSONObject currentProduct = new JSONObject()
          .put("productId", BazaarNotifier.bazaarConversions.getString(key))
          .put("sellOfferPrice",
              (bazaarData.getJSONObject(key).getJSONArray("buy_summary").length() > 0
                  && bazaarData.getJSONObject(key).getJSONArray("sell_summary").length() > 0) ?
                  bazaarData.getJSONObject(key).getJSONArray("buy_summary").getJSONObject(0)
                      .getDouble("pricePerUnit") : 0)
          .put("buyOrderPrice",
              (bazaarData.getJSONObject(key).getJSONArray("sell_summary").length() > 0
                  && bazaarData.getJSONObject(key).getJSONArray("buy_summary").length() > 0) ?
                  bazaarData.getJSONObject(key).getJSONArray("sell_summary").getJSONObject(0)
                      .getDouble("pricePerUnit") : 0)
          .put("sellCount",
              bazaarData.getJSONObject(key).getJSONObject("quick_status").getLong("buyMovingWeek"))
          .put("buyCount",
              bazaarData.getJSONObject(key).getJSONObject("quick_status")
                  .getLong("sellMovingWeek"));

      double diff = currentProduct.getDouble("sellOfferPrice") * .99 - currentProduct
          .getDouble("buyOrderPrice");
      double profitFlowPerMinute =
          diff * Math.min(currentProduct.getLong("sellCount"), currentProduct.getLong("buyCount"))
              / 10080;
      bazaarDataFormatted.put(currentProduct.put("profitFlowPerMinute", profitFlowPerMinute));
    }

    bazaarDataFormatted = Utils.sortJSONArray(bazaarDataFormatted, "profitFlowPerMinute");

    JSONObject bazaarCache = new JSONObject();
    bazaarDataFormatted.forEach((data) -> {
      JSONObject jsonData = (JSONObject)data;
      bazaarCache.put(jsonData.getString("productId").toLowerCase(), data);
    });

    BazaarNotifier.bazaarCache = bazaarCache;
    BazaarNotifier.bazaarDataFormatted = bazaarDataFormatted;
  }
}
