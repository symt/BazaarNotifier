package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.Order;
import dev.meyi.bn.utilities.ReflectionHelper;
import dev.meyi.bn.utilities.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@SuppressWarnings("unused")
public class ChestTickHandler {

  public static String lastScreenDisplayName = "";

  // /blockdata x y z {CustomName:"___"} << For Custom Chest Name Testing

  public static void updateBazaarOrders(IInventory chest) {
    int[] verifiedOrders = new int[BazaarNotifier.orders.size()];
    ItemStack[] items = new ItemStack[chest.getSizeInventory() + 1];
    for (int i = 0; i < chest.getSizeInventory(); i++) {
      items[i] = chest.getStackInSlot(i);
    }
    items[items.length - 1] = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();

    for (ItemStack item : items) {
      if (item != null && Item.itemRegistry.getIDForObject(item.getItem()) != 160    // Glass
          && Item.itemRegistry.getIDForObject(item.getItem()) != 102    // Glass
          && Item.itemRegistry.getIDForObject(item.getItem()) != 262) { // Arrow
        NBTTagList lorePreFilter = item.getTagCompound().getCompoundTag("display")
            .getTagList("Lore", 8);

        List<String> lore = new ArrayList<>();

        for (int j = 0; j < lorePreFilter.tagCount(); j++) {
          lore.add(StringUtils.stripControlCodes(lorePreFilter.getStringTagAt(j)));
        }

        Pattern p = Pattern.compile("(BUY|SELL):? (.*)");
        Matcher m = p.matcher(StringUtils.stripControlCodes(item.getDisplayName()));
        String displayName;
        Order.OrderType type;
        if (m.find()) {
          displayName = m.group(2);
          type = "sell".equalsIgnoreCase(m.group(1)) ? Order.OrderType.SELL : Order.OrderType.BUY;
        } else {
          System.err.println("Bazaar item header incorrect. Aborting!");
          return;
        }

        if (BazaarNotifier.bazaarConv.containsValue(displayName)) {
          int amountLeft;

          String priceString;
          if (lore.get(4).toLowerCase().contains("expire")) {
            priceString = StringUtils.stripControlCodes(lore.get(6)).replaceAll(",", "")
                .split(" ")[3];
          } else if (lore.get(5).toLowerCase().contains("expire")) {
            priceString = StringUtils.stripControlCodes(lore.get(7)).replaceAll(",", "")
                .split(" ")[3];
          } else {
            priceString = StringUtils.stripControlCodes(
                lore.get((lore.get(3).startsWith("Filled:")) ? 5 : 4).replaceAll(",", "")
                    .split(" ")[3]);
          }
          int orderInQuestion = -1;
          for (int j = 0; j < BazaarNotifier.orders.size(); j++) {
            Order order = BazaarNotifier.orders.get(j);
            if (priceString.equalsIgnoreCase(order.priceString) && type.equals(
                order.type)) { // Todo check product also causing problems
              orderInQuestion = j;
              break;
            }
          }
          if (orderInQuestion != -1) {
            verifiedOrders[orderInQuestion] = 1;
            int totalAmount = BazaarNotifier.orders.get(orderInQuestion).startAmount;
            if (lore.get(3).startsWith("Filled:")) {
              if (lore.get(3).split(" ")[2].equals("100%")) {
                amountLeft = 0;
              } else {
                String intToParse = lore.get(3).split(" ")[1].split("/")[0];
                int amountFulfilled;

                if (intToParse.contains("k")) {
                  amountFulfilled = (int) (Double.parseDouble(intToParse.replace("k", "")) * 1000);
                } else {
                  amountFulfilled = Integer.parseInt(intToParse);
                }

                amountLeft = totalAmount - amountFulfilled;
              }
            } else {
              amountLeft = BazaarNotifier.orders.get(orderInQuestion).startAmount;
            }
            if (amountLeft > 0) {
              BazaarNotifier.orders.get(orderInQuestion).setAmountRemaining(amountLeft);
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

        IInventory chest = ReflectionHelper.getLowerChestInventory(
            (GuiChest) Minecraft.getMinecraft().currentScreen);
        if (chest == null) {
          return;
        }
        String chestName = chest.getDisplayName().getUnformattedText().toLowerCase();

        if (chest.hasCustomName() && !lastScreenDisplayName.equalsIgnoreCase(chestName)) {
          if (chestName.equals("confirm buy order") || chestName.equals("confirm sell offer")) {

            if (chest.getStackInSlot(13) != null) {
              lastScreenDisplayName = StringUtils.stripControlCodes(
                  chest.getDisplayName().getUnformattedText());
              orderConfirmation(chest);
            }

          } else if (chestName.contains("bazaar orders")) {
            if (chest.getStackInSlot(chest.getSizeInventory() - 5) != null &&
                Item.itemRegistry.getIDForObject(
                    chest.getStackInSlot(chest.getSizeInventory() - 5).getItem()) == 262) {
              lastScreenDisplayName = StringUtils.stripControlCodes(
                  chest.getDisplayName().getUnformattedText());
              updateBazaarOrders(chest);
            }
          } else if (chestName.contains("bazaar")) {
            lastScreenDisplayName = StringUtils.stripControlCodes(
                chest.getDisplayName().getUnformattedText());
          }
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
          chest.getStackInSlot(13).getTagCompound().getCompoundTag("display").getTagList("Lore", 8)
              .getStringTagAt(4)).split("x ", 2)[1];

      String productName;

      if (!BazaarNotifier.bazaarConv.containsValue(product)) {
        String[] possibleConversion = Utils.getItemIdFromName(product);
        productName = possibleConversion[1];

        if (!possibleConversion[0].equals(product) && !productName.isEmpty()) {
          BazaarNotifier.bazaarConv.put(productName, product);
          Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED
                  + "A possible conversion was found. Please report this to the discord server:"
                  + EnumChatFormatting.GRAY + " \""
                  + productName + "\" - \"" + product + "\" \"" + possibleConversion[0] + "\"."));
        } else {
          Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED
                  + "No item conversion was found for that item. Please report this to the discord server: "
                  + EnumChatFormatting.GRAY + "\""
                  + product + "\"."));
        }
      } else {
        productName = BazaarNotifier.bazaarConv.inverse().get(product);
      }

      if (BazaarNotifier.bazaarConv.containsKey(productName)) {
        String productWithAmount = StringUtils.stripControlCodes(
            chest.getStackInSlot(13).getTagCompound().getCompoundTag("display")
                .getTagList("Lore", 8).getStringTagAt(4)).split(": ")[1];

        int amount = Integer.parseInt(StringUtils.stripControlCodes(
            chest.getStackInSlot(13).getTagCompound().getCompoundTag("display")
                .getTagList("Lore", 8).getStringTagAt(4)).split(": ")[1].split("x ")[0].replaceAll(
            ",", ""));

        EventHandler.productVerify[0] = productName;
        EventHandler.productVerify[1] = productWithAmount;
        Order.OrderType type =
            StringUtils.stripControlCodes(chest.getDisplayName().getUnformattedText())
                .equalsIgnoreCase("Confirm Sell Offer") ? Order.OrderType.SELL
                : Order.OrderType.BUY;
        EventHandler.verify = new Order(product, amount, price, priceString, type);
      }
    }
  }
  @SubscribeEvent
  public void renderInChest(GuiScreenEvent.BackgroundDrawnEvent e){
    BazaarNotifier.modules.drawAll();
  }
}