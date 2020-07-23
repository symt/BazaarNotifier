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
    Executors.newScheduledThreadPool(1)
        .scheduleAtFixedRate(Suggester::basic, 5, 5, TimeUnit.SECONDS);
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
                    if (BazaarNotifier.bazaarDataRaw.getJSONObject(key).getJSONArray("sell_summary")
                        .getJSONObject(0).getInt("orders") != 1 &&
                        BazaarNotifier.bazaarDataRaw.getJSONObject(key).getJSONArray("sell_summary")
                            .getJSONObject(0)
                            .getDouble("pricePerUnit") == price && !BazaarNotifier.bazaarDataRaw
                        .getJSONObject(key).has("wasMatched")) {
                      BazaarNotifier.bazaarDataRaw.getJSONObject(key).put("wasMatched", true);
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(chatNotification(key, price, i, "Buy Order", "MATCHED"));
                    } else if (
                        BazaarNotifier.bazaarDataRaw.getJSONObject(key).getJSONArray("sell_summary")
                            .getJSONObject(0)
                            .getDouble("pricePerUnit") - price > 0) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(chatNotification(key, price, i, "Buy Order", "OUTDATED"));
                      BazaarNotifier.orders.getJSONArray(key).remove(i);
                    }
                  } else {
                    if (BazaarNotifier.bazaarDataRaw.getJSONObject(key).getJSONArray("buy_summary")
                        .getJSONObject(0).getInt("orders") != 1 &&
                        BazaarNotifier.bazaarDataRaw.getJSONObject(key).getJSONArray("buy_summary")
                            .getJSONObject(0)
                            .getDouble("pricePerUnit") == price && !BazaarNotifier.bazaarDataRaw
                        .getJSONObject(key).has("wasMatched")) {
                      BazaarNotifier.bazaarDataRaw.getJSONObject(key).put("wasMatched", true);
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(chatNotification(key, price, i, "Sell Offer", "MATCHED"));
                    } else if (price - BazaarNotifier.bazaarDataRaw.getJSONObject(key)
                        .getJSONArray("buy_summary")
                        .getJSONObject(0)
                        .getDouble("pricePerUnit") > 0) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(
                              chatNotification(key, price, i, "Sell Offer", "OUTDATED"));
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


  private ChatComponentText chatNotification(String key, double price, int i, String type,
      String notification) {
    EnumChatFormatting messageColor =
        (type.equalsIgnoreCase("Buy Order")) ? EnumChatFormatting.DARK_PURPLE
            : EnumChatFormatting.BLUE;
    return new ChatComponentText(
        messageColor + type
            + EnumChatFormatting.GRAY + " for "
            + messageColor + BazaarNotifier.orders
            .getJSONArray(key)
            .getJSONObject(i).getString("product").split("x")[0]
            + EnumChatFormatting.GRAY + "x " + messageColor
            + BazaarNotifier.bazaarConversions.get(key)
            + EnumChatFormatting.YELLOW
            + " " + notification + " " + EnumChatFormatting.GRAY + "("
            + messageColor + BazaarNotifier.df.format(price)
            + EnumChatFormatting.GRAY + ")"
    );
  }
}
