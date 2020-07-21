package dev.meyi;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.json.JSONArray;
import org.json.JSONObject;

public class EventHandler {

  @SubscribeEvent
  public void bazaarChatHandler(ClientChatReceivedEvent e) {
    String message = Utils.stripString(e.message.getUnformattedText());
    double price;
    String type;

    if (message.startsWith("Buy Order Setup!") || message.startsWith("Sell Offer Setup!")) {
      type = "buy";
      price =
          Double.parseDouble(message.split(" ")[message.split(" ").length - 2].replaceAll(",", ""))
              / Double
              .parseDouble(message.split(" ")[3].substring(0, message.split(" ")[3].length() - 1)
                  .replaceAll(",", ""));

      if (message.startsWith("Sell Offer Setup!")) {
        type = "sell";
        price /= .99;
      }
      price = Utils.round(price, 1);

      if (!BazaarNotifier.bazaarConversionsReversed
          .has(message.split("x ")[1].split(" for ")[0])) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
            BazaarNotifier.prefix + EnumChatFormatting.RED
                + "The bazaar item you just put an order for doesn't exist. Please report this in the discord server"));
      } else {
        String productName = BazaarNotifier.bazaarConversionsReversed
            .getString(message.split("x ")[1].split(" for ")[0]);

        String productWithAmount = message.split("! ")[1].split(" for ")[0];

        if (!BazaarNotifier.orders.has(productName) || BazaarNotifier.orders.isNull(productName)) {
          BazaarNotifier.orders.put(productName, new JSONArray());
        }
        BazaarNotifier.orders.getJSONArray(productName)
            .put(new JSONObject().put("product", productWithAmount).put("type", type)
                .put("price", price));
      }
    } else if (message.startsWith("[Bazaar] Your Buy Order for")) {
      String productName = BazaarNotifier.bazaarConversionsReversed
          .getString(message.split("x ")[1].split(" was ")[0]);
      String productWithAmount = message.split("for ")[1].split(" was ")[0];

      if (BazaarNotifier.orders.has(productName) && !BazaarNotifier.orders.isNull(productName)) {
        int orderToRemove = 0;
        boolean found = false;
        double highestPrice = Double.MIN_VALUE;
        for (int i = 0; i < BazaarNotifier.orders.getJSONArray(productName).length(); i++) {
          JSONObject order = BazaarNotifier.orders.getJSONArray(productName).getJSONObject(i);
          if (order.getString("type").equals("buy") && order.getString("product")
              .equals(productWithAmount)) {
            if (order.getDouble("price") > highestPrice) {
              highestPrice = order.getDouble("price");
              orderToRemove = i;
              found = true;
            }
          }
        }
        if (found) {
          BazaarNotifier.orders.getJSONArray(productName).remove(orderToRemove);
        }
        if (BazaarNotifier.orders.getJSONArray(productName).length() == 0) {
          BazaarNotifier.orders.remove(productName);
        }
      }
    } else if (message.startsWith("[Bazaar] Your Sell Offer for")) {
      String productName = BazaarNotifier.bazaarConversionsReversed
          .getString(message.split("x ")[1].split(" was ")[0]);
      String productWithAmount = message.split("for ")[1].split(" was ")[0];

      if (BazaarNotifier.orders.has(productName) && !BazaarNotifier.orders.isNull(productName)) {
        int orderToRemove = 0;
        boolean found = false;
        double lowestPrice = Double.MAX_VALUE;
        for (int i = 0; i < BazaarNotifier.orders.getJSONArray(productName).length(); i++) {
          JSONObject order = BazaarNotifier.orders.getJSONArray(productName).getJSONObject(i);
          if (order.getString("type").equals("sell") && order.getString("product")
              .equals(productWithAmount)) {
            if (order.getDouble("price") < lowestPrice) {
              lowestPrice = order.getDouble("price");
              orderToRemove = i;
              found = true;
            }
          }
        }
        if (found) {
          BazaarNotifier.orders.getJSONArray(productName).remove(orderToRemove);
        }
        if (BazaarNotifier.orders.getJSONArray(productName).length() == 0) {
          BazaarNotifier.orders.remove(productName);
        }
      }
    }
  }
}
