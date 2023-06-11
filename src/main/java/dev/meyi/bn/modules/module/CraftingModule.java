package dev.meyi.bn.modules.module;

import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.DualOption;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.migration.JsonName;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import dev.meyi.bn.utilities.ColoredText;
import dev.meyi.bn.utilities.RenderUtils;
import dev.meyi.bn.utilities.Defaults;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;


public class CraftingModule extends Module {

  public transient static final ModuleName type = ModuleName.CRAFTING;
  public transient static ArrayList<String[]> list = new ArrayList<>();
  private transient static boolean mouseButtonDown;
  private transient final  ArrayList<ColoredText> helperLine = new ArrayList<>();

  transient int lastHovered = 0;

  @JsonName("craftingSortingOption")
  @Dropdown(name = "Crafting Sorting Option",
          options = {"Instant Sell", "Sell Offer", "1m instant"},
          category = "Crafting Module",
          description = "After which condition should the crafting module be sorted"
  )
  public int craftingSortingOption = Defaults.CRAFTING_SORTING_OPTION;
  @JsonName("craftingListLength")
  @Slider(name = "Crafting List Entries",
          category = "Crafting Module",
          description = "The amount of entries in the Crafting Module list",
          min = 1,max = 25, step = 1
  )
  public int craftingListLength = Defaults.CRAFTING_LIST_LENGTH;
  @JsonName("useBuyOrders")
  @DualOption(name = "Use Buy Orders",
          left = "Instant Buy",
          right = "Buy Orders",
          description = "How you want to buy the crafting materials",
          category = "Crafting Module"
  )
  public boolean useBuyOrders = Defaults.USE_BUY_ORDERS;

  @Checkbox(name = "Show Instant Sell Profit",
          category = "Crafting Module",
          description = "Shows the instant sell profit tab"
  )
  public boolean showInstantSellProfit = Defaults.INSTANT_SELL_PROFIT;
  @Checkbox(name = "Show Sell Offer Profit",
          category = "Crafting Module",
          description = "Shows the sell offer profit tab"
  )
  public boolean showSellOfferProfit = Defaults.SELL_OFFER_PROFIT;
  @Checkbox(name = "Show Profit Percentage",
          category = "Crafting Module",
          description = "Shows the profit percentage tab"
  )
  public boolean showProfitPerMil = Defaults.PROFIT_PER_MIL;




  public CraftingModule() {
    super();
  }

