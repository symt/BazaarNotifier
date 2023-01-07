package dev.meyi.bn.modules.module;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.config.ModuleConfig;
import dev.meyi.bn.json.Order;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.utilities.Defaults;
import dev.meyi.bn.utilities.ReflectionHelper;
import dev.meyi.bn.utilities.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

public class NotificationModule extends Module {

  public static final ModuleName type = ModuleName.NOTIFICATION;
  int longestXString = 0;

  public NotificationModule() {
    super();
  }

  public NotificationModule(ModuleConfig module) {
    super(module);
  }

  //source dsm
  public static void drawOnSlot(int chestSize, int slot, int color) {
    chestSize += 36;
    ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
    int guiLeft = (sr.getScaledWidth() - 176) / 2;
    int guiTop = (sr.getScaledHeight() - 222) / 2;
    int xSlotPos = (slot % 9) * 18 + 8;
    int ySlotPos = slot / 9;
    ySlotPos = ySlotPos * 18 + 18;
    int x = guiLeft + xSlotPos;
    int y = guiTop + ySlotPos;
    // Move down when chest isn't 6 rows
    if (chestSize != 90) {
      y += (6 - (chestSize - 36) / 9) * 9;
    }
    GL11.glTranslated(0, 0, 1);
    Gui.drawRect(x, y, x + 16, y + 16, color);
    GL11.glTranslated(0, 0, -1);
  }

  @Override
  protected void draw() {
    // add extra space after "Buy" so it lines up with sell

    List<LinkedHashMap<String, Color>> items = new ArrayList<>();

    if (BazaarNotifier.orders.size() != 0) {

      int size = Math.min(shift + 10, BazaarNotifier.orders.size());

      for (int i = shift; i < size; i++) {
        Order currentOrder = BazaarNotifier.orders.get(i);
        LinkedHashMap<String, Color> message = new LinkedHashMap<>();

        Color typeSpecificColor =
            currentOrder.orderStatus == Order.OrderStatus.BEST
                || currentOrder.orderStatus == Order.OrderStatus.SEARCHING
                ? new Color(0x55FF55)
                : currentOrder.type.equals(Order.OrderType.BUY) ? new Color(0xFF55FF)
                    : new Color(0x55FFFF);

        String notification = currentOrder.orderStatus.name();
        message.put(WordUtils.capitalizeFully(currentOrder.type.name()), typeSpecificColor);
        message.put(" - ", new Color(0xAAAAAA));
        message.put(notification + " ", new Color(0xFFFF55));
        message.put("(", new Color(0xAAAAAA));
        message.put(BazaarNotifier.dfNoDecimal.format(currentOrder.startAmount),
            typeSpecificColor);
        message.put("x ", new Color(0xAAAAAA));
        message.put(currentOrder.product, typeSpecificColor);
        message.put(", ", new Color(0xAAAAAA));
        message.put(BazaarNotifier.df.format(currentOrder.pricePerUnit),
            typeSpecificColor);
        message.put(")", new Color(0xAAAAAA));
        items.add(message);
      }

      longestXString = RenderUtils.drawColorfulParagraph(items, x, y, scale);
      boundsX = x + longestXString;
    } else {
      RenderUtils.drawCenteredString("No orders found", x, y, 0xAAAAAA, scale);
      float X = x + 200 * scale;
      boundsX = (int) X;
    }
    float Y =
        y + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * scale * 10 + 20 * scale - 2;
    boundsY = (int) Y;
    highlightOrder(checkHoveredText());
  }

  @Override
  protected void reset() {
    x = Defaults.NOTIFICATION_MODULE_X;
    y = Defaults.NOTIFICATION_MODULE_Y;
    scale = 1;
    active = true;
  }

  @Override
  protected String name() {
    return ModuleName.NOTIFICATION.name();
  }

  @Override
  protected boolean shouldDrawBounds() {
    return true;
  }

  @Override
  protected int getMaxShift() {
    return BazaarNotifier.orders.size() - 10;
  }

  protected int checkHoveredText() {
    float _y = y;
    float y2 = _y + (10 * 11 * scale);
    int mouseYFormatted = getMouseCoordinateY();
    int mouseXFormatted = getMouseCoordinateX();
    float relativeYMouse = (mouseYFormatted - _y) / (11 * scale);
    if (this.longestXString != 0) {
      if (mouseXFormatted >= x && mouseXFormatted <= x + longestXString
          && mouseYFormatted >= _y && mouseYFormatted <= y2 - 3 * scale) {
        return Math.round(relativeYMouse + shift);
      } else {
        return -1;
      }
    } else {
      return -1;
    }
  }

  public void highlightOrder(int hoveredText) {
    if (BazaarNotifier.orders.size() <= hoveredText || hoveredText == -1) {
      return;
    }

    if (Minecraft.getMinecraft().currentScreen instanceof GuiChest && BazaarNotifier.inBazaar
        && BazaarNotifier.activeBazaar) {
      IInventory chest = ReflectionHelper.getLowerChestInventory(
          (GuiChest) Minecraft.getMinecraft().currentScreen);
      if (chest == null) {
        return;
      }
      String chestName = chest.getDisplayName().getUnformattedText().toLowerCase();

      if (chestName.contains("bazaar orders")) {
        ItemStack[] items = new ItemStack[chest.getSizeInventory()];
        for (int i = 0; i < chest.getSizeInventory(); i++) {
          items[i] = chest.getStackInSlot(i);
        }

        for (int j = 0; j < items.length; j++) {
          ItemStack item = items[j];

          if (item == null
              || Item.itemRegistry.getIDForObject(item.getItem()) == 160
              || Item.itemRegistry.getIDForObject(item.getItem()) == 102
              || Item.itemRegistry.getIDForObject(item.getItem()) == 262) {
            continue;
          }
          String itemDisplayName = StringUtils.stripControlCodes(item.getDisplayName());
          Order.OrderType type = itemDisplayName.split(" ")[0]
              .equalsIgnoreCase("sell") ? Order.OrderType.SELL : Order.OrderType.BUY;
          String product = itemDisplayName
              .replaceAll("SELL ", "").replaceAll("BUY ", "");
          NBTTagList lorePreFilter = item.getTagCompound().getCompoundTag("display")
              .getTagList("Lore", 8);
          List<String> lore = new ArrayList<>();
          for (int k = 0; k < lorePreFilter.tagCount(); k++) {
            lore.add(StringUtils.stripControlCodes(lorePreFilter.getStringTagAt(k)));
          }

          int amount = Integer.parseInt(
              lore.get(2).toLowerCase().split("amount: ")[1].replaceAll("[x,.]", ""));

          String ppu = lore.get(3).equals("") ? lore.get(4) : lore.get(5);
          ppu = ppu.toLowerCase().replace("price per unit: ", "").replace(" coins", "")
              .replaceAll(",", "");

          if (!ppu.contains("expired!") && !ppu.contains("expires in")) {

            double pricePerUnit = Double.parseDouble(ppu);

            Order o = new Order(product, type, pricePerUnit, amount);

            for (int i = 0; i < BazaarNotifier.orders.size(); i++) {
              if (o.matches(BazaarNotifier.orders.get(hoveredText))) {
                drawOnSlot(chest.getSizeInventory(), j, 0xff00ff00);
              }
            }
          }
        }
      }
    }
  }
}
