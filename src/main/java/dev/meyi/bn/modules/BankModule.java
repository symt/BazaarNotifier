package dev.meyi.bn.modules;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.config.ModuleConfig;
import dev.meyi.bn.utilities.ColorUtils;
import dev.meyi.bn.utilities.Defaults;
import dev.meyi.bn.modules.calc.BankCalculator;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;

public class BankModule extends Module {
  public static final ModuleName type = ModuleName.BANK;

  public BankModule() {
    super();
  }

  public BankModule(ModuleConfig config) {
    super(config);
  }


  @Override
  protected void draw() {
    List<LinkedHashMap<String, Color>> items = new ArrayList<>();

    LinkedHashMap<String, Color> message = new LinkedHashMap<>();
    message.put("Total profit: ", Color.CYAN);
    message.put(BazaarNotifier.df.format((int) BankCalculator.calculateProfit()), Color.MAGENTA);
    items.add(message);
    LinkedHashMap<String, Color> message2 = new LinkedHashMap<>();
    message2.put("Bazaar profit: ", Color.CYAN);
    message2.put(BazaarNotifier.df.format(BankCalculator.bazaarProfit) , Color.MAGENTA);
    items.add(message2);

    int longestXString = ColorUtils.drawColorfulParagraph(items, x, y, scale);
    boundsX = x + longestXString;
    boundsY = (int) (y + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 2) * scale);
  }

  @Override
  protected void reset() {
    x = Defaults.BANK_MODULE_X;
    y = Defaults.BANK_MODULE_Y;
    scale = 1;
    active = true;
  }

  @Override
  protected String name() {
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
