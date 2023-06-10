package dev.meyi.bn.modules;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.platform.Platform;
import dev.meyi.bn.BazaarNotifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;


public abstract class Module extends Hud{
  protected int shift = 0;
  protected boolean moving = false;
  protected boolean needsToMove = false;
  protected int mouseWheelShift = 0;
  protected int padding = 3;
  private int lastMouseX, lastMouseY;

  protected transient String longestString = "";



  @Switch(name="Show Modules Outside of Bazaar",
  category = "General"
  )
  protected boolean showEverywhere = false;

  public Module() {
    position.setPosition(20,20);
    setScale(1, false);
  }

  @Override
  protected boolean shouldShow() {
    if (showInGuis && Platform.getGuiPlatform().getCurrentScreen() != null &&
            (Platform.getGuiPlatform().getCurrentScreen() instanceof OneConfigGui))  return false;
    if(BazaarNotifier.inBazaar) return true;
    if(showInChat && Platform.getGuiPlatform().isInChat()) return true;
    if(showInDebug && Platform.getGuiPlatform().isInDebug()) return true;
    if(showInGuis && Platform.getGuiPlatform() != null && Minecraft.getMinecraft().currentScreen != null &&
            !Platform.getGuiPlatform().isInDebug() && !Platform.getGuiPlatform().isInChat()) return true;
    return showEverywhere;
  }

  public abstract void draw();

  protected abstract void reset();

  public abstract String name();

  protected abstract boolean shouldDrawBounds();

  protected abstract int getMaxShift();

  public void handleMovement() {
    if (moving) {
      position.setPosition(position.getX() + getMouseCoordinateX() - lastMouseX, position.getY() + getMouseCoordinateY() - lastMouseY);
    }
    moving = true;
    lastMouseX = getMouseCoordinateX();
    lastMouseY = getMouseCoordinateY();
  }

  public void drawBounds() {
    if (shouldDrawBounds()) {
      Gui.drawRect((int)position.getX() - padding, (int)position.getY() - padding,
              (int)position.getRightX() + padding, (int)position.getBottomY() + padding, 0x66000000);
    }
  }

  public boolean inMovementBox() {
    return (getMouseCoordinateX() >= position.getX()
        && getMouseCoordinateY() >= position.getY()
        && getMouseCoordinateX() <= position.getRightX()
        && getMouseCoordinateY() <= position.getBottomY());
  }


  protected int getMouseCoordinateX() {
    return Mouse.getX() / new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
  }

  protected int getMouseCoordinateY() {
    return (Display.getHeight() - Mouse.getY()) / new ScaledResolution(Minecraft.getMinecraft())
        .getScaleFactor();
  }

  public String getReadableName() {
    String name = StringUtils.lowerCase(name());
    return StringUtils.capitalize(name) + " Module";
  }

  public void setActive(boolean active) {
    System.out.println("Set to disabled" + active);
    enabled = active;
  }
}
