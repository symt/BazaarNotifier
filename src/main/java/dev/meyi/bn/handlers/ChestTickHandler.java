package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.Order;
import dev.meyi.bn.utilities.ReflectionHelper;
import dev.meyi.bn.utilities.Utils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

    Pattern p = Pattern.compile("(?:\\[.*\\] )(.*)");
    Matcher m = p.matcher(StringUtils.stripControlCodes(Minecraft.getMinecraft().thePlayer.getDisplayName().getUnformattedText()));
    String playerName = null;
    if (m.find()) {
      playerName = m.group(1);
    } else {
      System.err.println("Improperly formatted player name. Aborting!");
    }

    for (ItemStack item : items) {
      if(item == null) continue;
      int itemID = Item.itemRegistry.getIDForObject(item.getItem());
      if (    itemID == 160    // Glass
          ||  itemID == 102    // Glass
          ||  itemID == 262    // Arrow
          || (itemID == 154 && StringUtils.stripControlCodes(item.getDisplayName()).equals("Claim All Coins"))) { //Hopper
        continue;
      } //Hopper
      List<String> lore = Utils.getLoreFromItemStack(item);

      Pattern p2 = Pattern.compile("(BUY|SELL):? (.*)");
      Matcher m2 = p2.matcher(StringUtils.stripControlCodes(item.getDisplayName()));
      String displayName;
      Order.OrderType type;
      if (m2.find()) {
        displayName = m2.group(2);
        type = "sell".equalsIgnoreCase(m2.group(1)) ? Order.OrderType.SELL : Order.OrderType.BUY;
      } else {
        System.err.println("Bazaar item header incorrect. Aborting!");
        return;
      }

      if (BazaarNotifier.bazaarConv.containsValue(displayName)) {
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
          Order order = BazaarNotifier.orders.get(orderInQuestion);
          int totalAmount = order.startAmount;
          int amountLeft = Utils.getOrderAmountLeft(lore, totalAmount);
          if (amountLeft > 0) {
            order.setAmountRemaining(amountLeft);
          }
        } else if (playerName != null) {
          Pattern p3 = Pattern.compile("By: (?:\\[.*\\] )?(.*)");
          String creator = "";
          for (String line : lore) {
            Matcher m3 = p3.matcher(line);
            if (m3.find()) {
              creator = m3.group(1);
              break;
            }
          }
          if (creator.equals(playerName) || creator.isEmpty()) { //isEmpty for non Coop Islands
            if (lore.get(4).toLowerCase().contains("expire") || lore.get(5).toLowerCase().contains("expire")) {
              continue;
            }
            String totalAmount = lore.get(2).split(" ")[2];
            int startAmount = Integer.parseInt(totalAmount.substring(0, totalAmount.length()-1).replace(",", ""));
            Order newOrder = new Order(displayName, startAmount, Double.parseDouble(priceString), priceString, type);
            newOrder.setAmountRemaining(Utils.getOrderAmountLeft(lore, startAmount));
            if (newOrder.getAmountRemaining() != 0) {
              BazaarNotifier.orders.add(newOrder);
              verifiedOrders = Arrays.copyOf(verifiedOrders, verifiedOrders.length + 1);
              verifiedOrders[verifiedOrders.length-1] = 1;
            }
          }
        }
      } else {
        System.out.println(BazaarNotifier.orders);
        System.err.println("Some orders weren't found! Bad display name: " + displayName);
        return;
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
    try {
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
                if (orderConfirmation(chest)) { // Don´t reset the lastScreenDisplayName to retry in the next tick
                  lastScreenDisplayName = StringUtils.stripControlCodes(
                          chest.getDisplayName().getUnformattedText());
                }
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
    }catch (Exception ex){
      Minecraft.getMinecraft().thePlayer.addChatMessage(
              new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED +
                "The onChestTick method ran into an error. Please report this in the discord server"));
      ex.printStackTrace();
    }
  }

  /**
   * @return success
   */
  private boolean orderConfirmation(IInventory chest) {

    if (chest.getStackInSlot(13) != null) {
      String priceString = "";
      String product = "";
      double price = 0;

      try {
        priceString = StringUtils.stripControlCodes(
                chest.getStackInSlot(13).getTagCompound().getCompoundTag("display").getTagList("Lore", 8)
                        .getStringTagAt(2)).split(" ")[3].replaceAll(",", "");
        price = Double.parseDouble(priceString);

        product = StringUtils.stripControlCodes(
                chest.getStackInSlot(13).getTagCompound().getCompoundTag("display").getTagList("Lore", 8)
                        .getStringTagAt(4)).split("x ", 2)[1];
      }catch (Exception e){
        return false;
      }
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
    return true;
  }
  @SubscribeEvent
  public void renderInChest(GuiScreenEvent.BackgroundDrawnEvent e){
    BazaarNotifier.modules.drawAllGui();
  }
}