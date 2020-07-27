package dev.meyi.bn.modules;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.ColorUtils;
import dev.meyi.bn.utilities.Defaults;
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

      for (int i = 0; i < 10; i++) {
        LinkedHashMap<String, Color> message = new LinkedHashMap<>();
        message.put((i + 1) + ". ", Color.BLUE);
        message.put(BazaarNotifier.bazaarDataFormatted.getJSONObject(i).getString("productId"),
            Color.CYAN);
        message.put(" - ", Color.GRAY);
        message.put("EP: ", Color.RED);
        message.put("" + BazaarNotifier.df.format(
            BazaarNotifier.bazaarDataFormatted.getJSONObject(i)
                .getDouble("profitFlowPerMinute")), Color.ORANGE);
        items.add(message);
      }

      int longestXString = 0;
      for (int i = 0; i < items.size(); i++) {
        int length = ColorUtils
            .drawMulticoloredString(Minecraft.getMinecraft().fontRendererObj,
                x, y
                    + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 2) * i,
                items.get(i), false);

        if (length > longestXString) {
          longestXString = length;
        }
      }
      boundsX = x + longestXString;
      boundsY = y
          + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * items.size() + 2 * (
          items.size() - 1);
    }
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
    return BazaarNotifier.bazaarDataFormatted.length() != 0;
  }

  @Override
  public JSONObject generateModuleConfig() {
    return super.generateModuleConfig();
  }
}
