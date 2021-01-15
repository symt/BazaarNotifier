package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.Utils;
import java.math.BigDecimal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import org.json.JSONObject;

public class EventHandler {

  static JSONObject verify = null;
  static String[] productVerify = new String[2];

  @SubscribeEvent
  public void bazaarChatHandler(ClientChatReceivedEvent e) {
    String message = Utils
        .stripString(StringUtils.stripControlCodes(e.message.getUnformattedText()));
    if (message.startsWith("Buy Order Setup!") || message.startsWith("Sell Offer Setup!")) {
      if (productVerify[0] != null && productVerify[1] != null && productVerify[0]
          .equals(BazaarNotifier.bazaarConversionsReversed
              .getString(message.split("x ")[1].split(" for ")[0])) && productVerify[1]
          .equals(message.split("! ")[1].split(" for ")[0])) {
        BazaarNotifier.orders.put(verify);
        verify = null;
        productVerify = new String[2];
      }
    } else if (message.startsWith("[Bazaar] Your ") && message.endsWith(" was filled!")) {
      String item = message.split("x ")[1].split(" was ")[0];
      int amount = Integer.parseInt(message.split(" for ")[1].split("x ")[0].replaceAll(",", ""));
      int orderToRemove = 0;
      boolean found = false;
      double edgePrice;
      if (message.startsWith("[Bazaar] Your Buy Order")) {
        edgePrice = Double.MIN_VALUE;
        for (int i = 0; i < BazaarNotifier.orders.length(); i++) {
          JSONObject order = BazaarNotifier.orders.getJSONObject(i);
          if (order.getString("product").equalsIgnoreCase(item)
              && order.getInt("startAmount") == amount && order.getString("type").equals("buy")
              && order.getDouble("pricePerUnit") > edgePrice) {
            edgePrice = order.getDouble("pricePerUnit");
            orderToRemove = i;
            found = true;
          }
        }
      } else if (message.startsWith("[Bazaar] Your Sell Offer")) {
        edgePrice = Double.MAX_VALUE;
        for (int i = 0; i < BazaarNotifier.orders.length(); i++) {
          JSONObject order = BazaarNotifier.orders.getJSONObject(i);
          if (order.getString("product").equalsIgnoreCase(item)
              && order.getInt("startAmount") == amount && order.getString("type").equals("sell")
              && order.getDouble("pricePerUnit") < edgePrice) {
            edgePrice = order.getDouble("pricePerUnit");
            orderToRemove = i;
            found = true;
          }
        }
      }
      if (found) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
            BazaarNotifier.prefix + EnumChatFormatting.GREEN + "An order was filled!"));
        e.setCanceled(true);
        BazaarNotifier.orders.remove(orderToRemove);
      } else {
        /*
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
            BazaarNotifier.prefix + EnumChatFormatting.RED
                + "For some reason, you have an order that didn't successfully delete when filled! This message might be bugged. If all your orders are fine, ignore this message."));
      */
      }
    } else if (message.startsWith("Cancelled!")) {
      double refund = 0;
      int refundAmount = 0;
      String itemRefunded = "";
      if (message.endsWith("buy order!")) {
        refund = Double
            .parseDouble(message.split("Refunded ")[1].split(" coins")[0].replaceAll(",", ""));
        if (refund >= 10000) {
          refund = Math.round(refund);
        }
      } else if (message.endsWith("sell offer!")) {
        refundAmount = Integer
            .parseInt(message.split("Refunded ")[1].split("x ")[0].replaceAll(",", ""));
        itemRefunded = message.split("x ")[1].split(" from")[0];
      }
      for (int i = 0; i < BazaarNotifier.orders.length(); i++) {
        JSONObject order = BazaarNotifier.orders.getJSONObject(i);
        if (message.endsWith("buy order!") && order.getString("type").equals("buy")) {
          if (BigDecimal.valueOf(refund >= 10000 ? Math.round(order.getDouble("orderValue"))
              : order.getDouble("orderValue"))
              .compareTo(BigDecimal.valueOf(refund)) == 0) {
            BazaarNotifier.orders.remove(i);
            break;
          }
        } else if (message.endsWith("sell offer!") && order.getString("type").equals("sell")) {
          if (order.getString("product").equalsIgnoreCase(itemRefunded)
              && order.getInt("amountRemaining") == refundAmount) {
            BazaarNotifier.orders.remove(i);
            break;
          }
        }
      }
    } else if (message.startsWith("Bazaar! Claimed ")) {
      ChestTickHandler.updateBazaarOrders(
          ((GuiChest) Minecraft.getMinecraft().currentScreen).lowerChestInventory);
    }
  }

  @SubscribeEvent
  public void menuOpenedEvent(GuiOpenEvent e) {
    if (e.gui instanceof GuiChest && (BazaarNotifier.validApiKey || BazaarNotifier.apiKeyDisabled)
        && ((((GuiChest) e.gui).lowerChestInventory.hasCustomName() && (Utils
        .stripString(((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText())
        .startsWith("Bazaar") || Utils
        .stripString(((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText())
        .equalsIgnoreCase("How much do you want to pay?") || Utils
        .stripString(((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText())
        .matches("Confirm (Buy|Sell) (Order|Offer)")) || Utils
        .stripString(((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText())
        .contains("Bazaar")) || BazaarNotifier.forceRender)) {
      if (!BazaarNotifier.inBazaar) {
        BazaarNotifier.inBazaar = true;
      }
    }
    if (e.gui == null && BazaarNotifier.inBazaar) {
      BazaarNotifier.inBazaar = false;
    }
  }

  @SubscribeEvent
  public void disconnectEvent(ClientDisconnectionFromServerEvent e) {
    BazaarNotifier.inBazaar = false;
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