package dev.meyi.bn.gui;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.utilities.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatComponentText;

import java.io.IOException;

public class SettingsGui extends GuiScreen {
    GuiTextField apiKey;


    @Override
    public void initGui() {
        super.initGui();
        int buttonId = 0;
        for (Module m: BazaarNotifier.modules) {
            buttonList.add(new GuiButton(buttonId, getButtonX(buttonId) , getButtonY(buttonId), m.getReadableName()));
            buttonId++;
        }
        buttonList.add(new GuiButton(buttonId, getButtonX(buttonId),getButtonY(buttonId),
                BazaarNotifier.activeBazaar ? "Mod active" : "Mod disabled"));
        buttonId++;
        buttonList.add(new GuiButton(buttonId, getButtonX(buttonId), getButtonY(buttonId),
                BazaarNotifier.config.showChatMessages? "Chat messages enabled" : "Chat messages disabled"));
        buttonId++;
        apiKey = new GuiTextField(buttonId, fontRendererObj, getButtonX(buttonId), getButtonY(buttonId), 200, 20);
        apiKey.setFocused(true);
        apiKey.setCanLoseFocus(false);
        apiKey.setText(BazaarNotifier.config.api);
    }

    @Override
    protected void actionPerformed(GuiButton Button) throws IOException {
        if (Button.id < BazaarNotifier.modules.size()) {
            BazaarNotifier.guiToOpen = "module"+ Button.id;
        }else if (Button.id ==  BazaarNotifier.modules.size()){
            BazaarNotifier.activeBazaar ^= true;
            Button.displayString = BazaarNotifier.activeBazaar ? "Mod active" : "Mod disabled";
        }else if (Button.id ==  BazaarNotifier.modules.size()+1){
            BazaarNotifier.config.showChatMessages ^= true;
            Button.displayString = BazaarNotifier.config.showChatMessages? "Chat messages enabled" : "Chat messages disabled";
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
            if(key.equals("")){
                return;
            }
            if(Utils.validateApiKey(key)){
                BazaarNotifier.config.api = key;
            }else{
                Minecraft.getMinecraft().thePlayer.addChatMessage(
                        new ChatComponentText("Your API-Key was not saved because it was invalid"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ignored){
            //this only happens if the GUI is closed during initialising
        }
    }

    public int getButtonX(int id){
        if(id%2 == 0){
            //left side
            return width/2 - 205;
        }else{
           //right side
            return width/2 + 5;
        }
    }
    public int getButtonY(int id){
        if(id%2 == 0){
            //left side
            return (int)Math.round(height / Math.E - 50 + 25*id/2f);
        }else{
            //right side
            return (int)Math.round(height / Math.E -50 + 25*(id-1)/2f);
        }
    }
}
