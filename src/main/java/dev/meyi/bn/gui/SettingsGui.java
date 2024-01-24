package dev.meyi.bn.gui;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.utilities.Utils;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class SettingsGui extends GuiScreen {

  @Override
  public void initGui() {
    super.initGui();
    int buttonId = 0;
    for (Module m : BazaarNotifier.modules) {
      buttonList.add(
          new GuiButton(buttonId, getButtonX(buttonId), getButtonY(buttonId), m.getReadableName()));
      buttonId++;
    }
    buttonList.add(new GuiButton(buttonId, getButtonX(buttonId), getButtonY(buttonId),
        "Mod: " + SettingsGui.getOnOff(BazaarNotifier.activeBazaar)));
    buttonId++;
    buttonList.add(new GuiButton(buttonId, getButtonX(buttonId), getButtonY(buttonId),
        "Chat Messages: " + SettingsGui.getOnOff(BazaarNotifier.config.showChatMessages)));
  }

  @Override
  protected void actionPerformed(GuiButton Button) {
    if (Button.id < BazaarNotifier.modules.size()) {
      BazaarNotifier.guiToOpen = "module" + Button.id;
    } else if (Button.id == BazaarNotifier.modules.size()) {
      BazaarNotifier.activeBazaar ^= true;
      Button.displayString = "Mod: " + getOnOff(BazaarNotifier.activeBazaar);
    } else if (Button.id == BazaarNotifier.modules.size() + 1) {
      BazaarNotifier.config.showChatMessages ^= true;
      Button.displayString = "Chat Messages: " + getOnOff(BazaarNotifier.config.showChatMessages);
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    this.drawDefaultBackground();
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  public int getButtonX(int id) {
    return SettingsGui.getButtonX(id, width);
  }

  public int getButtonY(int id) {
    return SettingsGui.getButtonY(id, height);
  }

  public static String getOnOff(boolean b) {
    return b ? "ON" : "OFF";
  }

  public static int getButtonX(int id, int width) {
    if (id % 2 == 0) {
      // left side
      return width / 2 - 205;
    } else {
      // right side
      return width / 2 + 5;
    }
  }

  public static int getButtonY(int id, int height) {
    if (id % 2 == 0) {
      // left side
      return (int) Math.round(height / Math.E - 50 + 25 * id / 2f);
    } else {
      // right side
      return (int) Math.round(height / Math.E - 50 + 25 * (id - 1) / 2f);
    }
  }
}
