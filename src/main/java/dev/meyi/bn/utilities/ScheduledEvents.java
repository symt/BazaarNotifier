package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import org.json.JSONObject;

public class ScheduledEvents {

  public static ScheduledEvents instance;

  public boolean inOutdatedRequest = false;

  private ScheduledEvents() {
    outdatedNotification();
    suggestionLoop();
  }

  public static void create() {
    if (instance == null) {
      new ScheduledEvents();
    }
  }

  public void suggestionLoop() {
    Executors.newScheduledThreadPool(1)
        .scheduleAtFixedRate(Suggester::basic, 5, 5, TimeUnit.SECONDS);
  }

  public void outdatedNotification() {
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
      if (!inOutdatedRequest) {
        inOutdatedRequest = true;
        try {
          if (BazaarNotifier.activeBazaar && (BazaarNotifier.validApiKey || BazaarNotifier.apiKeyDisabled)) {
            BazaarNotifier.bazaarDataRaw = Utils.getBazaarData();
            if (BazaarNotifier.orders.length() > 0) {
              for (int i = 0; i < BazaarNotifier.orders.length(); i++) {
                JSONObject currentOrder = BazaarNotifier.orders.getJSONObject(i);
                String key = BazaarNotifier.bazaarConversionsReversed
                    .getString(currentOrder.getString("product"));
                double price = currentOrder.getDouble("pricePerUnit");
                if (currentOrder.getString("type").equals("buy")) {
                  double diff =
                      BazaarNotifier.bazaarDataRaw.getJSONObject(key).getJSONArray("sell_summary")
                          .getJSONObject(0)
                          .getDouble("pricePerUnit") - price;
                  if (BazaarNotifier.bazaarDataRaw.getJSONObject(key).getJSONArray("sell_summary")
                      .getJSONObject(0).getInt("orders") != 1 && diff == 0
                      && !currentOrder.getBoolean("matchedOrder")) {
                    currentOrder.put("matchedOrder", true).put("outdatedOrder", false)
                        .put("goodOrder", false)
                        .put("currentNotification", "MATCHED");
                    Minecraft.getMinecraft().thePlayer
                        .addChatMessage(
                            Utils.chatNotification(key, price, i, "Buy Order", "MATCHED"));
                  } else if (diff > 0 && !currentOrder.getBoolean("outdatedOrder")) {
                    Minecraft.getMinecraft().thePlayer
                        .addChatMessage(
                            Utils.chatNotification(key, price, i, "Buy Order", "OUTDATED"));
                    currentOrder.put("outdatedOrder", true).put("matchedOrder", false)
                        .put("goodOrder", false)
                        .put("currentNotification", "OUTDATED");
                  } else if (diff == 0 &&
                      BazaarNotifier.bazaarDataRaw.getJSONObject(key).getJSONArray("sell_summary")
                          .getJSONObject(0).getInt("orders") == 1) {
                    if (currentOrder.getBoolean("outdatedOrder") || currentOrder
                        .getBoolean("matchedOrder")) {
                      Minecraft.getMinecraft().thePlayer.addChatMessage(
                          Utils.chatNotification(key, price, i, "Buy Order", "REVIVED"));
                    }
                    currentOrder.put("outdatedOrder", false).put("matchedOrder", false)
                        .put("goodOrder", true);
                  }
                } else {
                  double diff = price - BazaarNotifier.bazaarDataRaw.getJSONObject(key)
                      .getJSONArray("buy_summary")
                      .getJSONObject(0)
                      .getDouble("pricePerUnit");
                  if (BazaarNotifier.bazaarDataRaw.getJSONObject(key).getJSONArray("buy_summary")
                      .getJSONObject(0).getInt("orders") != 1 && diff == 0 && !currentOrder
                      .getBoolean("matchedOrder")) {
                    currentOrder.put("matchedOrder", true).put("outdatedOrder", false)
                        .put("goodOrder", false)
                        .put("currentNotification", "MATCHED");
                    Minecraft.getMinecraft().thePlayer
                        .addChatMessage(
                            Utils.chatNotification(key, price, i, "Sell Offer", "MATCHED"));
                  } else if (diff > 0 && !currentOrder.getBoolean("outdatedOrder")) {
                    currentOrder.put("outdatedOrder", true).put("matchedOrder", false)
                        .put("goodOrder", false)
                        .put("currentNotification", "OUTDATED");
                    Minecraft.getMinecraft().thePlayer
                        .addChatMessage(
                            Utils.chatNotification(key, price, i, "Sell Offer", "OUTDATED"));
                  } else if (diff == 0
                      && BazaarNotifier.bazaarDataRaw.getJSONObject(key).getJSONArray("buy_summary")
                      .getJSONObject(0).getInt("orders") == 1) {
                    if (currentOrder.getBoolean("outdatedOrder") || currentOrder
                        .getBoolean("matchedOrder")) {
                      Minecraft.getMinecraft().thePlayer.addChatMessage(
                          Utils.chatNotification(key, price, i, "Sell Offer", "REVIVED"));
                    }
                    currentOrder.put("outdatedOrder", false).put("matchedOrder", false)
                        .put("goodOrder", true);
                  }
                }
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        inOutdatedRequest = false;
      }
    }, 0, 2, TimeUnit.SECONDS);
  }
}
