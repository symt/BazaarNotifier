package dev.meyi.bn.modules;

import com.google.gson.JsonObject;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.ColorUtils;
import dev.meyi.bn.utilities.Defaults;
import dev.meyi.bn.utilities.Order;
import dev.meyi.bn.utilities.Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.text.WordUtils;

public class NotificationModule extends Module {
  public static final ModuleName type = ModuleName.NOTIFICATION;

  public NotificationModule() {
    super();
  }

  public NotificationModule(JsonObject module) {
    super(module);
  }

  @Override
  protected void draw() {
    // add extra space after "Buy" so it lines up with sell

    List<LinkedHashMap<String, Color>> items = new ArrayList<>();

    if (BazaarNotifier.newOrders.size() != 0) {

      int size = Math.min(shift + 10, BazaarNotifier.newOrders.size());

      for (int i = shift; i < size; i++) {
        Order currentOrder = BazaarNotifier.newOrders.get(i);
        LinkedHashMap<String, Color> message = new LinkedHashMap<>();

        Color typeSpecificColor = currentOrder.goodOrder ? new Color(0x55FF55)
            : currentOrder.type.equals("buy") ? new Color(0xFF55FF)
                : new Color(0x55FFFF);

        String notification = currentOrder.goodOrder ? "BEST" :
            currentOrder.matchedOrder ? "MATCHED" : "OUTDATED";
        message.put(WordUtils.capitalizeFully(currentOrder.type), typeSpecificColor);
        message.put(" - ", new Color(0xAAAAAA));
        message.put(notification + " ", new Color(0xFFFF55));
        message.put("(", new Color(0xAAAAAA));
        message.put(BazaarNotifier.dfNoDecimal.format(currentOrder.startAmount),
            typeSpecificColor);
        message.put("x ", new Color(0xAAAAAA));
        message.put(currentOrder.product, typeSpecificColor);
        message.put(", ", new Color(0xAAAAAA));
        message.put(BazaarNotifier.df.format(currentOrder.pricePerUnit),
            typeSpecificColor);
        message.put(")", new Color(0xAAAAAA));
        items.add(message);
      }

      int longestXString = ColorUtils.drawColorfulParagraph(items, x, y, scale);
      boundsX = x + longestXString;
    } else {
      Utils.drawCenteredString("No orders found", (int) ((x / scale) + 200 * scale / scale / 4),
          (int) (y / scale + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 6)), 0xAAAAAA,
          scale);
      float X = x + 200 * scale;
      boundsX = (int) X;
    }
    float Y =
        y + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * scale * 10 + 20 * scale - 2;
    boundsY = (int) Y;
  }

  @Override
  protected void reset() {
    x = Defaults.NOTIFICATION_MODULE_X;
    y = Defaults.NOTIFICATION_MODULE_Y;
    scale = 1;
  }

  @Override
  protected String name() {
    return ModuleName.NOTIFICATION.name();
  }

  @Override
  protected boolean shouldDrawBounds() {
    return true;
  }

  @Override
  protected int getMaxShift() {
    return BazaarNotifier.newOrders.size() - 10;
  }


}
