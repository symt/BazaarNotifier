package dev.meyi.bn.modules;

import dev.meyi.bn.config.ModuleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public abstract class Module {


  protected int x;
  protected int y;
  protected float scale;
  int lastMouseX, lastMouseY;
  int boundsX, boundsY;
  int padding = 3;
  int shift = 0;
  int mouseWheelShift = 0;
  boolean moving = false;
  boolean active = true;
  boolean needsToMove = false;

  public Module() {
    x = 0;
    y = 0;
    scale = 1;
  }

  public Module(ModuleConfig module) {
    x = module.x;
    y = module.y;
    scale = module.scale;
    active = module.active;
  }

  protected abstract void draw();

  protected abstract void reset();

  protected abstract String name();

  protected abstract boolean shouldDrawBounds();

  protected abstract int getMaxShift();

  public void handleMovement() {
    if (moving) {
      x += getMouseCoordinateX() - lastMouseX;
      y += getMouseCoordinateY() - lastMouseY;
    }
    moving = true;
    lastMouseX = getMouseCoordinateX();
    lastMouseY = getMouseCoordinateY();
  }

  public void drawBounds() {
    if (shouldDrawBounds()) {
      Gui.drawRect(x - padding, y - padding, boundsX + padding, boundsY + padding, 0x66000000);
    }
  }

  public boolean inMovementBox() {
    return (getMouseCoordinateX() >= x
        && getMouseCoordinateY() >= y
        && getMouseCoordinateX() <= boundsX
        && getMouseCoordinateY() <= boundsY);
  }

  public ModuleConfig generateModuleConfig() {
    return new ModuleConfig(name(),x,y,scale,active);
  }
  public ModuleConfig generateDefaultConfig(){
    return new ModuleConfig(name(), 10,10,1,true);
  }

  protected int getMouseCoordinateX() {
    return Mouse.getX() / new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
  }

  protected int getMouseCoordinateY() {
    return (Display.getHeight() - Mouse.getY()) / new ScaledResolution(Minecraft.getMinecraft())
        .getScaleFactor();
  }

}
