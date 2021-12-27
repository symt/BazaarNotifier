package dev.meyi.bn.modules;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.config.Configuration;
import dev.meyi.bn.handlers.MouseHandler;
import dev.meyi.bn.utilities.ColorUtils;
import dev.meyi.bn.utilities.Defaults;
import dev.meyi.bn.utilities.EnchantedCraftingHandler;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.input.Keyboard;

public class CraftingModule extends Module {

  int longestXString;
  ArrayList<ArrayList<String>> list;
  int lastHovered = 0;
  private int materialCountWheel = 1;
  private final LinkedHashMap<String, Color> helperLine = new LinkedHashMap<>();
  public CraftingModule() {
    super();
  }

  public CraftingModule(JSONObject module) {
    super(module);
  }

  private void generateHelperLine() {
    helperLine.clear();
    helperLine.put("   ", Color.MAGENTA);
    helperLine.put("Profits (Buy Orders)", Color.LIGHT_GRAY);
    if (Configuration.showProfitPerMil || Configuration.showInstantSellProfit || Configuration.showSellOfferProfit) {
      helperLine.put(" - ", Color.GRAY);
    }
    if (Configuration.showInstantSellProfit) {
      helperLine.put(" Instant Sell", Color.LIGHT_GRAY);
      if (Configuration.showSellOfferProfit) {
        helperLine.put(" /", Color.GRAY);
      }
    }
    if (Configuration.showSellOfferProfit) {
      helperLine.put(" Sell Offer", Color.LIGHT_GRAY);
      if (Configuration.showProfitPerMil) {
        helperLine.put(" / ", Color.GRAY);
      }
    }
    if (Configuration.showProfitPerMil) {
      helperLine.put("1m Instant", Color.LIGHT_GRAY);
    }
  }

  @Override
  protected void draw() {
    scrollCount();
    list = EnchantedCraftingHandler.getBestEnchantRecipes();
    if (BazaarNotifier.bazaarDataRaw != null) {
      List<LinkedHashMap<String, Color>> items = new ArrayList<>();
      generateHelperLine();
      items.add(helperLine);
      for (int i = shift; i < Configuration.craftingListLength + shift; i++) {
        LinkedHashMap<String, Color> message = new LinkedHashMap<>();
        if (i < list.size()) {
          if (!list.get(i).isEmpty()) {

            Double profitInstaSell = Double.valueOf(list.get(i).get(0));
            Double profitSellOffer = Double.valueOf(list.get(i).get(1));
            Double pricePerMil = Double.valueOf(list.get(i).get(2));
            String itemName = list.get(i).get(3);

            String itemNameConverted = BazaarNotifier.bazaarConversions.getString(itemName);
            message.put(String.valueOf(i + 1), Color.MAGENTA);
            message.put(". ", Color.MAGENTA);
            message.put(itemNameConverted, Color.CYAN);

            if (Configuration.showProfitPerMil || Configuration.showInstantSellProfit || Configuration.showSellOfferProfit) {
              message.put(" - ", Color.GRAY);
            }

            if (Configuration.showInstantSellProfit) {
              message.put(BazaarNotifier.df.format(profitInstaSell),
                  getColor(profitInstaSell.intValue()));
            }
            if (Configuration.showInstantSellProfit && Configuration.showSellOfferProfit) {
              message.put(" / ", Color.GRAY);
            }
            if (Configuration.showSellOfferProfit) {
              message.put(BazaarNotifier.df.format(profitSellOffer),
                  getColor(profitSellOffer.intValue()));
            }
            if (Configuration.showSellOfferProfit && Configuration.showProfitPerMil) {
              message.put(" /  ", Color.GRAY);
            }
            if (Configuration.showProfitPerMil) {
              message.put(BazaarNotifier.df.format(pricePerMil),
                  getColorForMil(pricePerMil.intValue()));
            }
          } else {
            message.put("Error, just wait", Color.RED);
          }
          items.add(message);
        }
      }
      this.longestXString = ColorUtils.drawColorfulParagraph(items, x, y, scale);
      boundsX = x + this.longestXString;
      renderMaterials(checkHoveredText(), list);
    }
    float Y = y + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * scale
        * (Configuration.craftingListLength+1) + (Configuration.craftingListLength+1) * 2 * scale - 2;
    boundsY = (int) Y;
  }

  protected Color getColor(int price) {
    if (price <= 0) {
      return Color.RED;
    } else if (price <= 5000) {
        return Color.YELLOW;
    } else {
        return Color.GREEN;
    }
  }

  protected Color getColorForMil(int price) {
    if (price <= 0) {
      return Color.RED;
    } else if (price <= 30000) {
        return Color.YELLOW;
    } else {
        return Color.GREEN;
    }
  }

