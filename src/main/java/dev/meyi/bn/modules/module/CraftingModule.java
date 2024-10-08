package dev.meyi.bn.modules.module;

import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.DualOption;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.migration.JsonName;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.calculation.CraftingPriceCalculation;
import dev.meyi.bn.modules.HoverableModule;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import dev.meyi.bn.utilities.ColoredText;
import dev.meyi.bn.utilities.Defaults;
import dev.meyi.bn.utilities.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;


public class CraftingModule extends HoverableModule {

  public transient static final ModuleName type = ModuleName.CRAFTING;
  public transient static ArrayList<CraftingPriceCalculation> craftingPrices = new ArrayList<>();
  private transient static boolean mouseButtonDown;
  @JsonName("craftingListLength")
  @Slider(name = "Crafting List Entries",
      category = "Crafting Module",
      description = "The amount of entries in the Crafting Module list",
      min = 1, max = 25, step = 1
  )
  public int craftingListLength = Defaults.CRAFTING_LIST_LENGTH;
  @JsonName("craftingSortingOption")
  @Dropdown(name = "Crafting Sorting Option",
      options = {"Instant Sell", "Sell Offer", "1m instant"},
      category = "Crafting Module",
      description = "After which condition should the crafting module be sorted"
  )
  public int craftingSortingOption = Defaults.CRAFTING_SORTING_OPTION;
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
  transient int lastHovered = 0;


  public CraftingModule() {
    super();
  }

