package dev.meyi.bn;

import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.json.JSONArray;
import org.json.JSONObject;

public class EventHandler {

  JSONObject verify = null;
  String[] productVerify = new String[2];

  @SubscribeEvent
  public void bazaarChatHandler(ClientChatReceivedEvent e) {
    String message = Utils.stripString(e.message.getUnformattedText());
    if (message.startsWith("Buy Order Setup!") || message.startsWith("Sell Offer Setup!")) {
      if (productVerify[0] != null && productVerify[1] != null && productVerify[0]
          .equals(BazaarNotifier.bazaarConversionsReversed
              .getString(message.split("x ")[1].split(" for ")[0])) && productVerify[1]
          .equals(message.split("! ")[1].split(" for ")[0])) {
        if (!BazaarNotifier.orders.has(productVerify[0]) || BazaarNotifier.orders
            .isNull(productVerify[0])) {
          BazaarNotifier.orders.put(productVerify[0], new JSONArray());
        }
        BazaarNotifier.orders.getJSONArray(productVerify[0]).put(verify);

        verify = null;
        productVerify = new String[2];
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


  @SubscribeEvent
  public void menuOpenedEvent(GuiOpenEvent e) {
    if (e.gui instanceof GuiChest) {
      IInventory chestInventory = ((GuiChest) e.gui).lowerChestInventory;
      if (chestInventory.hasCustomName()) {
        if (Utils.stripString(chestInventory.getDisplayName().getUnformattedText())
            .contains("Confirm Buy Order") || Utils
            .stripString(chestInventory.getDisplayName().getUnformattedText())
            .contains("Confirm Sell Offer")) {
          final String type = Utils
              .stripString(chestInventory.getDisplayName().getUnformattedText())
              .contains("Confirm Sell Offer") ? "sell" : "buy";
          new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
              double price = Double.parseDouble(StringUtils.stripControlCodes(
                  chestInventory.getStackInSlot(13).getTagCompound().getCompoundTag("display")
                      .getTagList("Lore", 8).getStringTagAt(2)).split(" ")[3].replaceAll(",", ""));
              String product = StringUtils.stripControlCodes(
                  chestInventory.getStackInSlot(13).getTagCompound().getCompoundTag("display")
                      .getTagList("Lore", 8).getStringTagAt(4)).split("x ")[1];
              if (!BazaarNotifier.bazaarConversionsReversed
                  .has(product)) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                    BazaarNotifier.prefix + EnumChatFormatting.RED
                        + "The bazaar item you just put an order for doesn't exist. Please report this in the discord server"));
              } else {
                String productName = BazaarNotifier.bazaarConversionsReversed
                    .getString(product);
                String productWithAmount = StringUtils.stripControlCodes(
                    chestInventory.getStackInSlot(13).getTagCompound().getCompoundTag("display")
                        .getTagList("Lore", 8).getStringTagAt(4)).split(": ")[1];
                productVerify[0] = productName;
                productVerify[1] = productWithAmount;
                verify = new JSONObject().put("product", productWithAmount).put("type", type)
                    .put("price", price);
              }
            }
          }, 50);
        }
      }
    }
  }
}
