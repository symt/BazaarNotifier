package dev.meyi.bn.modules;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.config.Configuration;
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
  public static final ModuleName type = ModuleName.SUGGESTION;

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

      for (int i = shift; i < Configuration.suggestionListLength + shift; i++) {
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

      int longestXString = ColorUtils.drawColorfulParagraph(items, x, y, scale);

      boundsX = x + longestXString;

    } else {
      Utils.drawCenteredString("Waiting for bazaar data", x + 100,
          y + (int) ((Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 2) * 5 * scale),
          0xAAAAAA, scale);
      boundsX = x + 200;
    }
    float Y = y + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * scale
        * Configuration.suggestionListLength + Configuration.suggestionListLength * 2 * scale - 2;
    boundsY = (int) Y;
  }

  @Override
  protected void reset() {
    x = Defaults.SUGGESTION_MODULE_X;
    y = Defaults.SUGGESTION_MODULE_Y;
    scale = 1;
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
    return BazaarNotifier.bazaarDataFormatted.length() - Configuration.suggestionListLength;
  }

  @Override
  public JSONObject generateModuleConfig() {
    return super.generateModuleConfig();
  }


}