  @Override
  protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
    draw();
  }

  @Override
  protected float getWidth(float scale, boolean example) {
   return RenderUtils.getStringWidth(longestString)*scale;
  }

  @Override
  protected float getHeight(float scale, boolean example) {
    try {
      return (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT
              * (BazaarNotifier.config.craftingModule.craftingListLength + 1)
              + (BazaarNotifier.config.craftingModule.craftingListLength + 1) * 2 )*scale - 2;
    }catch (NullPointerException e){
      return 1;
    }
  }


  private void generateHelperLine() {
    if (Mouse.isButtonDown(1) && getMouseCoordinateY() > position.getY() - 2 && getMouseCoordinateY() < position.getY() + 10) {
      int[] width = new int[4];
      int totalWidth = 0;
      int relativeX = getMouseCoordinateX() - (int)position.getX();
      width[0] =
          (int) (Minecraft.getMinecraft().fontRendererObj.getStringWidth(
              BazaarNotifier.config.craftingModule.useBuyOrders ? "   Profits (Buy Orders) -"
                  : "   Profits (Instant Buy) -") * scale);
      width[1] = BazaarNotifier.config.craftingModule.showInstantSellProfit ?
          (int) (Minecraft.getMinecraft().fontRendererObj.getStringWidth("  Instant Sell ") * scale)
          : 0;
      width[2] = BazaarNotifier.config.craftingModule.showSellOfferProfit ?
          (int) (Minecraft.getMinecraft().fontRendererObj.getStringWidth("/ Sell Offer ") * scale)
          : 0;
      width[3] = BazaarNotifier.config.craftingModule.showProfitPerMil ?
          (int) (Minecraft.getMinecraft().fontRendererObj.getStringWidth("/ Profit Percentage") * scale)
          : 0;

      for (int i : width) {
        totalWidth += i;
      }

      for (int i = 3; i >= 0; i--) {
        totalWidth -= width[i];
        if (totalWidth < relativeX && inMovementBox() && BazaarNotifier.inBazaar) {
          switch (i) {
            case 0:
              if (mouseButtonDown) {
                BazaarNotifier.config.craftingModule.useBuyOrders ^= true;
                mouseButtonDown = false;
              }
            case 1:
              BazaarNotifier.config.craftingModule.craftingSortingOption = 0;
              break;
            case 2:
              BazaarNotifier.config.craftingModule.craftingSortingOption = 1;
              break;
            case 3:
              BazaarNotifier.config.craftingModule.craftingSortingOption = 2;
              break;
          }
          CraftingCalculator.getBestEnchantRecipes();
          break;
        }
      }
    } else if (!Mouse.isButtonDown(1)) {
      mouseButtonDown = true;
    }
    helperLine.clear();
    helperLine.add(new ColoredText("   ", BazaarNotifier.config.infoColor.toJavaColor()));
    helperLine
        .add(new ColoredText(BazaarNotifier.config.craftingModule.useBuyOrders ? "Profits (Buy Orders)" : "Profits (Instant Buy)",
            BazaarNotifier.config.infoColor.toJavaColor()));
    if (BazaarNotifier.config.craftingModule.showProfitPerMil || BazaarNotifier.config
        .craftingModule.showInstantSellProfit
        || BazaarNotifier.config.craftingModule.showSellOfferProfit) {
      helperLine.add(new ColoredText(" - ", BazaarNotifier.config.infoColor.toJavaColor()));
    }
    if (BazaarNotifier.config.craftingModule.showInstantSellProfit) {
      helperLine.add(new ColoredText(" Instant Sell", BazaarNotifier.config.craftingModule.craftingSortingOption == 0 ?
          new Color(141, 152, 201) : BazaarNotifier.config.infoColor.toJavaColor()));
      if (BazaarNotifier.config.craftingModule.showSellOfferProfit) {
        helperLine.add(new ColoredText(" /", BazaarNotifier.config.infoColor.toJavaColor()));
      }
    }
    if (BazaarNotifier.config.craftingModule.showSellOfferProfit) {
      helperLine.add(new ColoredText(" Sell Offer", BazaarNotifier.config.craftingModule.craftingSortingOption == 1 ?
          new Color(141, 152, 201) : BazaarNotifier.config.infoColor.toJavaColor()));
      if (BazaarNotifier.config.craftingModule.showProfitPerMil) {
        helperLine.add(new ColoredText(" / ", BazaarNotifier.config.infoColor.toJavaColor()));
      }
    }
    if (BazaarNotifier.config.craftingModule.showProfitPerMil) {
      helperLine.add(new ColoredText("Profit Percentage", BazaarNotifier.config.craftingModule.craftingSortingOption == 2 ?
          new Color(141, 152, 201) : BazaarNotifier.config.infoColor.toJavaColor()));
    }
  }
  
  @Override
  public void draw() {
    GL11.glTranslated(0, 0, 1);
    drawBounds();
    if (BazaarNotifier.bazaarDataRaw != null) {
      ArrayList<ArrayList<ColoredText>> items = new ArrayList<>();
      generateHelperLine();
      items.add(helperLine);
      for (int i = shift; i < BazaarNotifier.config.craftingModule.craftingListLength + shift; i++) {
        ArrayList<ColoredText> message = new ArrayList<>();
        if (i < list.size()) {
          if (list.get(i).length != 0) {
            Double profitInstaSell = Double.valueOf(list.get(i)[0]);
            Double profitSellOffer = Double.valueOf(list.get(i)[1]);
            Double pricePerMil = Double.valueOf(list.get(i)[2]);
            String itemName = list.get(i)[6];

            String itemNameConverted = BazaarNotifier.bazaarConv.get(itemName);
            message.add(new ColoredText(String.valueOf(i + 1), BazaarNotifier.config.numberColor.toJavaColor()));
            message.add(new ColoredText(". ", BazaarNotifier.config.numberColor.toJavaColor()));
            message.add(new ColoredText(itemNameConverted, BazaarNotifier.config.itemColor.toJavaColor()));

            if (BazaarNotifier.config.craftingModule.showProfitPerMil
                || BazaarNotifier.config.craftingModule.showInstantSellProfit
                || BazaarNotifier.config.craftingModule.showSellOfferProfit) {
              message.add(new ColoredText(" - ", BazaarNotifier.config.infoColor.toJavaColor()));
            }

            if (BazaarNotifier.config.craftingModule.showInstantSellProfit) {
              message.add(new ColoredText(BazaarNotifier.df.format(profitInstaSell),
                  getColor(profitInstaSell.intValue())));
            }
            if (BazaarNotifier.config.craftingModule.showInstantSellProfit
                && BazaarNotifier.config.craftingModule.showSellOfferProfit) {
              message.add(new ColoredText(" / ", BazaarNotifier.config.infoColor.toJavaColor()));
            }
            if (BazaarNotifier.config.craftingModule.showSellOfferProfit) {
              message.add(new ColoredText(BazaarNotifier.df.format(profitSellOffer),
                  getColor(profitSellOffer.intValue())));
            }
            if (BazaarNotifier.config.craftingModule.showSellOfferProfit
                && BazaarNotifier.config.craftingModule.showProfitPerMil) {
              message.add(new ColoredText(" /  ", BazaarNotifier.config.infoColor.toJavaColor()));
            }
            if (BazaarNotifier.config.craftingModule.showProfitPerMil) {
              message.add(new ColoredText(BazaarNotifier.df.format(pricePerMil) + "%",
                  getColorForMil(pricePerMil.intValue())));
            }
          } else {
            message.add(new ColoredText("Error, just wait", Color.RED));
          }
          items.add(message);
        }
      }
      longestString = RenderUtils.getLongestString(items);

      RenderUtils.drawColorfulParagraph(items, (int)position.getX(), (int)position.getY(), scale);
      if(BazaarNotifier.inBazaar) {
        renderMaterials(checkHoveredText(), list);
      }
    } else {
      RenderUtils.drawCenteredString("Waiting for bazaar data", (int)position.getX(), (int)position.getY(), 0xAAAAAA, scale);
      //Todo add height and width
    }
    GL11.glTranslated(0, 0, -1);
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
    position.setPosition(Defaults.CRAFTING_MODULE_X, Defaults.CRAFTING_MODULE_Y);
    setScale(1, false);
    enabled = true;
    BazaarNotifier.config.craftingModule.craftingListLength = Defaults.CRAFTING_LIST_LENGTH;
  }

  @Override
  public String name() {
    return ModuleName.CRAFTING.name();
  }

  @Override
  protected boolean shouldDrawBounds() {
    return true;
  }

  @Override
  protected int getMaxShift() {
    return list.size() - BazaarNotifier.config.craftingModule.craftingListLength;
  }

  protected int checkHoveredText() {
    float _y = position.getY() + 11 * scale;
    float y2 = _y + ((BazaarNotifier.config.craftingModule.craftingListLength) * 11 * scale);
    int mouseYFormatted = getMouseCoordinateY();
    int mouseXFormatted = getMouseCoordinateX();
    float relativeYMouse = (mouseYFormatted - _y) / (11 * scale);
    if (getWidth(scale, false) != 0) {
      if (inMovementBox() && mouseYFormatted >= _y && mouseYFormatted <= y2 - 3 * scale) {
        return (int) relativeYMouse + shift;
      } else {
        return -1;
      }
    } else {
      return 1;
    }
  }

  protected void renderMaterials(int hoveredText, ArrayList<String[]> list) {
    checkMouseMovement();
    ArrayList<ArrayList<ColoredText>> material = new ArrayList<>();
    ArrayList<ColoredText> text = new ArrayList<>();

    if (hoveredText > -1) {
      if (hoveredText < list.size()) {

        int materialCount;
        materialCount = BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
            .getAsJsonObject(list.get(hoveredText)[6]).getAsJsonArray("material").size();
        for (int b = 0; b < materialCount / 2; b++) {
          StringBuilder _material = new StringBuilder();
          if (b == 0) {
            _material.append((BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                .getAsJsonObject(list.get(hoveredText)[6]).getAsJsonArray("material").get(1)
                .getAsInt()
                * mouseWheelShift)).append("x ").append(BazaarNotifier.bazaarConv
                .get(BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                    .getAsJsonObject(list.get(hoveredText)[6]).getAsJsonArray("material")
                    .get(0).getAsString()));
          } else {
            _material.append(" | ").append(
                BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                    .getAsJsonObject(list.get(hoveredText)[6]).getAsJsonArray("material")
                    .get(b * 2 + 1).getAsInt() * mouseWheelShift).append("x ").append(
                BazaarNotifier.bazaarConv.get(
                    BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                        .getAsJsonObject(list.get(hoveredText)[6]).getAsJsonArray("material")
                        .get(b * 2).getAsString()));
          }

          text.add(new ColoredText(_material.toString(), Color.LIGHT_GRAY));
        }
        material.add(text);
        int longestXString = RenderUtils.drawColorfulParagraph(material, getMouseCoordinateX(),
            getMouseCoordinateY() - (int) (8 * scale), scale);
        Gui.drawRect(getMouseCoordinateX() - padding,
            getMouseCoordinateY() - (int) (8 * scale) - (int) (padding * scale),
            (int) (getMouseCoordinateX() + longestXString + padding * scale),
            (int) (getMouseCoordinateY() + padding * scale), 0xFF404040);
        RenderUtils.drawColorfulParagraph(material, getMouseCoordinateX(),
            getMouseCoordinateY() - (int) (8 * scale), scale);
      }
    }
  }


  public void checkMouseMovement() {
    if (lastHovered != checkHoveredText()) {
      mouseWheelShift = 1;
    }
    lastHovered = checkHoveredText();
  }
}