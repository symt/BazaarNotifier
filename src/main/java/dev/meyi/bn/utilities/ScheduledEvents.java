package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

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
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(Suggester::basic, 5, 5, TimeUnit.SECONDS);
  }

  public void outdatedNotification() {
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
      if (!inOutdatedRequest) {
        inOutdatedRequest = true;
        try {
          if (BazaarNotifier.activeBazaar) {
            BazaarNotifier.bazaarDataRaw = Utils.getBazaarData();

            if (BazaarNotifier.orders.length() > 0) {
              Iterator<String> ordersIT = BazaarNotifier.orders.keys();
              while (ordersIT.hasNext()) {
                String key = ordersIT.next();
                for (int i = 0; i < BazaarNotifier.orders.getJSONArray(key).length(); i++) {
                  double price = BazaarNotifier.orders.getJSONArray(key).getJSONObject(i)
                      .getDouble("price");
                  if (BazaarNotifier.orders.getJSONArray(key).getJSONObject(i).getString("type")
                      .equals("buy")) {
                    // Buy
                    if (BazaarNotifier.bazaarDataRaw.getJSONObject(key).getJSONArray("sell_summary")
                        .getJSONObject(0)
                        .getDouble("pricePerUnit") - price > 0) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(new ChatComponentText(
                              EnumChatFormatting.DARK_PURPLE + "Buy Order"
                                  + EnumChatFormatting.GRAY + " for "
                                  + EnumChatFormatting.DARK_PURPLE + BazaarNotifier.orders
                                  .getJSONArray(key)
                                  .getJSONObject(i).getString("product").split("x")[0]
                                  + EnumChatFormatting.GRAY + "x " + EnumChatFormatting.DARK_PURPLE
                                  + BazaarNotifier.bazaarConversions.get(key)
                                  + EnumChatFormatting.YELLOW
                                  + " OUTDATED " + EnumChatFormatting.GRAY + "("
                                  + EnumChatFormatting.DARK_PURPLE + price
                                  + EnumChatFormatting.GRAY + ")"
                          ));
                      BazaarNotifier.orders.getJSONArray(key).remove(i);
                    }
                  } else {
                    // Sell
                    if (price - BazaarNotifier.bazaarDataRaw.getJSONObject(key)
                        .getJSONArray("buy_summary")
                        .getJSONObject(0)
                        .getDouble("pricePerUnit") > 0) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(new ChatComponentText(
                              EnumChatFormatting.BLUE + "Sell Offer"
                                  + EnumChatFormatting.GRAY + " of "
                                  + EnumChatFormatting.BLUE + BazaarNotifier.orders
                                  .getJSONArray(key)
                                  .getJSONObject(i).getString("product").split("x")[0]
                                  + EnumChatFormatting.GRAY + "x " + EnumChatFormatting.BLUE
                                  + BazaarNotifier.bazaarConversions.get(key)
                                  + EnumChatFormatting.YELLOW
                                  + " OUTDATED " + EnumChatFormatting.GRAY + "("
                                  + EnumChatFormatting.BLUE + price
                                  + EnumChatFormatting.GRAY + ")"
                          ));
                      BazaarNotifier.orders.getJSONArray(key).remove(i);
                    }
                  }
                }
                if (BazaarNotifier.orders.getJSONArray(key).length() == 0) {
                  ordersIT.remove();
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
