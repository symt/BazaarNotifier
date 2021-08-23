package dev.meyi.bn.modules;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.ColorUtils;
import dev.meyi.bn.utilities.Defaults;
import dev.meyi.bn.utilities.Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.json.JSONObject;

public class SuggestionModule extends Module {

  public SuggestionModule() {
    super();
  }

  public SuggestionModule(JSONObject module) {
    super(module);
  }

  @Override
  protected void draw() {
    if (BazaarNotifier.bazaarDataFormatted.length() != 0) {
      List<LinkedHashMap<String, Color>> items = new ArrayList<>();

      for (int i = shift; i < BazaarNotifier.config.getInt("suggesterLength") + shift; i++) {
        LinkedHashMap<String, Color> message = new LinkedHashMap<>();
        message.put((i + 1) + ". ", Color.MAGENTA);
        message.put(BazaarNotifier.bazaarDataFormatted.getJSONObject(i).getString("productId"),
            Color.CYAN);
        message.put(" - ", Color.GRAY);
        message.put("EP: ", Color.RED);
        message.put("" + BazaarNotifier.df.format(
            BazaarNotifier.bazaarDataFormatted.getJSONObject(i)
                .getDouble("profitFlowPerMinute")), Color.ORANGE);
        items.add(message);
      }

      int longestXString = ColorUtils.drawColorfulParagraph(items, x, y);

      boundsX = x + longestXString;

    } else {
      Utils.drawCenteredString("Waiting for bazaar data", x + 100,
          y + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 10 + 16) / 2, 0xAAAAAA, 1F);
      boundsX = x + 200;
    }
    boundsY = y + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * BazaarNotifier.config.getInt("suggesterLength") + BazaarNotifier.config.getInt("craftingLength") *2-2;
  }

  @Override
  protected void reset() {
    x = Defaults.SUGGESTION_MODULE_X;
    y = Defaults.SUGGESTION_MODULE_Y;
  }

  @Override
  protected String name() {
    return ModuleName.SUGGESTION.name();
  }

  @Override
  protected boolean shouldDrawBounds() {
    return true;
  }

  @Override
  protected int getMaxShift() {
    return BazaarNotifier.bazaarDataFormatted.length() - 10;
  }

  @Override
  public JSONObject generateModuleConfig() {
    return super.generateModuleConfig();
  }




}


