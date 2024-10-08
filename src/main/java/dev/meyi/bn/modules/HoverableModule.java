package dev.meyi.bn.modules;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.migration.JsonName;
import dev.meyi.bn.utilities.ColoredText;
import dev.meyi.bn.utilities.Defaults;
import dev.meyi.bn.utilities.RenderUtils;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

public abstract class HoverableModule extends Module {

  @JsonName("snapTooltips")
  @Switch(name = "Render Tooltips",
      description = "Render tooltips when hovering over items in module")
  public boolean renderTooltips = Defaults.RENDER_TOOLTIPS;

  @JsonName("snapTooltips")
  @Switch(name = "Snap Tooltips",
      description = "Float tooltips or snap to right side next to the item")
  public boolean snapTooltips = Defaults.SNAP_TOOLTIP_LOCATION;

  protected abstract ArrayList<ArrayList<ColoredText>> generateTooltipText(int lineNumber);

  protected void renderTooltip() {
    if (renderTooltips) {
      int lineNumber = getHoveredLineNumber();

      if (lineNumber != -1) {
        ArrayList<ArrayList<ColoredText>> tooltipText = generateTooltipText(lineNumber);

        if (tooltipText != null && tooltipText.size() != 0) {
          drawTooltip(tooltipText, lineNumber);
        }
      }
    }
  }

  protected int getHoveredLineNumber() {
    int mouseYFormatted = getMouseCoordinateY();
    float lineNumber = (mouseYFormatted - position.getY()) / (
        (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 2) * scale);
    if (inMovementBox()) {
      return (int) lineNumber;
    } else {
      return -1;
    }
  }

  protected void drawTooltip(ArrayList<ArrayList<ColoredText>> textToRender, int lineNumber) {
    if (textToRender == null) {
      return;
    }

    int fontHeight = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;

    int[] corner = new int[2];
    if (snapTooltips) {
      corner[0] = (int) position.getRightX(); // x
      corner[1] = (int) (position.getY() + (fontHeight + 2) * lineNumber); // y
    } else {
      corner[0] = getMouseCoordinateX();
      corner[1] = (int) (getMouseCoordinateY()
          - (fontHeight + 2) * textToRender.size() / 2 * scale);
    }

    // Not sure what the minimum is to be rendered on top of items, but this works
    GL11.glTranslated(0, 0, 500);

    int longestXString = RenderUtils.drawColorfulParagraph(textToRender,
        (int) (corner[0] + padding * scale),
        (int) (corner[1] + padding * scale), scale);

    GL11.glTranslated(0, 0, -1);

    Gui.drawRect(corner[0],
        corner[1],
        (int) (corner[0] + longestXString + padding * 2 * scale),
        (int) (corner[1] + ((fontHeight + 2) * textToRender.size() + (padding - 2) * 2) * scale),
        0xFF404040);

    GL11.glTranslated(0, 0, -499);
  }
}
