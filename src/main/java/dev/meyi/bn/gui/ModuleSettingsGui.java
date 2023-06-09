package dev.meyi.bn.gui;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.calc.BankCalculator;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.config.GuiSlider;

public class ModuleSettingsGui extends GuiScreen {

  Module module;
  GuiSlider scaleSlider;
  GuiSlider lengthSlider;

  public ModuleSettingsGui(Module module) {
    this.module = module;
  }

  @Override
  public void initGui() {
    super.initGui();
    buttonList.add(new GuiButton(ButtonIds.IS_ACTIVE.id, getButtonX(),
        getButtonY(), "Module: " + SettingsGui.getOnOff(module.isEnabled())));
    buttonList.add(scaleSlider = new GuiSlider(ButtonIds.SCALE_SLIDER.id, getButtonX(),
        getButtonY(), 200, 20, "Scale: ", "", 1,
        30, module.getScale() * 10, false, true));
    switch (module.name()) {
      case "SUGGESTION":
        buttonList.add(lengthSlider = new GuiSlider(ButtonIds.ENTRIES_SLIDER.id, getButtonX(),
            getButtonY(), 200, 20, "Entries: ", "", 1,
            100, BazaarNotifier.config.suggestionModule.suggestionListLength, false, true));
        buttonList.add(new GuiButton(ButtonIds.USE_ENCHANTMENTS.id, getButtonX(), getButtonY(),
            "Show Enchantments: " + SettingsGui.getOnOff(BazaarNotifier.config.suggestionModule.suggestionShowEnchantments)));
        break;
      case "CRAFTING":
        buttonList.add(lengthSlider = new GuiSlider(ButtonIds.ENTRIES_SLIDER.id, getButtonX(),
            getButtonY(), 200, 20, "Entries: ", "", 1,
            100, BazaarNotifier.config.craftingModule.craftingListLength, false, true));
        buttonList.add(new GuiButton(ButtonIds.COLLECTION_CHECK.id, getButtonX(),
            getButtonY(), "Collection Check: " +
            SettingsGui.getOnOff(BazaarNotifier.config.collectionCheck)));
        if ("".equals(BazaarNotifier.config.api)) {
          buttonList.get(3).enabled = false;
        }
        buttonList.add(new GuiButton(ButtonIds.SELLING_OPTION.id, getButtonX(),
            getButtonY(), "Sort By: " + (BazaarNotifier.config.craftingModule.craftingSortingOption == 0 ?
            "Instant Sell" : BazaarNotifier.config.craftingModule.craftingSortingOption == 1 ? "Sell Offer"
            : "Profit Per Million")));
        buttonList.add(new GuiButton(ButtonIds.INSTANT_SELL_PROFIT.id, getButtonX(),
            getButtonY(), "Instant Sell Profit: " +
            SettingsGui.getOnOff(BazaarNotifier.config.craftingModule.showInstantSellProfit)));
        buttonList.add(new GuiButton(ButtonIds.SELL_OFFER_PROFIT.id, getButtonX(),
            getButtonY(), "Sell Offer Profit: " +
            SettingsGui.getOnOff(BazaarNotifier.config.craftingModule.showSellOfferProfit)));
        buttonList.add(new GuiButton(ButtonIds.PROFIT_PER_MILLION.id, getButtonX(),
            getButtonY(), "Profit Per Million: " +
            SettingsGui.getOnOff(BazaarNotifier.config.craftingModule.showProfitPerMil)));
        buttonList.add(new GuiButton(ButtonIds.MATERIAL_BUYING_OPTION.id, getButtonX(),
            getButtonY(), "Materials: " +
            (BazaarNotifier.config.craftingModule.useBuyOrders ? "Buy Order" : "Instant Buy")));

        break;
      case "BANK":
        buttonList.add(new GuiButton(ButtonIds.BANK_RAW_DIFFERENCE.id, getButtonX(), getButtonY(), "Raw Difference: " +
            SettingsGui.getOnOff(BazaarNotifier.config.bankModule.bankRawDifference)));
        buttonList.add(new GuiButton(ButtonIds.RESET.id, getButtonX(), getButtonY(), "Reset"));
        break;
    }

    buttonList.add(new GuiButton(ButtonIds.BACK.id, 10, height - 25, "Back"));
  }

