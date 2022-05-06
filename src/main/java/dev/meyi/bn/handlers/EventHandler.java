package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.calc.BankCalculator;

import java.io.IOException;
import java.math.BigDecimal;

import dev.meyi.bn.modules.calc.CraftingCalculator;
import dev.meyi.bn.utilities.Order;
import dev.meyi.bn.utilities.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import org.lwjgl.opengl.GL11;

public class EventHandler {

  static Order verify = null;
  static String[] productVerify = new String[2];
  
  @SubscribeEvent
  public void bazaarChatHandler(ClientChatReceivedEvent e) {

    if (!BazaarNotifier.activeBazaar) {
      return;
    }
    String message = StringUtils.stripControlCodes(e.message.getUnformattedText());

    if (message.startsWith("Buy Order Setup!") || message.startsWith("Sell Offer Setup!")) {
      if (productVerify[0] != null && productVerify[1] != null && productVerify[0]
          .equals(BazaarNotifier.bazaarConv.inverse()
              .get(message.split("x ", 2)[1].split(" for ")[0])) && productVerify[1]
          .equals(message.split("! ")[1].split(" for ")[0])) {
        BazaarNotifier.orders.add(verify);
        BankCalculator.getBazaarProfit();
        verify = null;
        productVerify = new String[2];
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
        for (int i = 0; i < BazaarNotifier.orders.size(); i++) {
          Order order = BazaarNotifier.orders.get(i);
          if (order.product.equalsIgnoreCase(item)
              && order.startAmount == amount && order.type.equals("buy")
              && order.pricePerUnit > edgePrice) {
            edgePrice = order.pricePerUnit;
            orderToRemove = i;
            found = true;
            BankCalculator.bazaarProfit -= BazaarNotifier.orders.get(orderToRemove).orderValue;
          }
        }
      } else if (message.startsWith("[Bazaar] Your Sell Offer")) {
        edgePrice = Double.MAX_VALUE;
        for (int i = 0; i < BazaarNotifier.orders.size(); i++) {
          Order order = BazaarNotifier.orders.get(i);
          if (order.product.equalsIgnoreCase(item)
              && order.startAmount == amount && order.type.equals("sell")
              && order.pricePerUnit < edgePrice) {


            edgePrice = order.pricePerUnit;
            orderToRemove = i;
            found = true;
            BankCalculator.bazaarProfit += BazaarNotifier.orders.get(orderToRemove).orderValue;
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
      for (int i = 0; i < BazaarNotifier.orders.size(); i++) {
        Order order = BazaarNotifier.orders.get(i);
        if (message.endsWith("buy order!") && order.type.equals("buy")) {
          if (BigDecimal.valueOf(refund >= 10000 ? Math.round(order.orderValue)
              : order.orderValue)
              .compareTo(BigDecimal.valueOf(refund)) == 0) {
            //BankCalculator.bazaarProfit -= (order.startAmount -order.getAmountRemaining())*order.pricePerUnit;
            BazaarNotifier.orders.remove(i);


            break;
          }
        } else if (message.endsWith("sell offer!") && order.type.equals("sell")) {
          if (order.product.equalsIgnoreCase(itemRefunded)
              && order.getAmountRemaining() == refundAmount) {
            BazaarNotifier.orders.remove(i);


            break;
          }
        }
      }
    } else if (message.startsWith("Bazaar! Claimed ")) {
      ChestTickHandler.lastScreenDisplayName = ""; // Force update on next tick
      // ChestTickHandler.updateBazaarOrders(
      //    ((GuiChest) Minecraft.getMinecraft().currentScreen).lowerChestInventory);
    }else if (message.startsWith("Bazaar! Bought")){
      BankCalculator.bazaarProfit -=
              Double.parseDouble(message.split(" for ")[1].split(" coins")[0].replaceAll(",",""));

    }else if (message.startsWith("Bazaar! Sold")){
      BankCalculator.bazaarProfit +=
              Double.parseDouble(message.split(" for ")[1].split(" coins")[0].replaceAll(",",""));
    }else if (message.startsWith("Welcome to Hypixel SkyBlock!")){
      BankCalculator.getPurse();
    }else if (message.startsWith("Your new API key is")){
      String apiKey = message.split("key is ")[1];
      try {
        if (Utils.validateApiKey(apiKey)) {
          Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                  BazaarNotifier.prefix + EnumChatFormatting.RED
                          + "Your api key has been set."));
          BazaarNotifier.config.api = apiKey;
          BazaarNotifier.validApiKey = true;
          BazaarNotifier.activeBazaar = true;
          CraftingCalculator.getUnlockedRecipes();
          BazaarNotifier.config.collectionCheckDisabled = false;
        } else {
          Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                  BazaarNotifier.prefix + EnumChatFormatting.RED
                          + "Your api key is invalid. Please run /api new to get a fresh api key & use that in /bn api (key)"));
          BazaarNotifier.validApiKey = false;
        }
      } catch (IOException r) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.RED
                        + "An error occurred when trying to set your api key. Please re-run the command to try again."));
        BazaarNotifier.validApiKey = false;
        r.printStackTrace();
      }
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
        if(!BankCalculator.orderWait) {
          BankCalculator.purseLast = BankCalculator.getPurse();
        }
      }
    }

    if (e.gui == null && BazaarNotifier.inBazaar) {
      BazaarNotifier.inBazaar = false;
      Thread t = new Thread(() -> {
        BankCalculator.orderWait = true;
        try {
          Thread.sleep(1000);
          BankCalculator.getBazaarProfit();
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
        BankCalculator.orderWait = false;
      });
      t.start();
    }

    if (e.gui == null && BazaarNotifier.inBank) {
      BazaarNotifier.inBank = false;
    }

    if (e.gui == null && BankCalculator.isOnDangerousPage){
      BankCalculator.isOnDangerousPage = false;
      Thread t = new Thread(() -> {
        try {
          Thread.sleep(1000);
          BankCalculator.bank += (BankCalculator.purseInBank - BankCalculator.getPurse());
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      });
      t.start();

    }

    if (e.gui instanceof GuiChest && ((((GuiChest) e.gui).lowerChestInventory.hasCustomName() &&
        StringUtils.stripControlCodes(
            ((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText())
            .contains("Bank"))) &&
        !StringUtils.stripControlCodes(
            ((GuiChest) e.gui).lowerChestInventory.getDisplayName().getUnformattedText())
            .contains("Bank Account Upgrades")) {
      BazaarNotifier.inBank = true;
    }
  }


  @SubscribeEvent
  public void disconnectEvent(ClientDisconnectionFromServerEvent e) {
    BazaarNotifier.inBazaar = false;
    BazaarNotifier.inBank = false;
    BankCalculator.isOnDangerousPage = false;
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
