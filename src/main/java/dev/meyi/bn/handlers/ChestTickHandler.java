package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.Utils;
import java.util.ArrayList;
import java.util.List;
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
import org.lwjgl.Sys;
import scala.Console;
import scala.collection.immutable.Stream;

public class ChestTickHandler {

  public static String lastScreenDisplayName = "";
  // /blockdata x y z {CustomName:"___"} << For Custom Chest Name Testing

  @SubscribeEvent
  public void onChestTick(TickEvent e) {
    if (e.phase == Phase.END) {
      if (Minecraft.getMinecraft().currentScreen instanceof GuiChest && BazaarNotifier.inBazaar) {

        IInventory chest = ((GuiChest) Minecraft.getMinecraft().currentScreen).lowerChestInventory;
        String chestName = Utils.stripString(chest.getDisplayName().getUnformattedText().toLowerCase());

        if (chest.hasCustomName() && !lastScreenDisplayName.equalsIgnoreCase(chestName)) {

          if (chestName.equals("confirm buy order") ||
                  chestName.equals("confirm sell offer")) {

            if(chest.getStackInSlot(13) == null){
              return;
            }
            orderConfirmation(chest);

          }else if(chestName.contains("bazaar orders")){
            updateBazaarOrders(chest);
          }

          lastScreenDisplayName = Utils.stripString(chest.getDisplayName().getUnformattedText());

        }
      }
    }
  }

  public static void updateBazaarOrders(IInventory chest) {
    for (int i = 0; i < chest.getSizeInventory(); i++) {
      if (chest.getStackInSlot(i) != null
          && Item.itemRegistry.getIDForObject(chest.getStackInSlot(i).getItem()) != 160    // Glass
          && Item.itemRegistry.getIDForObject(chest.getStackInSlot(i).getItem()) != 262) { // Arrow
        NBTTagList lorePreFilter = chest.getStackInSlot(i).getTagCompound()
            .getCompoundTag("display")
            .getTagList("Lore", 8);

        List<String> lore = new ArrayList<>();

        for (int j = 0; j < lorePreFilter.tagCount(); j++) {
          lore.add(StringUtils.stripControlCodes(lorePreFilter.getStringTagAt(j)));
        }

        String displayName = StringUtils
            .stripControlCodes(chest.getStackInSlot(i).getDisplayName().split(": ")[1]);
        String type = StringUtils.stripControlCodes(
            chest.getStackInSlot(i).getDisplayName().split(": ")[0].toLowerCase());

        if (BazaarNotifier.bazaarConversionsReversed.has(displayName)) {
          int amountLeft = -1;
          double price;
          if (lore.get(4).toLowerCase().contains("expire")) {
            price = Double.parseDouble(
                StringUtils.stripControlCodes(lore.get(6)).replaceAll(",", "").split(" ")[3]);
          } else if (lore.get(5).toLowerCase().contains("expire")) {
            price = Double.parseDouble(
                StringUtils.stripControlCodes(lore.get(7)).replaceAll(",", "").split(" ")[3]);
          } else {
            price = Double
                .parseDouble(
                    StringUtils
                        .stripControlCodes(lore.get((lore.get(3).startsWith("Filled:")) ? 5 : 4)
                            .replaceAll(",", "").split(" ")[3]));
          }
          int orderInQuestion = -1;
          for (int j = 0; j < BazaarNotifier.orders.length(); j++) {
            JSONObject order = BazaarNotifier.orders.getJSONObject(j);
            if (Double.compare(order.getDouble("pricePerUnit"), price) == 0 && type
                .equals(order.getString("type"))) {
              orderInQuestion = j;
              break;
            }
          }
          if (orderInQuestion != -1) {
            boolean forceRemove = false;
            int totalAmount = BazaarNotifier.orders.getJSONObject(orderInQuestion)
                .getInt("startAmount");
            if (lore.get(3).startsWith("Filled:")) {
              if (lore.get(3).split(" ")[2].equals("100%")) {
                amountLeft = 0;
              } else {
                int amountFulfilled = 0;
                for (int j = 8; j < lore.size(); j++) {
                  if (lore.get(j).isEmpty()) {
                    break;
                  } else if (lore.get(j).startsWith(" + ") && lore.get(j).endsWith("others")) {
                    forceRemove = true;
                    break;
                  }
                  amountFulfilled += Integer.parseInt(
                      lore.get(j).replaceAll(",", "").split("x ")[0].substring(2));
                }
                amountLeft = totalAmount - amountFulfilled;
              }
            }
            if (forceRemove) {
              Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                  BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "Because of the limitations of the bazaar's information, you had an order removed that exceeded the maximum number of buyers/sellers. If you want, you can cancel the missing order freely and put it back up."));
              BazaarNotifier.orders.remove(orderInQuestion);
            } else if (amountLeft > 0) {
              BazaarNotifier.orders.getJSONObject(orderInQuestion)
                  .put("amountRemaining", amountLeft).put("orderValue", price * amountLeft);
            }
          }
        } else {
          System.out.println(BazaarNotifier.orders);
          System.err.println("Some orders weren't found! Bad display name: " + displayName);
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
                .getTagList("Lore", 8).getStringTagAt(4)).split(": ")[1].split("x ")[0]
            .replaceAll(",", ""));

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
