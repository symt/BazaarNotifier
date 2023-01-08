package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.Order;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;


public class RenderUtils {


  /**
   * @param renderer Minecraft's renderer
   * @param x horizontal coordinate for rendering
   * @param y vertical coordinate for rendering
   * @param message text split up by color in order of rendering
   * @param dropShadow should the shadow have text
   * @return length of the entire text
   */
  public static int drawMulticoloredString(FontRenderer renderer, int x, int y,
      LinkedHashMap<String, Color> message, boolean dropShadow, float moduleScale) {

    int renderLength = 0;
    for (Entry<String, Color> substring : message.entrySet()) {
      GL11.glScalef(moduleScale, moduleScale, 1);
      renderer.drawString(substring.getKey(), (x + renderLength) / moduleScale, y / moduleScale,
          substring.getValue().getRGB(),
          dropShadow);
      renderLength += renderer.getStringWidth(substring.getKey()) * moduleScale;
      GL11.glScalef((float) Math.pow(moduleScale, -1), (float) Math.pow(moduleScale, -1), 1);
    }

    return renderLength;
  }

  public static int drawColorfulParagraph(List<LinkedHashMap<String, Color>> items, int x, int y,
      float moduleScale) {
    float longestXString = 0;
    for (int i = 0; i < items.size(); i++) {
      float fontHeight =
          (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 2) * i * moduleScale;
      int length = RenderUtils
          .drawMulticoloredString(Minecraft.getMinecraft().fontRendererObj,
              x, y
                  + (int) fontHeight,
              items.get(i), false, moduleScale);
      if (length > longestXString) {
        longestXString = length;
      }
    }
    return (int) longestXString;
  }

  public static void chatNotification(Order order, String notification) {
    if (!BazaarNotifier.config.showChatMessages) {
      return;
    }
    EnumChatFormatting messageColor =
        (notification.equalsIgnoreCase("REVIVED") ? EnumChatFormatting.GREEN
            : order.type.equals(Order.OrderType.BUY) ? EnumChatFormatting.DARK_PURPLE
                : EnumChatFormatting.BLUE);
    if (Minecraft.getMinecraft().thePlayer != null) {
      Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
          messageColor + order.type.longName
              + EnumChatFormatting.GRAY + " for "
              + messageColor + BazaarNotifier.dfNoDecimal
              .format(order.startAmount)
              + EnumChatFormatting.GRAY + "x " + messageColor
              + order.product
              + EnumChatFormatting.YELLOW
              + " " + notification + " " + EnumChatFormatting.GRAY + "("
              + messageColor + BazaarNotifier.df.format(order.pricePerUnit)
              + EnumChatFormatting.GRAY + ")"
      ));
    }
  }

  public static void drawCenteredString(String text, int x, int y, int color, float moduleScale) {
    x = (int) (x / moduleScale + 200 / 4);
    y = (int) (y / moduleScale + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 6));
    GL11.glScalef(moduleScale, moduleScale, 1);
    Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color);
    GL11.glScalef((float) Math.pow(moduleScale, -1), (float) Math.pow(moduleScale, -1), 1);
  }
}