  @Override
  protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
    draw();
  }

  @Override
  protected float getWidth(float scale, boolean example) {
    return RenderUtils.getStringWidth(longestString) * scale + 2 * padding * scale;
  }

  @Override
  protected float getHeight(float scale, boolean example) {
    try {
      return ((Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT
          * (BazaarNotifier.config.craftingModule.craftingListLength + 1)
          + (BazaarNotifier.config.craftingModule.craftingListLength + 1) * 2) * scale - 2)
          + 2 * scale * padding;
    } catch (NullPointerException e) {
      return 1;
    }
  }


  private ArrayList<ColoredText> generateHelperLine() {
    if (Mouse.isButtonDown(1) && getMouseCoordinateY() > position.getY() - 1
        && getMouseCoordinateY()
        < position.getY() + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 1) * scale) {
      int[] width = new int[4];
      int totalWidth = 0;
      int relativeX = getMouseCoordinateX() - (int) position.getX();
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
          (int) (Minecraft.getMinecraft().fontRendererObj.getStringWidth("/ Profit %") * scale)
          : 0;

      for (int i : width) {
        totalWidth += i;
      }

      for (int i = 3; i >= 0; i--) {
        if (totalWidth > relativeX && totalWidth - width[i] < relativeX && inMovementBox()
            && BazaarNotifier.inBazaar) {
          switch (i) {
            case 0:
              if (mouseButtonDown) {
                BazaarNotifier.config.craftingModule.useBuyOrders ^= true;
                mouseButtonDown = false;
              }
              break;
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
          CraftingCalculator.sort();
          break;
        }
        totalWidth -= width[i];
      }
    } else if (!Mouse.isButtonDown(1)) {
      mouseButtonDown = true;
    }
    ArrayList<ColoredText> helperLine = new ArrayList<>();
    helperLine.add(new ColoredText("   ", BazaarNotifier.config.infoColor.toJavaColor()));
    helperLine
        .add(new ColoredText(
            BazaarNotifier.config.craftingModule.useBuyOrders ? "Crafting (Buy Orders)"
                : "Crafting (Instant Buy)",
            BazaarNotifier.config.infoColor.toJavaColor()));
    if (BazaarNotifier.config.craftingModule.showProfitPerMil || BazaarNotifier.config
        .craftingModule.showInstantSellProfit
        || BazaarNotifier.config.craftingModule.showSellOfferProfit) {
      helperLine.add(new ColoredText(" - ", BazaarNotifier.config.infoColor.toJavaColor()));
    }
    if (BazaarNotifier.config.craftingModule.showInstantSellProfit) {
      helperLine.add(new ColoredText(" Instant Sell",
          BazaarNotifier.config.craftingModule.craftingSortingOption == 0 ?
              new Color(141, 152, 201) : BazaarNotifier.config.infoColor.toJavaColor()));
      if (BazaarNotifier.config.craftingModule.showSellOfferProfit
          || BazaarNotifier.config.craftingModule.showProfitPerMil) {
        helperLine.add(new ColoredText(" /", BazaarNotifier.config.infoColor.toJavaColor()));
      }
    }
    if (BazaarNotifier.config.craftingModule.showSellOfferProfit) {
      helperLine.add(new ColoredText(" Sell Offer",
          BazaarNotifier.config.craftingModule.craftingSortingOption == 1 ?
              new Color(141, 152, 201) : BazaarNotifier.config.infoColor.toJavaColor()));
      if (BazaarNotifier.config.craftingModule.showProfitPerMil) {
        helperLine.add(new ColoredText(" /", BazaarNotifier.config.infoColor.toJavaColor()));
      }
    }
    if (BazaarNotifier.config.craftingModule.showProfitPerMil) {
      helperLine.add(new ColoredText(" Profit %",
          BazaarNotifier.config.craftingModule.craftingSortingOption == 2 ?
              new Color(141, 152, 201) : BazaarNotifier.config.infoColor.toJavaColor()));
    }

    return helperLine;
  }

  @Override
  public void draw() {
    GL11.glTranslated(0, 0, 1);
    if (BazaarNotifier.bazaarDataRaw != null) {
      ArrayList<ArrayList<ColoredText>> items = new ArrayList<>();
      items.add(generateHelperLine());
      for (int i = shift; i < BazaarNotifier.config.craftingModule.craftingListLength + shift;
          i++) {
        ArrayList<ColoredText> message = new ArrayList<>();
        if (i < craftingPrices.size()) {
          boolean flag = BazaarNotifier.config.craftingModule.useBuyOrders;
          CraftingPriceCalculation prices = craftingPrices.get(i);

          Double profitInstaSell = flag ? prices.buyOrderInstantSell : prices.instantBuyInstantSell;
          Double profitSellOffer = flag ? prices.buyOrderSellOffer : prices.instantBuySellOffer;
          Double profitPercentage =
              flag ? prices.buyOrderSellPercentage : prices.instantBuySellPercentage;
          String itemName = prices.itemName;

          String itemNameConverted = BazaarNotifier.itemConversionMap.get(itemName);
          message.add(new ColoredText(String.valueOf(i + 1),
              BazaarNotifier.config.numberColor.toJavaColor()));
          message.add(new ColoredText(". ", BazaarNotifier.config.numberColor.toJavaColor()));
          message.add(
              new ColoredText(itemNameConverted, BazaarNotifier.config.itemColor.toJavaColor()));

          if (BazaarNotifier.config.craftingModule.showProfitPerMil
              || BazaarNotifier.config.craftingModule.showInstantSellProfit
              || BazaarNotifier.config.craftingModule.showSellOfferProfit) {
            message.add(new ColoredText(" - ", BazaarNotifier.config.infoColor.toJavaColor()));
          }

          if (BazaarNotifier.config.craftingModule.showInstantSellProfit) {
            message.add(new ColoredText(BazaarNotifier.df.format(profitInstaSell),
                getColor(profitInstaSell.intValue())));

            if (BazaarNotifier.config.craftingModule.showSellOfferProfit) {
              message.add(new ColoredText(" / ", BazaarNotifier.config.infoColor.toJavaColor()));
            }
          }

          if (BazaarNotifier.config.craftingModule.showSellOfferProfit) {
            message.add(new ColoredText(BazaarNotifier.df.format(profitSellOffer),
                getColor(profitSellOffer.intValue())));

            if (BazaarNotifier.config.craftingModule.showProfitPerMil) {
              message.add(new ColoredText(" /  ", BazaarNotifier.config.infoColor.toJavaColor()));
            }
          }

          if (BazaarNotifier.config.craftingModule.showProfitPerMil) {
            message.add(new ColoredText(BazaarNotifier.df.format(profitPercentage) + "%",
                getColorForPercentage(profitPercentage.intValue())));
          }
          items.add(message);
        }
      }
      longestString = RenderUtils.getLongestString(items);

      RenderUtils.drawColorfulParagraph(items, (int) position.getX() + padding,
          (int) position.getY() + padding, scale);
    } else {
      RenderUtils.drawCenteredString("Waiting for bazaar data", (int) position.getX(),
          (int) position.getY(), 0xAAAAAA, scale);
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

  protected Color getColorForPercentage(int price) {
    if (price <= 0) {
      return Color.RED;
    } else if (price <= 10) {
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
    return craftingPrices.size() - BazaarNotifier.config.craftingModule.craftingListLength;
  }

  protected ArrayList<ArrayList<ColoredText>> generateTooltipText(int lineNumber) {
    lineNumber--; // remove header from crafting module line number

    checkMouseMovement();
    ArrayList<ArrayList<ColoredText>> para = new ArrayList<>();

    if (lineNumber > -1) {
      if (lineNumber < craftingPrices.size()) {

        CraftingPriceCalculation prices = craftingPrices.get(lineNumber);

        for (Map.Entry<String, Integer> recipe : BazaarNotifier.craftingRecipeMap.get(
            prices.itemName).material.entrySet()) {
          ArrayList<ColoredText> text = new ArrayList<>();

          text.add(new ColoredText(BazaarNotifier.itemConversionMap.get(recipe.getKey()), Color.YELLOW));
          text.add(new ColoredText(" (", Color.LIGHT_GRAY));
          text.add(new ColoredText(BazaarNotifier.dfNoDecimal.format(recipe.getValue()) + "x",
              Color.MAGENTA));
          text.add(new ColoredText(")", Color.LIGHT_GRAY));

          para.add(text);
        }
      }
    }
    return para;
  }


  public void checkMouseMovement() {
    if (lastHovered != getHoveredLineNumber()) {
      mouseWheelShift = 1;
    }
    lastHovered = getHoveredLineNumber();
  }
}