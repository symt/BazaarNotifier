package dev.meyi.bn.utilities;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;


public class ColorUtils {



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
      GL11.glScalef(moduleScale, moduleScale,1);
      renderer.drawString(substring.getKey(), (x + renderLength)/moduleScale, y/moduleScale, substring.getValue().getRGB(),
          dropShadow);
      renderLength += renderer.getStringWidth(substring.getKey())*moduleScale;
      GL11.glScalef((float)Math.pow(moduleScale, -1),(float)Math.pow(moduleScale, -1),1);
    }

    return renderLength;
  }

  public static int drawColorfulParagraph(List<LinkedHashMap<String, Color>> items, int x, int y, float moduleScale) {
    float longestXString = 0;
    for (int i = 0; i < items.size(); i++) {
      float fontHeight = (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 2) * i * moduleScale;
      int length = ColorUtils
          .drawMulticoloredString(Minecraft.getMinecraft().fontRendererObj,
              x, y
                  + (int)fontHeight,
              items.get(i), false, moduleScale);
      if (length > longestXString) {
        longestXString = length;
      }
    }
    return (int)longestXString;
  }
}