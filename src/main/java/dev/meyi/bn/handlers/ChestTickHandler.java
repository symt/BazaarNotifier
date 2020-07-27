package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import org.json.JSONObject;

public class ChestTickHandler {

  String lastScreenDisplayName = "";
  // /blockdata x y z {CustomName:"___"} << For Custom Chest Name Testing

  @SubscribeEvent
  public void onChestTick(TickEvent e) {
    if (e.phase == Phase.END) {
      if (Minecraft.getMinecraft().currentScreen instanceof GuiChest && BazaarNotifier.inBazaar) {
        IInventory chest = ((GuiChest) Minecraft.getMinecraft().currentScreen).lowerChestInventory;
        if (chest.hasCustomName() && !lastScreenDisplayName
            .equalsIgnoreCase(Utils.stripString(chest.getDisplayName().getUnformattedText()))) {
          lastScreenDisplayName = Utils.stripString(chest.getDisplayName().getUnformattedText());
          String chestName = Utils.stripString(chest.getDisplayName().getUnformattedText())
              .toLowerCase();
          if (chestName.contains("bazaar orders")) {
            updateBazaarOrders(chest);
          } else if (chestName.equals("confirm buy order") || chestName
              .equals("confirm sell offer")) {
            orderConfirmation(chest);
          } else if (chestName.equalsIgnoreCase("order options")) {
            // trying to cancel buy order OR sell offer
          }
        }
      }
    }
  }

  private void updateBazaarOrders(IInventory chest) {
    for (int i = 0; i < chest.getSizeInventory(); i++) {
      if (chest.getStackInSlot(i) != null
          && Item.itemRegistry.getIDForObject(chest.getStackInSlot(i).getItem()) != 160    // Glass
          && Item.itemRegistry.getIDForObject(chest.getStackInSlot(i).getItem()) != 262) { // Arrow
        NBTTagList lore = chest.getStackInSlot(13).getTagCompound().getCompoundTag("display")
            .getTagList("Lore", 8);

        String displayName = chest.getStackInSlot(13).getDisplayName().split(": ")[1];
        String type = chest.getStackInSlot(13).getDisplayName().split(": ")[0].toLowerCase();

        if (BazaarNotifier.bazaarConversionsReversed.has(displayName)) {
          String product = BazaarNotifier.bazaarConversionsReversed.getString(displayName);
          int amountLeft = -1;
          double price = Double
              .parseDouble(
                  lore.getStringTagAt((lore.getStringTagAt(3).startsWith("Filled:")) ? 5 : 4)
                      .replaceAll(",", "").split(" ")[3]);

          int orderInQuestion = -1;
          for (int j = 0; j < BazaarNotifier.orders.length(); j++) {
            JSONObject order = BazaarNotifier.orders.getJSONObject(j);
            if (Double.compare(order.getDouble("price"), price) == 0 && type
                .equals(order.getString("type"))) {
              orderInQuestion = j;
              break;
            }
          }
          if (orderInQuestion != -1) {
            int totalAmount = BazaarNotifier.orders.getJSONObject(orderInQuestion).getInt("amountRemaining");
            if (lore.getStringTagAt(3).startsWith("Filled:")) {
              if (lore.getStringTagAt(3).split(" ")[2].equals("100%")) {
                amountLeft = 0;
              } else {
                int amountFulfilled = 0;
                for (int j = 8; j < lore.tagCount(); j++) {
                  if (lore.getStringTagAt(j).isEmpty()) {
                    break;
                  }
                  amountFulfilled += Integer.parseInt(
                      lore.getStringTagAt(j).replaceAll(",", "").split("x ")[0].substring(2));
                }
                amountLeft = totalAmount - amountFulfilled;
              }
            }
            if (amountLeft > 0) {
              BazaarNotifier.orders.getJSONObject(orderInQuestion)
                  .put("amountRemaining", amountLeft).put("orderValue", price * amountLeft);
            }
          }
        } else {
          Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED
                  + "One or more bazaar orders has an unknown item. Please report this in the discord server"));
          return;
        }
      }
    }
  }

  private void orderConfirmation(IInventory chest) {
    if (chest.getStackInSlot(13) != null) {
      double price = Double.parseDouble(StringUtils.stripControlCodes(
          chest.getStackInSlot(13).getTagCompound().getCompoundTag("display")
              .getTagList("Lore", 8).getStringTagAt(2)).split(" ")[3].replaceAll(",", ""));
      String product = StringUtils.stripControlCodes(
          chest.getStackInSlot(13).getTagCompound().getCompoundTag("display")
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
            chest.getStackInSlot(13).getTagCompound().getCompoundTag("display")
                .getTagList("Lore", 8).getStringTagAt(4)).split(": ")[1];
        int amount = Integer.parseInt(StringUtils.stripControlCodes(
            chest.getStackInSlot(13).getTagCompound().getCompoundTag("display")
                .getTagList("Lore", 8).getStringTagAt(4)).split("x ")[0].replaceAll(",", ""));
        EventHandler.productVerify[0] = productName;
        EventHandler.productVerify[1] = productWithAmount;
        EventHandler.verify = new JSONObject()
            .put("product", product)
            .put("startAmount", amount)
            .put("amountRemaining", amount)
            .put("pricePerUnit", price)
            .put("outdatedOrder", false)
            .put("matchedOrder", false)
            .put("goodOrder", true)
            .put("orderValue", amount * price)
            .put("type", Utils.stripString(chest.getDisplayName().getUnformattedText())
                .equalsIgnoreCase("Confirm Sell Offer") ? "sell" : "buy");
      }
    }
  }
}

// TODO: add notification for a "good order" (an order that was revived)
