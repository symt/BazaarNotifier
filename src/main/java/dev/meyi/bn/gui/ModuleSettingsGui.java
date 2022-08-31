package dev.meyi.bn.gui;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.calc.BankCalculator;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

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
        buttonList.add(new GuiButton(ButtonIds.IS_ACTIVE.id, getButtonX(),
                getButtonY(), "Module: " + getOnOff(module.isActive())));
        buttonList.add(scaleSlider = new GuiSlider(ButtonIds.SCALE_SLIDER.id, getButtonX(),
                getButtonY(), 200, 20, "Scale: ", "", 1,
                30, module.getScale()*10, false,true));
        if(module.getName().equals("SUGGESTION")) {
            buttonList.add(lengthSlider = new GuiSlider(ButtonIds.ENTRIES_SLIDER.id, getButtonX(),
                    getButtonY(), 200, 20, "Entries: ", "", 1,
                    100, BazaarNotifier.config.suggestionListLength, false, true));
        }else if (module.getName().equals("CRAFTING")){
            buttonList.add(lengthSlider = new GuiSlider(ButtonIds.ENTRIES_SLIDER.id, getButtonX(),
                    getButtonY(), 200, 20, "Entries: ", "", 1,
                    100, BazaarNotifier.config.craftingListLength, false, true));
            buttonList.add(new GuiButton(ButtonIds.COLLECTION_CHECK.id,getButtonX(),
                    getButtonY(),"Collection Check: " +
                    getOnOff(BazaarNotifier.config.collectionCheckDisabled)));
            if(!BazaarNotifier.validApiKey) {
                buttonList.get(3).enabled = false;
            }
            buttonList.add(new GuiButton(ButtonIds.SELLING_OPTION.id, getButtonX(),
                    getButtonY(), "Sort By: " + (BazaarNotifier.config.craftingSortingOption == 0 ?
                    "Instant Sell" : BazaarNotifier.config.craftingSortingOption == 1 ? "Sell Offer":"Profit Per Million" )));
            buttonList.add(new GuiButton(ButtonIds.INSTANT_SELL_PROFIT.id, getButtonX(),
                    getButtonY(), "Instant Sell Profit: " +
                    getOnOff(BazaarNotifier.config.isShowInstantSellProfit())));
            buttonList.add(new GuiButton(ButtonIds.SELL_OFFER_PROFIT.id, getButtonX(),
                    getButtonY(),"Sell Offer Profit: " +
                    getOnOff(BazaarNotifier.config.isShowSellOfferProfit())));
            buttonList.add(new GuiButton(ButtonIds.PROFIT_PER_MILLION.id, getButtonX(),
                    getButtonY(), "Profit Per Million: " +
                    getOnOff(BazaarNotifier.config.isShowProfitPerMil())));
            buttonList.add(new GuiButton(ButtonIds.MATERIAL_BUYING_OPTION.id, getButtonX(),
                    getButtonY(), "Materials: " +
                    (BazaarNotifier.config.useBuyOrders? "Buy Order" : "Instant Buy")));

        }else if (module.getName().equals("BANK")){
            buttonList.add(new GuiButton(ButtonIds.RESET.id,getButtonX(),getButtonY(),"Reset"));
        }

        buttonList. add(new GuiButton(ButtonIds.BACK.id, 10, height-25, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton Button){
        if(Button.id == ButtonIds.IS_ACTIVE.id){
            module.setActive(!module.isActive());
            Button.displayString = "Module: " + getOnOff(module.isActive());
        } else if (Button.id == ButtonIds.SCALE_SLIDER.id) {
            module.setScale((float) scaleSlider.getValue()/10);
        } else if (Button.id == ButtonIds.ENTRIES_SLIDER.id) {
            if(module.getName().equals("SUGGESTION")) {
                BazaarNotifier.config.suggestionListLength = (int) Math.round(lengthSlider.getValue());
            }else{
                BazaarNotifier.config.craftingListLength = (int) Math.round(lengthSlider.getValue());
            }
        } else if (Button.id == ButtonIds.COLLECTION_CHECK.id) {
            BazaarNotifier.config.collectionCheckDisabled ^= true;
            Button.displayString = "Collection Check: " + getOnOff(BazaarNotifier.config.collectionCheckDisabled);
        } else if (Button.id == ButtonIds.SELLING_OPTION.id) {
            CraftingCalculator.toggleCrafting();
            Button.displayString ="Sort By: " + (BazaarNotifier.config.craftingSortingOption == 0 ? "Instant Sell" :
                    BazaarNotifier.config.craftingSortingOption == 1 ? "Sell Offer":"Profit Per Million");
        } else if (Button.id == ButtonIds.INSTANT_SELL_PROFIT.id) {
            BazaarNotifier.config.setShowInstantSellProfit(!BazaarNotifier.config.isShowInstantSellProfit());
            Button.displayString = "Instant Sell Profit: " + getOnOff(BazaarNotifier.config.isShowInstantSellProfit());
        } else if (Button.id == ButtonIds.SELL_OFFER_PROFIT.id) {
            BazaarNotifier.config.setShowSellOfferProfit(!BazaarNotifier.config.isShowSellOfferProfit());
            Button.displayString = "Sell Offer Profit: " + getOnOff(BazaarNotifier.config.isShowSellOfferProfit());
        } else if (Button.id == ButtonIds.PROFIT_PER_MILLION.id) {
            BazaarNotifier.config.setShowProfitPerMil(!BazaarNotifier.config.isShowProfitPerMil());
            Button.displayString = "Profit Per Million: " + getOnOff(BazaarNotifier.config.isShowProfitPerMil());
        } else if (Button.id == ButtonIds.MATERIAL_BUYING_OPTION.id) {
            BazaarNotifier.config.useBuyOrders ^= true;
            Button.displayString = "Materials: " + (BazaarNotifier.config.useBuyOrders?
                    "Buy Order" : "Instant Buy");
        } else if (Button.id == ButtonIds.RESET.id) {
            BankCalculator.reset();
        } else if (Button.id == ButtonIds.BACK.id){
            BazaarNotifier.guiToOpen = "settings";
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        //action Performed is only called on the first tick when picking up the slider
        if (scaleSlider.getValue() != module.getScale()){
            module.setScale((float) scaleSlider.getValue()/10);
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
    String getOnOff(boolean b){
        return b ? "ON" : "OFF";
    }

    public int getButtonX(){
        int id = buttonList.size();
        if(id%2 == 0){
            //left side
            return width/2 - 205;
        }else{
            //right side
            return width/2 + 5;
        }
    }
    public int getButtonY(){
        int id = buttonList.size();
        if(id%2 == 0){
            //left side
            return (int)Math.round(height / Math.E - 50 + 25*id/2f);
        }else{
            //right side
            return (int)Math.round(height / Math.E -50 + 25*(id-1)/2f);
        }
    }


    enum ButtonIds {
        IS_ACTIVE(0),
        SCALE_SLIDER(1),
        ENTRIES_SLIDER(2),
        COLLECTION_CHECK(3),
        SELLING_OPTION(4),
        INSTANT_SELL_PROFIT(5),
        SELL_OFFER_PROFIT(6),
        PROFIT_PER_MILLION(7),
        MATERIAL_BUYING_OPTION(8),
        RESET(9),

        BACK(100);

        final int id;

        ButtonIds(int i) {
            this.id = i;
        }
    }


}
