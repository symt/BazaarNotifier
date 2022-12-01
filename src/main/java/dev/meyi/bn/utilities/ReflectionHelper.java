package dev.meyi.bn.utilities;

import java.lang.reflect.Field;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;

public class ReflectionHelper {
  private static Field lowerChestInventory;
  public static void setup() {
    try {
      lowerChestInventory = GuiChest.class.getDeclaredField("field_147015_w");
      lowerChestInventory.setAccessible(true);
    } catch (NoSuchFieldException e) {
      lowerChestInventory = null;
      e.printStackTrace();
    }
  }

  public static IInventory getLowerChestInventory(GuiChest g) {
    try {
      if (lowerChestInventory != null) {
        return (IInventory)lowerChestInventory.get(g);
      }
    } catch (IllegalAccessException ignored) {}
    return null;
  }
}
