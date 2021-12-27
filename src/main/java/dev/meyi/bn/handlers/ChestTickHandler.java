package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.ProfitCalculator;
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

public class ChestTickHandler {

  public static String lastScreenDisplayName = "";
  // /blockdata x y z {CustomName:"___"} << For Custom Chest Name Testing

  public static void updateBazaarOrders(IInventory chest) {
    int[] verifiedOrders = new int[BazaarNotifier.orders.length()];
    for (int i = 0; i < chest.getSizeInventory(); i++) {
      if (chest.getStackInSlot(i) != null
          && Item.itemRegistry.getIDForObject(chest.getStackInSlot(i).getItem()) != 160    // Glass
          && Item.itemRegistry.getIDForObject(chest.getStackInSlot(i).getItem()) != 102    // Glass
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
          String priceString;
          if (lore.get(4).toLowerCase().contains("expire")) {
            priceString = StringUtils.stripControlCodes(lore.get(6)).replaceAll(",", "")
                .split(" ")[3];
            price = Double.parseDouble(priceString);
          } else if (lore.get(5).toLowerCase().contains("expire")) {
            priceString = StringUtils.stripControlCodes(lore.get(7)).replaceAll(",", "")
                .split(" ")[3];
            price = Double.parseDouble(priceString);
          } else {
            priceString = StringUtils.stripControlCodes(
                lore.get((lore.get(3).startsWith("Filled:")) ? 5 : 4).replaceAll(",", "")
                    .split(" ")[3]);
            price = Double.parseDouble(priceString);
          }
          int orderInQuestion = -1;
          for (int j = 0; j < BazaarNotifier.orders.length(); j++) {
            JSONObject order = BazaarNotifier.orders.getJSONObject(j);
            if (priceString.equalsIgnoreCase(order.getString("priceString")) && type
                .equals(order.getString("type"))) {
              orderInQuestion = j;
              break;
            }
          }
          if (orderInQuestion != -1) {
            verifiedOrders[orderInQuestion] = 1;
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
              if (BazaarNotifier.sendChatMessages) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                    BazaarNotifier.prefix + EnumChatFormatting.RED
                        + "Because of the limitations of the bazaar's information, you had an order removed that exceeded the maximum number of buyers/sellers. If you want, you can cancel the missing order freely and put it back up."));
              }
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

    for (int i = verifiedOrders.length - 1; i >= 0; i--) {
      if (verifiedOrders[i] == 0) {
        BazaarNotifier.orders.remove(i);
      }
    }
  }

  @SubscribeEvent
  public void onChestTick(TickEvent e) {
    if (e.phase == Phase.END) {
      if (Minecraft.getMinecraft().currentScreen instanceof GuiChest && BazaarNotifier.inBazaar
          && BazaarNotifier.activeBazaar) {

        IInventory chest = ((GuiChest) Minecraft.getMinecraft().currentScreen).lowerChestInventory;
        String chestName = chest.getDisplayName().getUnformattedText().toLowerCase();

        if (chest.hasCustomName() && !lastScreenDisplayName.equalsIgnoreCase(chestName)) {
          if (chestName.equals("confirm buy order") ||
              chestName.equals("confirm sell offer")) {

            if (chest.getStackInSlot(13) != null) {
              lastScreenDisplayName = StringUtils
                  .stripControlCodes(chest.getDisplayName().getUnformattedText());
              orderConfirmation(chest);
            }

          } else if (chestName.contains("bazaar orders")) {
            if (chest.getStackInSlot(chest.getSizeInventory() - 5) != null
                && Item.itemRegistry
                .getIDForObject(chest.getStackInSlot(chest.getSizeInventory() - 5).getItem())
                == 262) {
              lastScreenDisplayName = StringUtils
                  .stripControlCodes(chest.getDisplayName().getUnformattedText());
              updateBazaarOrders(chest);
            }
          }
        }
      } else if (BazaarNotifier.inBank && Minecraft
          .getMinecraft().currentScreen instanceof GuiChest) {
        IInventory chest = ((GuiChest) Minecraft.getMinecraft().currentScreen).lowerChestInventory;
        String chestName = chest.getDisplayName().getUnformattedText().toLowerCase();
        if (chestName.contains("bank account") && !chestName.contains("upgrade")) {
          ProfitCalculator.extractBankFromItemDescription(
              ((GuiChest) Minecraft.getMinecraft().currentScreen).lowerChestInventory);
        }
      } else if (!BazaarNotifier.inBazaar) { // if you aren't in the bazaar, this should be clear
        ChestTickHandler.lastScreenDisplayName = "";
      }
    }
  }

  private void orderConfirmation(IInventory chest) {

    if (chest.getStackInSlot(13) != null) {

      String priceString = StringUtils.stripControlCodes(
          chest.getStackInSlot(13).getTagCompound().getCompoundTag("display").getTagList("Lore", 8)
              .getStringTagAt(2)).split(" ")[3].replaceAll(",", "");
      double price = Double.parseDouble(priceString);

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
            .put("priceString", priceString)
            .put("outdatedOrder", false)
            .put("matchedOrder", false)
            .put("goodOrder", true)
            .put("orderValue", amount * price)
            .put("type", StringUtils.stripControlCodes(chest.getDisplayName().getUnformattedText())
                .equalsIgnoreCase("Confirm Sell Offer") ? "sell" : "buy");
      }
    }
  }
}