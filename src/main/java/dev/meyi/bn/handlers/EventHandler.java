package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import java.math.BigDecimal;

import dev.meyi.bn.utilities.ProfitCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import org.json.JSONObject;
import org.lwjgl.opengl.GL11;

public class EventHandler {

  static JSONObject verify = null;
  static String[] productVerify = new String[2];

  @SubscribeEvent
  public void bazaarChatHandler(ClientChatReceivedEvent e) {
    if (!BazaarNotifier.activeBazaar) {
      return;
    }
    String message = StringUtils.stripControlCodes(e.message.getUnformattedText());
    if (message.startsWith("Buy Order Setup!") || message.startsWith("Sell Offer Setup!")) {
      if (productVerify[0] != null && productVerify[1] != null && productVerify[0]
              .equals(BazaarNotifier.bazaarConversionsReversed
                      .getString(message.split("x ", 2)[1].split(" for ")[0])) && productVerify[1]
              .equals(message.split("! ")[1].split(" for ")[0])) {
        BazaarNotifier.orders.put(verify);
        verify = null;
        productVerify = new String[2];
      }
      if(message.startsWith("Sell Offer Setup!") && !BazaarNotifier.inBazaar){
          ProfitCalculator.moneyNotFromBazaar -= BazaarNotifier.orders.getJSONObject(BazaarNotifier.orders.length()-1).getDouble("orderValue");
      }

    } else if (message.startsWith("[Bazaar] Your ") && message.endsWith(" was filled!")) {
      String item = message.split("x ", 2)[1].split(" was ")[0];
      int amount = Integer
              .parseInt(message.split(" for ")[1].split("x ", 2)[0].replaceAll(",", ""));
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
            if(!BazaarNotifier.inBazaar) {
              ProfitCalculator.moneyNotFromBazaar =- order.getInt("orderValue");
            }
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
        BazaarNotifier.orders.remove(orderToRemove);
      } else {
        System.err.println("There is some error in removing your order from the list!!!");
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
                .parseInt(message.split("Refunded ")[1].split("x ", 2)[0].replaceAll(",", ""));
        itemRefunded = message.split("x ", 2)[1].split(" from")[0];
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
      ChestTickHandler.lastScreenDisplayName = ""; // Force update on next tick
      // ChestTickHandler.updateBazaarOrders(
      //    ((GuiChest) Minecraft.getMinecraft().currentScreen).lowerChestInventory);
    }
  }

  @SubscribeEvent
  public void menuOpenedEvent(GuiOpenEvent e) {
    if (e.gui instanceof GuiChest && (BazaarNotifier.validApiKey || BazaarNotifier.apiKeyDisabled)
            && ((((GuiChest) e.gui).lowerChestInventory.hasCustomName() && (StringUtils
            .stripControlCodes(
                    ((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText())
            .startsWith("Bazaar") || StringUtils.stripControlCodes(
                    ((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText())
            .equalsIgnoreCase("How much do you want to pay?") || StringUtils.stripControlCodes(
                    ((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText())
            .matches("Confirm (Buy|Sell) (Order|Offer)")) || StringUtils.stripControlCodes(
                    ((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText())
            .contains("Bazaar")) || BazaarNotifier.forceRender)) {
      if (!BazaarNotifier.inBazaar) {
        BazaarNotifier.inBazaar = true;
        ProfitCalculator.moneyNotFromBazaar += ProfitCalculator.calculateProfit() - ProfitCalculator.moneyOnBazaarLeave;
      }
    }

    if (e.gui == null && BazaarNotifier.inBazaar) {
      BazaarNotifier.inBazaar = false;
      ProfitCalculator.moneyOnBazaarLeave = ProfitCalculator.calculateProfit();
    }

    if (e.gui == null && BazaarNotifier.inBank) {
      BazaarNotifier.inBank = false;
    }

    if (e.gui instanceof GuiChest && ((((GuiChest) e.gui).lowerChestInventory.hasCustomName() &&
            StringUtils.stripControlCodes(((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText()).contains("Bank Account"))) &&
            !StringUtils.stripControlCodes(((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText()).contains("Bank Account Upgrades")){
              BazaarNotifier.inBank = true;
    }
  }


  @SubscribeEvent
  public void disconnectEvent(ClientDisconnectionFromServerEvent e) {
    BazaarNotifier.inBazaar = false;
    BazaarNotifier.inBank = false;
    ProfitCalculator.moneyOnBazaarLeave = ProfitCalculator.calculateProfit();
  }

  @SubscribeEvent
  public void renderBazaarEvent(BackgroundDrawnEvent e) {
    if (BazaarNotifier.inBazaar && BazaarNotifier.activeBazaar) {
     GL11.glTranslated(0, 0, 1);
      BazaarNotifier.modules.drawAllOutlines();
      BazaarNotifier.modules.drawAllModules();
      GL11.glTranslated(0, 0, -1);
    }
  }

  // TODO: Look for fix to old animations?
}
