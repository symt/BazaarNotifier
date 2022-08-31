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

  GuiTextField apiKey;
  String previousApiKey;


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
        "Mod: " + getOnOff(BazaarNotifier.activeBazaar)));
    buttonId++;
    buttonList.add(new GuiButton(buttonId, getButtonX(buttonId), getButtonY(buttonId),
        "Chat Messages: " + getOnOff(BazaarNotifier.config.showChatMessages)));
    buttonId++;
    apiKey = new GuiTextField(buttonId, fontRendererObj, getButtonX(buttonId), getButtonY(buttonId),
        200, 20);
    apiKey.setMaxStringLength(40);
    apiKey.setFocused(true);
    apiKey.setCanLoseFocus(false);
    apiKey.setText(BazaarNotifier.config.api.equals("") ? "Api key missing" : "Api key set");
    previousApiKey = BazaarNotifier.config.api;
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
    apiKey.drawTextBox();
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    super.keyTyped(typedChar, keyCode);
    apiKey.textboxKeyTyped(typedChar, keyCode);
  }

  @Override
  public void onGuiClosed() {
    super.onGuiClosed();
    try {
      String key = apiKey.getText();
      if (key.equals("") || key.equals(previousApiKey) || key.equals("Api key missing") || key
          .equals("Api key set")) {
        return;
      }
      key = key.replaceAll(" ", "");
      if (Utils.validateApiKey(key)) {
        BazaarNotifier.config.api = key;
        Minecraft.getMinecraft().thePlayer.addChatMessage(
            new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.GREEN + "A new api key has been set."));
      } else {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
            BazaarNotifier.prefix + EnumChatFormatting.RED
                + "Your api key was not saved because it was invalid."));
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (NullPointerException | IllegalArgumentException ignored) {
      //this only happens if the GUI is closed during initialising
    }
  }

  String getOnOff(boolean b) {
    return b ? "ON" : "OFF";
  }

  public int getButtonX(int id) {
    if (id % 2 == 0) {
      //left side
      return width / 2 - 205;
    } else {
      //right side
      return width / 2 + 5;
    }
  }

  public int getButtonY(int id) {
    if (id % 2 == 0) {
      //left side
      return (int) Math.round(height / Math.E - 50 + 25 * id / 2f);
    } else {
      //right side
      return (int) Math.round(height / Math.E - 50 + 25 * (id - 1) / 2f);
    }
  }
}
