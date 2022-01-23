package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import dev.meyi.bn.modules.calc.SuggestionCalculator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;

public class ScheduledEvents {

  public static ScheduledEvents instance;

  public boolean inOutdatedRequest = false;

  private ScheduledEvents() {
    outdatedNotification();
    suggestionLoop();
    collectionLoop();
  }

  public static void create() {
    CraftingCalculator.getUnlockedRecipes();
    if (instance == null) {
      new ScheduledEvents();
    }
  }

  public void suggestionLoop() {
    Executors.newScheduledThreadPool(1)
        .scheduleAtFixedRate(SuggestionCalculator::basic, 5, 5, TimeUnit.SECONDS);
  }

  public void collectionLoop() {
    Executors.newScheduledThreadPool(1)
        .scheduleAtFixedRate(CraftingCalculator::getUnlockedRecipes, 5, 5, TimeUnit.MINUTES);
  }

  public void outdatedNotification() {
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
      if (!inOutdatedRequest) {
        inOutdatedRequest = true;
        try {
          if (BazaarNotifier.activeBazaar && (BazaarNotifier.validApiKey
              || BazaarNotifier.apiKeyDisabled)) {
            BazaarNotifier.bazaarDataRaw = Utils.getBazaarData();
            if (BazaarNotifier.orders.size() > 0) {
              for (int i = 0; i < BazaarNotifier.orders.size(); i++) {
                Order currentOrder = BazaarNotifier.orders.get(i);
                String key = BazaarNotifier.bazaarConversionsReversed
                    .get(currentOrder.product).getAsString();
                double price = currentOrder.pricePerUnit;
                if (currentOrder.type.equals("buy")) {
                  double diff =
                      BazaarNotifier.bazaarDataRaw.getAsJsonObject(key).getAsJsonArray("sell_summary")
                          .get(0).getAsJsonObject()
                          .get("pricePerUnit").getAsDouble() - price;
                  if (BazaarNotifier.bazaarDataRaw.getAsJsonObject(key).getAsJsonArray("sell_summary")
                      .get(0).getAsJsonObject().get("orders").getAsInt() != 1 && diff == 0
                      && !currentOrder.matchedOrder) {
                    currentOrder.matchedOrder = true;
                    currentOrder.outdatedOrder = false;
                    currentOrder.goodOrder = false;
                    currentOrder.currentNotification = "MATCHED";
                    if (BazaarNotifier.sendChatMessages) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(
                              Utils.chatNotification(price, i, "Buy Order", "MATCHED"));
                    }
                  } else if (diff > 0 && !currentOrder.outdatedOrder) {
                    if (BazaarNotifier.sendChatMessages) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(
                              Utils.chatNotification(price, i, "Buy Order", "OUTDATED"));
                    }
                    currentOrder.outdatedOrder = true;
                    currentOrder.matchedOrder = false;
                    currentOrder.goodOrder = false;
                    currentOrder.currentNotification = "OUTDATED";
                  } else if (diff == 0 &&
                      BazaarNotifier.bazaarDataRaw.getAsJsonObject(key).getAsJsonArray("sell_summary")
                          .get(0).getAsJsonObject().get("orders").getAsInt() == 1) {
                    if (currentOrder.outdatedOrder || currentOrder
                        .matchedOrder) {
                      if (BazaarNotifier.sendChatMessages) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(
                            Utils.chatNotification(price, i, "Buy Order", "REVIVED"));
                      }
                    }
                    currentOrder.outdatedOrder = false;
                    currentOrder.matchedOrder = false;
                    currentOrder.goodOrder = true;
                  }
                } else {
                  double diff = price - BazaarNotifier.bazaarDataRaw.getAsJsonObject(key)
                      .getAsJsonArray("buy_summary")
                      .get(0).getAsJsonObject()
                      .get("pricePerUnit").getAsDouble();
                  if (BazaarNotifier.bazaarDataRaw.get(key).getAsJsonObject().get("buy_summary").getAsJsonArray()
                      .get(0).getAsJsonObject().get("orders").getAsInt() != 1 && diff == 0 && !currentOrder.matchedOrder) {
                    currentOrder.matchedOrder = true;
                    currentOrder.outdatedOrder = false;
                    currentOrder.goodOrder = false;
                    currentOrder.currentNotification = "MATCHED";
                    if (BazaarNotifier.sendChatMessages) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(
                              Utils.chatNotification(price, i, "Sell Offer", "MATCHED"));
                    }
                  } else if (diff > 0 && !currentOrder.outdatedOrder) {
                    currentOrder.outdatedOrder = true;
                    currentOrder.matchedOrder = false;
                    currentOrder.goodOrder = false;
                    currentOrder.currentNotification = "OUTDATED";
                    if (BazaarNotifier.sendChatMessages) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(
                              Utils.chatNotification(price, i, "Sell Offer", "OUTDATED"));
                    }
                  } else if (diff == 0
                      && BazaarNotifier.bazaarDataRaw.getAsJsonObject(key).getAsJsonArray("buy_summary")
                      .get(0).getAsJsonObject().get("orders").getAsInt() == 1) {
                    if (currentOrder.outdatedOrder || currentOrder
                        .matchedOrder) {
                      if (BazaarNotifier.sendChatMessages) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(
                            Utils.chatNotification(price, i, "Sell Offer", "REVIVED"));
                      }
                    }
                    currentOrder.outdatedOrder = false;
                    currentOrder.matchedOrder = false;
                    currentOrder.goodOrder = true;
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
