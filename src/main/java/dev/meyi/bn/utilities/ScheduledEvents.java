package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.resp.Order;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import dev.meyi.bn.modules.calc.SuggestionCalculator;
import net.minecraft.client.Minecraft;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScheduledEvents {

  public static ScheduledEvents instance;

  public boolean inOutdatedRequest = false;

  private ScheduledEvents() {
    outdatedNotification();
    craftingLoop();
    suggestionLoop();
    collectionLoop();
  }

  public static void create() {
    CraftingCalculator.getUnlockedRecipes();
    if (instance == null) {
      new ScheduledEvents();
    }
  }
  public void craftingLoop(){
    Executors.newScheduledThreadPool(1)
        .scheduleAtFixedRate(CraftingCalculator::getBestEnchantRecipes, 5,5,TimeUnit.SECONDS);
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
                String key = BazaarNotifier.bazaarConv.inverse()
                    .get(currentOrder.product);
                double price = currentOrder.pricePerUnit;
                if (currentOrder.type.equals("buy")) {
                  double diff =
                      BazaarNotifier.bazaarDataRaw.products.get(key).sell_summary.get(0).pricePerUnit - price;
                  if (BazaarNotifier.bazaarDataRaw.products.get(key).sell_summary.get(0).orders != 1 && diff == 0
                      && currentOrder.orderStatus != Order.OrderType.MATCHED) {
                    currentOrder.orderStatus = Order.OrderType.MATCHED;
                    currentOrder.currentNotification = "MATCHED";
                    if (BazaarNotifier.config.showChatMessages) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(
                              Utils.chatNotification(price, i, "Buy Order", "MATCHED"));
                    }
                  } else if (diff > 0 && currentOrder.orderStatus != Order.OrderType.OUTDATED) {
                    if (BazaarNotifier.config.showChatMessages) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(
                              Utils.chatNotification(price, i, "Buy Order", "OUTDATED"));
                    }
                    currentOrder.orderStatus = Order.OrderType.OUTDATED;
                    currentOrder.currentNotification = "OUTDATED";
                  } else if (diff == 0 &&
                      BazaarNotifier.bazaarDataRaw.products.get(key).sell_summary.get(0).orders == 1) {
                    if (currentOrder.orderStatus == Order.OrderType.OUTDATED ||
                            currentOrder.orderStatus == Order.OrderType.MATCHED) {
                      if (BazaarNotifier.config.showChatMessages) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(
                            Utils.chatNotification(price, i, "Buy Order", "REVIVED"));
                      }
                    }
                    currentOrder.orderStatus = Order.OrderType.BEST;
                  }
                } else {
                  double diff = price - BazaarNotifier.bazaarDataRaw.products.get(key).buy_summary.get(0).pricePerUnit;
                  if (BazaarNotifier.bazaarDataRaw.products.get(key).buy_summary.get(0).orders != 1 &&
                          diff == 0 && currentOrder.orderStatus != Order.OrderType.MATCHED) {
                    currentOrder.orderStatus = Order.OrderType.MATCHED;
                    currentOrder.currentNotification = "MATCHED";
                    if (BazaarNotifier.config.showChatMessages) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(
                              Utils.chatNotification(price, i, "Sell Offer", "MATCHED"));
                    }
                  } else if (diff > 0 && currentOrder.orderStatus != Order.OrderType.OUTDATED) {
                    currentOrder.orderStatus = Order.OrderType.OUTDATED;
                    currentOrder.currentNotification = "OUTDATED";
                    if (BazaarNotifier.config.showChatMessages) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(
                              Utils.chatNotification(price, i, "Sell Offer", "OUTDATED"));
                    }
                  } else if (diff == 0
                      && BazaarNotifier.bazaarDataRaw.products.get(key).buy_summary.get(0).orders == 1) {
                    if (currentOrder.orderStatus == Order.OrderType.OUTDATED ||
                            currentOrder.orderStatus == Order.OrderType.MATCHED) {
                      if (BazaarNotifier.config.showChatMessages) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(
                            Utils.chatNotification(price, i, "Sell Offer", "REVIVED"));
                      }
                    }
                    currentOrder.orderStatus = Order.OrderType.BEST;
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
