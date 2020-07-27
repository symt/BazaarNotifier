package dev.meyi.bn.modules;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.Defaults;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.json.JSONObject;

public class NotificationModule extends Module {

  public NotificationModule() {
    super();
  }

  public NotificationModule(JSONObject module) {
    super(module);
  }

  @Override
  protected void draw() {
    // add extra space after "Buy" so it lines up with sell

    List<LinkedHashMap<String, Color>> items = new ArrayList<>();

    for (int i = 0; i < BazaarNotifier.orders.length(); i++) {
      // Do stuff
    }

    boundsX =
        x + Minecraft.getMinecraft().fontRendererObj.getStringWidth(Defaults.LONGEST_NOTIFICATION);
    boundsY = y + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * Math.max(items.size(), 10)
        + 2 * (Math.max(items.size(), 10) - 1);
  }

  @Override
  protected void reset() {
    x = Defaults.NOTIFICATION_MODULE_X;
    y = Defaults.NOTIFICATION_MODULE_Y;
  }

  @Override
  protected String name() {
    return ModuleName.NOTIFICATION.name();
  }


  @Override
  protected boolean shouldDrawBounds() {
    return false;
  }

}
