package dev.meyi.bn.modules.calc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.util.Iterator;
import java.util.Map;


public class SuggestionCalculator {

  public static void basic() {
    try {
      if (BazaarNotifier.validApiKey || BazaarNotifier.apiKeyDisabled) {
        JsonObject bazaarData = BazaarNotifier.bazaarDataRaw;
        Iterator<Map.Entry<String, JsonElement>> bazaarKeys = bazaarData.entrySet().iterator();
        JsonArray bazaarDataFormatted = new JsonArray();

        while (bazaarKeys.hasNext()) {
          Map.Entry<String, JsonElement> keys = bazaarKeys.next();
          String key = keys.getKey();
          JsonObject product = bazaarData.getAsJsonObject(key);

          if (!BazaarNotifier.bazaarConv.containsKey(key)) {
            BazaarNotifier.bazaarConv.put(key, key);
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("ADDED ITEMS"));
          }
          JsonObject currentProduct = new JsonObject();
          currentProduct.addProperty("productId", BazaarNotifier.bazaarConv.get(key));
          currentProduct.addProperty("sellOfferPrice",
                  (product.getAsJsonArray("buy_summary").size() > 0
                      && product.getAsJsonArray("sell_summary").size() > 0) ?
                      product.getAsJsonArray("buy_summary").get(0).getAsJsonObject()
                          .get("pricePerUnit").getAsDouble() : 0);
          currentProduct.addProperty("buyOrderPrice",
                  (product.getAsJsonArray("sell_summary").size() > 0
                      && product.getAsJsonArray("buy_summary").size() > 0) ?
                      product.getAsJsonArray("sell_summary").get(0).getAsJsonObject()
                          .get("pricePerUnit").getAsDouble() : 0);
          currentProduct.addProperty("sellCount",
                  product.getAsJsonObject("quick_status")
                      .get("buyMovingWeek").getAsLong());
          currentProduct.addProperty("buyCount",
                  product.getAsJsonObject("quick_status")
                      .get("sellMovingWeek").getAsLong());

          double diff = currentProduct.get("sellOfferPrice").getAsDouble() * .99d - currentProduct
              .get("buyOrderPrice").getAsDouble();
          double profitFlowPerMinute =
              (currentProduct.get("sellCount").getAsLong() + currentProduct.get("buyCount").getAsLong() == 0) ? 0 :
                  ((currentProduct.get("sellCount").getAsLong() * currentProduct.get("buyCount").getAsLong()) / (
                      10080d
                          * (currentProduct.get("sellCount").getAsLong() + currentProduct
                          .get("buyCount").getAsLong())))
                      * diff;
          currentProduct.addProperty("profitFlowPerMinute", profitFlowPerMinute);
          bazaarDataFormatted.add(currentProduct);
        }

        bazaarDataFormatted = Utils.sortJSONArray(bazaarDataFormatted, "profitFlowPerMinute");

        JsonObject bazaarCache = new JsonObject();
        bazaarDataFormatted.forEach((data) -> {
          JsonObject jsonData = (JsonObject) data;
          bazaarCache.add(jsonData.get("productId").getAsString().toLowerCase(), data);
        });

        BazaarNotifier.bazaarCache = bazaarCache;
        BazaarNotifier.bazaarDataFormatted = bazaarDataFormatted;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String setSuggestionLength(int length) {
    if (length > BazaarNotifier.bazaarDataRaw.entrySet().size()) {
      return length + " is too long";
    } else {
      BazaarNotifier.config.suggestionListLength = length;
      return "Item list size set to " + length;
    }
  }


}