  @Override
  protected void actionPerformed(GuiButton Button) {
    if (Button.id == ButtonIds.IS_ACTIVE.id) {
      module.setActive(!module.isEnabled());
      Button.displayString = "Module: " + SettingsGui.getOnOff(module.isEnabled());
    } else if (Button.id == ButtonIds.SCALE_SLIDER.id) {
      module.setScale((float) scaleSlider.getValue() / 10, false);
    } else if (Button.id == ButtonIds.ENTRIES_SLIDER.id) {
      if (module.name().equals("SUGGESTION")) {
        BazaarNotifier.config.suggestionModule.suggestionListLength = (int) Math.round(lengthSlider.getValue());
      } else {
        BazaarNotifier.config.craftingModule.craftingListLength = (int) Math.round(lengthSlider.getValue());
      }
    } else if (Button.id == ButtonIds.COLLECTION_CHECK.id) {
      BazaarNotifier.config.collectionCheck ^= true;
      Button.displayString =
          "Collection Check: " + SettingsGui.getOnOff(BazaarNotifier.config.collectionCheck);

      if (BazaarNotifier.config.collectionCheck) {
        new Thread(() -> {
          CraftingCalculator.getUnlockedRecipes();
          Button.displayString =
              "Collection Check: " + SettingsGui.getOnOff(
                  BazaarNotifier.config.collectionCheck);
          if (!BazaarNotifier.config.collectionCheck) {
            Minecraft.getMinecraft().thePlayer
                .addChatMessage(new ChatComponentText(BazaarNotifier.prefix +
                    EnumChatFormatting.RED
                    + "There was an error while enabling the collections check. " +
                    "Make sure your Collections API is enabled. Try again in a few minutes."));
          }
        }).start();
      }
    } else if (Button.id == ButtonIds.SELLING_OPTION.id) {
      CraftingCalculator.toggleCrafting();
      Button.displayString =
          "Sort By: " + (BazaarNotifier.config.craftingModule.craftingSortingOption == 0 ? "Instant Sell" :
              BazaarNotifier.config.craftingModule.craftingSortingOption == 1 ? "Sell Offer"
                  : "Profit Per Million");
    } else if (Button.id == ButtonIds.INSTANT_SELL_PROFIT.id) {
      BazaarNotifier.config.craftingModule.showInstantSellProfit ^= true;
      Button.displayString =
          "Instant Sell Profit: " + SettingsGui.getOnOff(
              BazaarNotifier.config.craftingModule.showInstantSellProfit);
    } else if (Button.id == ButtonIds.SELL_OFFER_PROFIT.id) {
      BazaarNotifier.config.craftingModule.showSellOfferProfit ^= true;
      Button.displayString =
          "Sell Offer Profit: " + SettingsGui.getOnOff(BazaarNotifier.config.craftingModule.showSellOfferProfit);
    } else if (Button.id == ButtonIds.PROFIT_PER_MILLION.id) {
      BazaarNotifier.config.craftingModule.showProfitPerMil^=true;
      Button.displayString =
          "Profit Per Million: " + SettingsGui.getOnOff(BazaarNotifier.config.craftingModule.showProfitPerMil);
    } else if (Button.id == ButtonIds.MATERIAL_BUYING_OPTION.id) {
      BazaarNotifier.config.craftingModule.useBuyOrders ^= true;
      Button.displayString = "Materials: " + (BazaarNotifier.config.craftingModule.useBuyOrders ?
          "Buy Order" : "Instant Buy");
      CraftingCalculator.getBestEnchantRecipes();
    } else if (Button.id == ButtonIds.RESET.id) {
      BankCalculator.reset();
    } else if (Button.id == ButtonIds.USE_ENCHANTMENTS.id) {
      BazaarNotifier.config.suggestionModule.suggestionShowEnchantments ^= true;
      Button.displayString =
          "Show Enchantments: " + SettingsGui.getOnOff(
              BazaarNotifier.config.suggestionModule.suggestionShowEnchantments);
    } else if (Button.id == ButtonIds.BANK_RAW_DIFFERENCE.id) {
      BazaarNotifier.config.bankModule.bankRawDifference ^= true;
      Button.displayString =
          "Raw Difference: " + SettingsGui.getOnOff(
              BazaarNotifier.config.bankModule.bankRawDifference);
    } else if (Button.id == ButtonIds.BACK.id) {
      BazaarNotifier.guiToOpen = "settings";
    }
  }


  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    this.drawDefaultBackground();
    super.drawScreen(mouseX, mouseY, partialTicks);
    // Action Performed is only called on the first tick when picking up the slider
    if (scaleSlider.getValue() != module.getScale()) {
      module.setScale((float) scaleSlider.getValue() / 10, false);
    }
    if (module.name().equals("SUGGESTION")) {
      if (lengthSlider.getValue() != BazaarNotifier.config.suggestionModule.suggestionListLength) {
        BazaarNotifier.config.suggestionModule.suggestionListLength = (int) Math.round(lengthSlider.getValue());
      }
    } else if (module.name().equals("CRAFTING")) {
      if (lengthSlider.getValue() != BazaarNotifier.config.craftingModule.craftingListLength) {
        BazaarNotifier.config.craftingModule.craftingListLength = (int) Math.round(lengthSlider.getValue());
      }
    }
  }

  public int getButtonX() {
    return SettingsGui.getButtonX(buttonList.size(), width);
  }

  public int getButtonY() {
    return SettingsGui.getButtonY(buttonList.size(), height);
  }

  private enum ButtonIds {
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
    USE_ENCHANTMENTS(10),
    BANK_RAW_DIFFERENCE(11),

    BACK(100);

    final int id;

    ButtonIds(int i) {
      this.id = i;
    }
  }
}