  @Override
  protected void reset() {
    x = Defaults.CRAFTING_MODULE_X;
    y = Defaults.CRAFTING_MODULE_Y;
    scale = 1;
    Configuration.craftingListLength = 10;
  }

  @Override
  protected String name() {
    return ModuleName.CRAFTING.name();
  }

  @Override
  protected boolean shouldDrawBounds() {
    return true;
  }

  @Override
  protected int getMaxShift() {
    return list.size() - Configuration.craftingListLength;
  }

  protected int checkHoveredText() {
    float _y = y + 11 * scale;
    float y2 = _y + ((Configuration.craftingListLength) * 11 * scale);
    int mouseYFormatted = getMouseCoordinateY();
    int mouseXFormatted = getMouseCoordinateX();
    float relativeYMouse = (mouseYFormatted - _y) / (11 * scale);
    if (this.longestXString != 0) {
      if (mouseXFormatted >= x && mouseXFormatted <= x + (longestXString / 2)
          && mouseYFormatted >= _y && mouseYFormatted <= y2 - 3 * scale) {
        return (int) relativeYMouse + shift;
      } else {
        return -1;
      }
    } else {
      return 1;
    }
  }

  protected void renderMaterials(int hoveredText, ArrayList<ArrayList<String>> list) {
    List<LinkedHashMap<String, Color>> material = new ArrayList<>();
    LinkedHashMap<String, Color> text = new LinkedHashMap<>();

    if (hoveredText > -1) {
      if (hoveredText < list.size()) {
        if (BazaarNotifier.enchantCraftingList.getJSONObject("normal")
            .has(list.get(hoveredText).get(3))) {
          try {
            text.put(materialCountWheel * 160 + "x ", Color.LIGHT_GRAY);
            text.put(BazaarNotifier.bazaarConversions.getString(
                BazaarNotifier.enchantCraftingList.getJSONObject("normal")
                    .getJSONObject(list.get(hoveredText).get(3)).getString("material")),
                Color.LIGHT_GRAY);
          } catch (JSONException e) {
            text.put("Error", Color.RED);
          }
        } else {
          int materialCount;
          StringBuilder _material = new StringBuilder();
          materialCount = BazaarNotifier.enchantCraftingList.getJSONObject("other")
              .getJSONObject(list.get(hoveredText).get(3)).getJSONArray("material").length();
          for (int b = 0; b < materialCount / 2; b++) {
            if (b == 0) {
              _material.append((BazaarNotifier.enchantCraftingList.getJSONObject("other")
                  .getJSONObject(list.get(hoveredText).get(3)).getJSONArray("material").getInt(1)
                  * materialCountWheel)).append("x ").append(BazaarNotifier.bazaarConversions
                  .getString(BazaarNotifier.enchantCraftingList.getJSONObject("other")
                      .getJSONObject(list.get(hoveredText).get(3)).getJSONArray("material")
                      .getString(0)));
            } else {
              _material.append(" | ").append(
                  BazaarNotifier.enchantCraftingList.getJSONObject("other")
                      .getJSONObject(list.get(hoveredText).get(3)).getJSONArray("material")
                      .getInt(b * 2 + 1) * materialCountWheel).append("x ").append(
                  BazaarNotifier.bazaarConversions.getString(
                      BazaarNotifier.enchantCraftingList.getJSONObject("other")
                          .getJSONObject(list.get(hoveredText).get(3)).getJSONArray("material")
                          .getString(b * 2)));
            }
          }
          text.put(_material.toString(), Color.LIGHT_GRAY);
        }
        material.add(text);
        int longestXString = ColorUtils.drawColorfulParagraph(material, getMouseCoordinateX(),
            getMouseCoordinateY() - (int) (8 * scale), scale);
        Gui.drawRect(getMouseCoordinateX() - padding,
            getMouseCoordinateY() - (int) (8 * scale) - (int) (padding * scale),
            (int) (getMouseCoordinateX() + longestXString + padding * scale),
            (int) (getMouseCoordinateY() + padding * scale), 0xFF404040);
        ColorUtils.drawColorfulParagraph(material, getMouseCoordinateX(),
            getMouseCoordinateY() - (int) (8 * scale), scale);
      }
    }
  }

  public void scrollCount() {
    checkMouseMovement();
    if (MouseHandler.mouseWheelMovement != 0 && Keyboard.isKeyDown(42) && !Keyboard.isKeyDown(29)) {
      materialCountWheel -= MouseHandler.mouseWheelMovement;
      if (materialCountWheel < 1) {
        materialCountWheel = 1;
      }
    }
  }

  public void checkMouseMovement() {
    if (lastHovered != checkHoveredText()) {
      materialCountWheel = 1;
    }
    lastHovered = checkHoveredText();
  }
}
