package dev.meyi.bn.gui;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;


import java.io.IOException;

public class ModuleSettingsGui extends GuiScreen {
    Module module;
    GuiSlider scaleSlider;
    GuiSlider lengthSlider;

    public ModuleSettingsGui(Module module){
        this.module = module;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(0, getButtonX(0), getButtonY(0), module.active? "Module active" : "Module disabled"));
        buttonList.add(scaleSlider = new GuiSlider(1, getButtonX(1), getButtonY(1), 200, 20, "Scale: ", "", 1, 30, module.scale*10, false,true));
        if(module.getName().equals("SUGGESTION")) {
            buttonList.add(lengthSlider = new GuiSlider(2, getButtonX(2), getButtonY(2), 200, 20, "Entries: ", "", 1, 100, BazaarNotifier.config.suggestionListLength, false, true));
        }else if ( module.getName().equals("CRAFTING")){
            buttonList.add(lengthSlider = new GuiSlider(2, getButtonX(2), getButtonY(2), 200, 20, "Entries: ", "", 1, 100, BazaarNotifier.config.craftingListLength, false, true));
            buttonList.add(new GuiButton(3,getButtonX(3),getButtonY(3),BazaarNotifier.config.collectionCheckDisabled ? "Collection checks disabled" : "Collection checks enabled"));
            if(!BazaarNotifier.validApiKey) {
                buttonList.get(3).enabled = false;
            }
            buttonList.add(new GuiButton(4, getButtonX(4), getButtonY(4), BazaarNotifier.config.craftingSortingOption == 0 ?
                    "Instant sell" : BazaarNotifier.config.craftingSortingOption == 1 ? "Sell offer":"Profit per million" ));
            buttonList.add(new GuiButton(5, getButtonX(5), getButtonY(5), BazaarNotifier.config.showInstantSellProfit ?
                    "Showing instant sell profit" : "Hiding instant sell profit"));
            buttonList.add(new GuiButton(6, getButtonX(6), getButtonY(6), BazaarNotifier.config.showSellOfferProfit ?
                    "Showing sell offer profit" : "Hiding sell offer profit"));
            buttonList.add(new GuiButton(7, getButtonX(7), getButtonY(7), BazaarNotifier.config.showProfitPerMil ?
                    "Showing profit per million" : "Hiding profit per million"));
            buttonList.add(new GuiButton(8, getButtonX(8), getButtonY(8), BazaarNotifier.config.useBuyOrders ?
                    "Buy order materials" : "Instant buy materials"));

        }

        buttonList. add(new GuiButton(100, 10, height-25, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton Button) throws IOException {
        switch (Button.id){
            case 0: module.active ^= true;
                Button.displayString = module.active? "Module active" : "Module disabled";
                break;
            case 1: module.scale = (float) scaleSlider.getValue()/10;
                break;
            case 2: if(module.getName().equals("SUGGESTION")) {
                        BazaarNotifier.config.suggestionListLength = (int) Math.round(lengthSlider.getValue());
                    }else{
                        BazaarNotifier.config.craftingListLength = (int) Math.round(lengthSlider.getValue());
                    }
                    break;
            case 3: BazaarNotifier.config.collectionCheckDisabled ^= true;
                    Button.displayString = BazaarNotifier.config.collectionCheckDisabled ? "Collection checks disabled" :
                            "Collection checks enabled";
                    break;
            case 4: CraftingCalculator.toggleCrafting();
                    Button.displayString = BazaarNotifier.config.craftingSortingOption == 0 ? "Instant sell" :
                            BazaarNotifier.config.craftingSortingOption == 1 ? "Sell offer":"Profit per million";
                    break;
            case 5: BazaarNotifier.config.showInstantSellProfit ^= true;
                    Button.displayString = BazaarNotifier.config.showInstantSellProfit ? "Showing instant sell profit" :
                            "Hiding instant sell profit";
                    break;
            case 6: BazaarNotifier.config.showSellOfferProfit ^= true;
                    Button.displayString = BazaarNotifier.config.showSellOfferProfit ? "Showing sell offer profit" :
                            "Hiding sell offer profit";
                    break;
            case 7: BazaarNotifier.config.showProfitPerMil ^= true;
                    Button.displayString = BazaarNotifier.config.showProfitPerMil ? "Showing profit per million" :
                            "Hiding profit per million";
                    break;
            case 8: BazaarNotifier.config.useBuyOrders ^= true;
                    Button.displayString = BazaarNotifier.config.useBuyOrders ?
                            "Buy order materials" : "Instant buy materials";
                    break;
            case 100: BazaarNotifier.guiToOpen = "settings";
                    break;
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(module.getName().equals("SUGGESTION")) {
            BazaarNotifier.config.suggestionListLength = (int) lengthSlider.getValue();
        }else if (module.getName().equals("CRAFTING")){
            BazaarNotifier.config.craftingListLength = (int) lengthSlider.getValue();
        }
        //action Performed is only called on the first tick when picking up the slider
        if (scaleSlider.getValue() != module.scale){
            module.scale = (float) scaleSlider.getValue()/10;
        }
        if(module.getName().equals("SUGGESTION")) {
            if(lengthSlider.getValue() != BazaarNotifier.config.suggestionListLength) {
                BazaarNotifier.config.suggestionListLength = (int) Math.round(lengthSlider.getValue());
            }
        }else if(module.getName().equals("CRAFTING")){
            if(lengthSlider.getValue() != BazaarNotifier.config.craftingListLength) {
                BazaarNotifier.config.craftingListLength = (int) Math.round(lengthSlider.getValue());
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
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
