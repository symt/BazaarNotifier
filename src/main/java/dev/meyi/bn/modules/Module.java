package dev.meyi.bn.modules;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.json.JSONObject;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public abstract class Module {


  int lastMouseX, lastMouseY;
  protected int x;
  protected int y;
  int boundsX, boundsY;
  int padding = 3;
  int shift = 0;

  boolean moving = false;
  boolean needsToMove = false;

  public Module() {
    reset();
  }

  public Module(JSONObject module) {
    x = module.getInt("x");
    y = module.getInt("y");
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

  public JSONObject generateModuleConfig() {
    JSONObject config = new JSONObject();
    config.put("x", x).put("y", y).put("name", name());
    return config;
  }

  protected int getMouseCoordinateX() {
    return Mouse.getX() / new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
  }

  protected int getMouseCoordinateY() {
    return (Display.getHeight() - Mouse.getY()) / new ScaledResolution(Minecraft.getMinecraft())
        .getScaleFactor();
  }

}
