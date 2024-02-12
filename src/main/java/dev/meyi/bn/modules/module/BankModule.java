package dev.meyi.bn.modules.module;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.migration.JsonName;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.modules.calc.BankCalculator;
import dev.meyi.bn.utilities.ColoredText;
import dev.meyi.bn.utilities.RenderUtils;
import dev.meyi.bn.utilities.Defaults;
import java.awt.Color;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class BankModule extends Module {
  @JsonName("bazaarProfit")
  public double bazaarProfit = 0;

  @JsonName("bazaarDailyAmount")
  public double bazaarDailyAmount = 1E10;

  public BankModule() {
    super();
  }

  public transient static final ModuleName type = ModuleName.BANK;
  transient int lines = 2;

  @JsonName("bankRawDifference")
  @Switch(name = "Raw Difference",
          category = "Bank Module",
          description = "Show profit including current orders"
  )
  public boolean bankRawDifference = Defaults.BANK_RAW_DIFFERENCE;

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
    return ((Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * lines) + lines )*scale - 2;
  }


  @Override
  public void draw() {
    GL11.glTranslated(0, 0, 1);
    drawBounds();
    ArrayList<ArrayList<ColoredText>> items = new ArrayList<>();

    ArrayList <ColoredText> header = new ArrayList<>();
    header.add(new ColoredText("Bank Module", BazaarNotifier.config.infoColor.toJavaColor()));
    items.add(header);

    ArrayList <ColoredText> bazaarProfitMessage = new ArrayList<>();
    bazaarProfitMessage.add(new ColoredText("Bazaar Profit: ", BazaarNotifier.config.itemColor.toJavaColor()));
    bazaarProfitMessage.add(new ColoredText(BazaarNotifier.df.format(bazaarProfit), Color.ORANGE));
    items.add(bazaarProfitMessage);


    if (BazaarNotifier.config.bankModule.bankRawDifference) {
      ArrayList<ColoredText> bazaarDifferenceMessage = new ArrayList<>();
      bazaarDifferenceMessage.add(new ColoredText("Bazaar Difference: ", BazaarNotifier.config.itemColor.toJavaColor()));
      bazaarDifferenceMessage.add(new ColoredText(BazaarNotifier.df.format(BankCalculator.getRawDifference()), Color.ORANGE));
      items.add(bazaarDifferenceMessage);
    }

    if (BazaarNotifier.config.bankModule.bazaarDailyAmount <= 1E9) {
      ArrayList<ColoredText> bazaarDifferenceMessage = new ArrayList<>();
      bazaarDifferenceMessage.add(new ColoredText("Daily Limit: ", BazaarNotifier.config.itemColor.toJavaColor()));
      if (BazaarNotifier.config.bankModule.bazaarDailyAmount > 0) {
        bazaarDifferenceMessage.add(new ColoredText(
            BazaarNotifier.df.format(Math.max(BazaarNotifier.config.bankModule.bazaarDailyAmount, 0)),
            Color.ORANGE));
      } else {
        bazaarDifferenceMessage.add(new ColoredText("NONE", Color.RED));
      }
      items.add(bazaarDifferenceMessage);
    }

    lines = 2 + (BazaarNotifier.config.bankModule.bankRawDifference ? 1 : 0)
              + (BazaarNotifier.config.bankModule.bazaarDailyAmount <= 1E9 ? 1 : 0);


    longestString = RenderUtils.getLongestString(items);
    RenderUtils.drawColorfulParagraph(items, (int)position.getX(), (int)position.getY(), scale);
    GL11.glTranslated(0, 0, -1);
  }

  @Override
  protected void reset() {
    position.setPosition(Defaults.BANK_MODULE_X, Defaults.BANK_MODULE_Y);
    setScale(1, false);

    BankCalculator.reset();
    enabled = true;
  }

  @Override
  public String name() {
    return ModuleName.BANK.name();
  }

  @Override
  protected boolean shouldDrawBounds() {
    return true;
  }

  @Override
  protected int getMaxShift() {
    return 0;
  }
}
