package dev.meyi.bn.modules;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.platform.GuiPlatform;
import cc.polyfrost.oneconfig.platform.Platform;
import dev.meyi.bn.BazaarNotifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;


public abstract class Module extends Hud {
  protected transient int shift = 0;
  protected transient boolean moving = false;
  protected transient boolean needsToMove = false;
  protected transient int mouseWheelShift = 0;
  protected transient int padding = 3;
  private transient int lastMouseX, lastMouseY;

  protected transient String longestString = "";



  @Switch(name="Show Module Outside of Bazaar",
  category = "General"
  )
  protected boolean showEverywhere = false;

  public Module() {
    super(true, 20, 20, 2, 1);
  }

  @Override
  protected boolean shouldShow() {
    if(enabled) {
      GuiPlatform guiPlatform = Platform.getGuiPlatform();
      if(BazaarNotifier.inBazaar){
        //Drawing at this case is handled by the ChestTickHandler to keep it above the gray gui background;
        return false;
      }
      if (showInGuis && (guiPlatform.getCurrentScreen() instanceof OneConfigGui))  return false;
      if (showInChat && guiPlatform.isInChat()) return true;
      if (showInDebug && guiPlatform.isInDebug()) return true;
      if (showInGuis && guiPlatform.getCurrentScreen() != null) return true;
      return showEverywhere;
    }else {
      return false;
    }
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
      Gui.drawRect((int)position.getX(), (int)position.getY(),
              (int)position.getRightX(), (int)position.getBottomY(), 0x66000000);
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
    enabled = active;
  }

  public float getModuleWidth(){
    return this.getWidth(scale, false);
  }

  public float getModuleHeight(){
    return this.getHeight(scale, false);  }
}
