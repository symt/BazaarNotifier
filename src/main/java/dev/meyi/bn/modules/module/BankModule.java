package dev.meyi.bn.modules.module;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.migration.JsonName;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.modules.calc.BankCalculator;
import dev.meyi.bn.utilities.RenderUtils;
import dev.meyi.bn.utilities.Defaults;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class BankModule extends Module {

  @JsonName("bazaarProfit")
  public double bazaarProfit = 0;
  public BankModule() {
    super();
  }

  public transient static final ModuleName type = ModuleName.BANK;
  transient int  lines = 2;
  transient float longestXString = 1;

  @JsonName("bankRawDifference")
  @Switch(name = "Raw Difference",
          category = "Bank Module",
          description = "No clue"//Todo
  )
  public boolean bankRawDifference = Defaults.BANK_RAW_DIFFERENCE;

  @Override
  protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
    draw();
  }

  @Override
  protected float getWidth(float scale, boolean example) {
    if(example) {
      return 150*scale;
    }else {
      return longestXString;
    }
  }

  @Override
  protected float getHeight(float scale, boolean example) {
    return ((Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * lines) + lines )*scale - 2;
  }


  @Override
  public void draw() {
    GL11.glTranslated(0, 0, 1);
    drawBounds();
    List<LinkedHashMap <String, Color>> items = new ArrayList<>();

    LinkedHashMap <String, Color> header = new LinkedHashMap <>();
    header.put("Bank Module (Experimental)", BazaarNotifier.config.infoColor.toJavaColor());
    items.add(header);

    LinkedHashMap <String, Color> message2 = new LinkedHashMap <>();
    message2.put("Bazaar Profit: ", BazaarNotifier.config.itemColor.toJavaColor());
    message2.put(BazaarNotifier.df.format(BankCalculator.getBazaarProfit()), Color.ORANGE);
    items.add(message2);


    if (BazaarNotifier.config.bankModule.bankRawDifference) {
      LinkedHashMap <String, Color> message3 = new LinkedHashMap <>();
      message3.put("Bazaar Difference: ", BazaarNotifier.config.itemColor.toJavaColor());
      message3.put(BazaarNotifier.df.format(BankCalculator.getRawDifference()), Color.ORANGE);
      items.add(message3);
    }

    lines = BazaarNotifier.config.bankModule.bankRawDifference?3:2;
    longestXString = RenderUtils.drawColorfulParagraph(items, (int)position.getX(), (int)position.getY(), scale);
    GL11.glTranslated(0, 0, -1);
  }

  @Override
  protected void reset() {
    position.setPosition(Defaults.BANK_MODULE_X, Defaults.BANK_MODULE_Y);
    setScale(1, false);
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
