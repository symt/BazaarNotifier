package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class MouseHandler {

  boolean mouseButtonDownAlready = false;
  int x = 0;
  int y = 0;
  int scaleFactor;

  @SubscribeEvent
  public void mouseActionCheck(TickEvent e) {
    if (e.phase == TickEvent.Phase.START) {
      if (BazaarNotifier.inBazaar) {
        if (Mouse.isButtonDown(0)) {
          scaleFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
          if (!mouseButtonDownAlready && inMovementBox()) {
            mouseButtonDownAlready = true;
          } else if (mouseButtonDownAlready) {
            int shiftedX = getMouseCoordinateX() - x;
            int shiftedY = getMouseCoordinateY() - y;

            BazaarNotifier.X_POS += shiftedX;
            BazaarNotifier.Y_POS += shiftedY;
          }
          x = getMouseCoordinateX();
          y = getMouseCoordinateY();
        } else if (mouseButtonDownAlready) {
          mouseButtonDownAlready = false;
        }
      }
    }
  }

  private int getMouseCoordinateX() {
    return Mouse.getX() / scaleFactor;
  }

  private int getMouseCoordinateY() {
    return (Display.getHeight() - Mouse.getY()) / scaleFactor;
  }

  private boolean inMovementBox() {
    return (getMouseCoordinateX() >= BazaarNotifier.X_POS
        && getMouseCoordinateY() >= BazaarNotifier.Y_POS
        && getMouseCoordinateX() <= BazaarNotifier.currentBoundsX
        && getMouseCoordinateY() <= BazaarNotifier.currentBoundsY);
  }
}
