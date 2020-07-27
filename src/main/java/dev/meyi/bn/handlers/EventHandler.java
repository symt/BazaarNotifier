package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.json.JSONObject;

public class EventHandler {

  static JSONObject verify = null;
  static String[] productVerify = new String[2];

  @SubscribeEvent
  public void bazaarChatHandler(ClientChatReceivedEvent e) {
    String message = Utils.stripString(e.message.getUnformattedText());
    if (message.startsWith("Buy Order Setup!") || message.startsWith("Sell Offer Setup!")) {
      if (productVerify[0] != null && productVerify[1] != null && productVerify[0]
          .equals(BazaarNotifier.bazaarConversionsReversed
              .getString(message.split("x ")[1].split(" for ")[0])) && productVerify[1]
          .equals(message.split("! ")[1].split(" for ")[0])) {
        BazaarNotifier.orders.put(verify);
        verify = null;
        productVerify = new String[2];
      }
    } else if (message.matches("\\[Bazaar] Your (Buy|Sell) (Order|Offer) for")) {
      int amount = Integer.parseInt(message.split(" for ")[1].split("x ")[0].replaceAll(",", ""));
      int orderToRemove = 0;
      boolean found = false;
      double edgePrice = 0;
      for (int i = 0; i < BazaarNotifier.orders.length(); i++) {
        JSONObject order = BazaarNotifier.orders.getJSONObject(i);
        if (order.getInt("startAmount") == amount) {
          if ((order.getString("type").equals("buy") ? order.getDouble("price") > edgePrice
              : order.getDouble("price") < edgePrice)) {
            edgePrice = order.getDouble("price");
            orderToRemove = i;
            found = true;
          }
        }
      }
      if (found) {
        BazaarNotifier.orders.remove(orderToRemove);
      }
    } else if (message.startsWith("Cancelled!")) {
      double refund = 0d;
      int refundAmount = 0;
      String itemRefunded = "";
      if (message.endsWith("buy order!")) {
        refund = Double
            .parseDouble(message.split("Refunded ")[1].split(" coins")[0].replaceAll(",", ""));
      } else if (message.endsWith("sell offer!")) {
        refundAmount = Integer
            .parseInt(message.split("Refunded ")[1].split("x ")[0].replaceAll(",", ""));
        itemRefunded = message.split("x ")[1].split(" from")[0];
      }
      for (int i = 0; i < BazaarNotifier.orders.length(); i++) {
        JSONObject order = BazaarNotifier.orders.getJSONObject(i);
        if (message.endsWith("buy order!")) {
          if (Double.compare(order.getDouble("orderValue"), refund) == 0) {
            BazaarNotifier.orders.remove(i);
          }
        } else if (message.endsWith("sell offer!")) {
          if (order.getString("product").equalsIgnoreCase(itemRefunded)
              && order.getInt("amountRemaining") == refundAmount) {
            BazaarNotifier.orders.remove(i);
          }
        }
      }
    }
  }

  @SubscribeEvent
  public void menuOpenedEvent(GuiOpenEvent e) {
    if (e.gui instanceof GuiChest && BazaarNotifier.validApiKey
        && ((((GuiChest) e.gui).lowerChestInventory.hasCustomName() && Utils
        .stripString(((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText())
        .startsWith("Bazaar")) || BazaarNotifier.forceRender)) {
      if (!BazaarNotifier.inBazaar) {
        BazaarNotifier.inBazaar = true;
      }
    }
    if (e.gui == null && BazaarNotifier.inBazaar) {
      BazaarNotifier.inBazaar = false;
    }
  }

  @SubscribeEvent
  public void renderBazaarEvent(BackgroundDrawnEvent e) {
    if (BazaarNotifier.inBazaar) {
      BazaarNotifier.modules.drawAllModules();
    }
  }

  @SubscribeEvent
  public void renderOutlines(RenderGameOverlayEvent.Post e) {
    if (BazaarNotifier.inBazaar) {
      BazaarNotifier.modules.drawAllOutlines();
    }
  }
}
