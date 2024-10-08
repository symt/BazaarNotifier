package dev.meyi.bn.modules.module;

import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.Order;
import dev.meyi.bn.modules.HoverableModule;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.utilities.ColoredText;
import dev.meyi.bn.utilities.Defaults;
import dev.meyi.bn.utilities.ReflectionHelper;
import dev.meyi.bn.utilities.RenderUtils;
import dev.meyi.bn.utilities.Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

public class NotificationModule extends HoverableModule {

  public transient static final ModuleName type = ModuleName.NOTIFICATION;

  public NotificationModule() {
    super();
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
  protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
    draw();
  }

  @Override
  protected float getWidth(float scale, boolean example) {
    if (longestString != null) {
      if (!longestString.isEmpty()) {
        return RenderUtils.getStringWidth(longestString) * scale + 2 * padding * scale;
      }
    }
    return 200 * scale;
  }

  @Override
  protected float getHeight(float scale, boolean example) {
    return ((Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 10 + 20) * scale - 2)
        + 2 * padding * scale;
  }

  @Override
  public void draw() {
    GL11.glTranslated(0, 0, 1);
    // add extra space after "Buy" so it lines up with sell

    ArrayList<ArrayList<ColoredText>> items = new ArrayList<>();

    if (BazaarNotifier.orders.size() != 0) {

      int size = Math.min(shift + 10, BazaarNotifier.orders.size());

      for (int i = shift; i < size; i++) {
        Order currentOrder = BazaarNotifier.orders.get(i);
        ArrayList<ColoredText> message = new ArrayList<>();

        Color statusSpecificColor = currentOrder.orderStatus == Order.OrderStatus.BEST
            || currentOrder.orderStatus == Order.OrderStatus.SEARCHING
            ? Color.GREEN : currentOrder.orderStatus == Order.OrderStatus.MATCHED ?
            Color.YELLOW : Color.RED;
        Color typeSpecificColor =
            currentOrder.type == Order.OrderType.BUY ? new Color(90, 0, 250) : Color.CYAN;

        message.add(new ColoredText(i + 1 + ". ", BazaarNotifier.config.numberColor.toJavaColor()));
        message.add(new ColoredText(WordUtils.capitalizeFully(currentOrder.type.name()),
            typeSpecificColor));
        message.add(new ColoredText(" - ", BazaarNotifier.config.infoColor.toJavaColor()));
        message.add(
            new ColoredText(BazaarNotifier.dfNoDecimal.format(currentOrder.startAmount) + "x ",
                BazaarNotifier.config.itemColor.toJavaColor()));
        message.add(
            new ColoredText(currentOrder.product, BazaarNotifier.config.itemColor.toJavaColor()));
        message.add(new ColoredText(" - ", BazaarNotifier.config.infoColor.toJavaColor()));
        message.add(new ColoredText(currentOrder.orderStatus.name() + " ", statusSpecificColor));

        items.add(message);
      }
      longestString = RenderUtils.getLongestString(items);
      RenderUtils.drawColorfulParagraph(items, (int) position.getX() + padding,
          (int) position.getY() + padding, scale);
    } else {
      longestString = "";
      RenderUtils.drawCenteredString("No orders found", (int) position.getX(),
          (int) position.getY(), 0xAAAAAA, scale);
    }

    highlightOrder(getHoveredLineNumber());
    GL11.glTranslated(0, 0, -1);
  }

  @Override
  protected void reset() {
    position.setPosition(Defaults.NOTIFICATION_MODULE_X, Defaults.NOTIFICATION_MODULE_Y);
    setScale(1, false);
    enabled = true;
  }

  @Override
  public String name() {
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

  public void highlightOrder(int hoveredText) {
    if (!renderTooltips || BazaarNotifier.orders.size() <= hoveredText || hoveredText == -1) {
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

          if (Utils.shouldSkipChestItem(item)) {
            continue;
          }
          String itemDisplayName = StringUtils.stripControlCodes(item.getDisplayName());
          Order.OrderType type = itemDisplayName.split(" ")[0]
              .equalsIgnoreCase("sell") ? Order.OrderType.SELL : Order.OrderType.BUY;
          String product = itemDisplayName
              .replaceAll("SELL ", "").replaceAll("BUY ", "");
          List<String> lore = Utils.getLoreFromItemStack(item);

          int amount = Integer.parseInt(
              lore.get(2).toLowerCase().split("amount: ")[1].replaceAll("[x,.]", ""));

          String ppu = lore.get(3).equals("") ? lore.get(4) : lore.get(5);
          ppu = ppu.toLowerCase().replace("price per unit: ", "").replace(" coins", "")
              .replaceAll(",", "");

          if (!(ppu.contains("expired!") || ppu.contains("expires in"))) {

            double pricePerUnit = Double.parseDouble(ppu);

            Order o = new Order(product, type, pricePerUnit, amount);

            for (int i = 0; i < BazaarNotifier.orders.size(); i++) {
              if (o.matches(BazaarNotifier.orders.get(hoveredText))) {
                drawOnSlot(chest.getSizeInventory(), j, 0xff00ff00);
                break;
              }
            }
          }
        }
      }
    }
  }

  @Override
  protected ArrayList<ArrayList<ColoredText>> generateTooltipText(int lineNumber) {
    return null;
  }
}
