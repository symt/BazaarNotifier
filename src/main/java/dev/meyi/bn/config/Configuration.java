package dev.meyi.bn.config;

import com.google.gson.Gson;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.utilities.Defaults;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Configuration {

  private static final int MODULE_LENGTH = 4;

  public boolean collectionCheckDisabled;
  public int craftingSortingOption;
  public int craftingListLength;

  private boolean showInstantSellProfit;
  private boolean showSellOfferProfit;
  private boolean showProfitPerMil;
  public int suggestionListLength;
  public boolean showChatMessages;
  public boolean useBuyOrders;
  public String api = "";
  public String version;
  public ModuleConfig[] modules;

  public Configuration(boolean collectionCheckDisabled, int craftingSortingOption,
      int craftingListLength,
      boolean showInstantSellProfit, boolean showSellOfferProfit, boolean showProfitPerMil,
      int suggestionListLength, boolean showChatMessages, String apiKey,boolean useBuyOrders, ModuleConfig[] modules) {
    this.collectionCheckDisabled = collectionCheckDisabled;
    this.craftingSortingOption = craftingSortingOption;
    this.craftingListLength = craftingListLength;
    this.showInstantSellProfit = showInstantSellProfit;
    this.showSellOfferProfit = showSellOfferProfit;
    this.showProfitPerMil = showProfitPerMil;
    this.suggestionListLength = suggestionListLength;
    this.api =
        apiKey == null ? "" : apiKey; // It is fixed in createDefaultConfig, but redundancies.
    this.version = BazaarNotifier.VERSION;
    this.modules = modules;
    this.showChatMessages = showChatMessages;
    this.useBuyOrders = useBuyOrders;
  }


  public static void saveConfig(File file, Configuration config) {
    Gson gson = new Gson();
    BazaarNotifier.config.modules = BazaarNotifier.modules.generateConfig();
    try {
      if (!file.isFile()) {
        file.createNewFile();
      }
      Files.write(Paths.get(file.getAbsolutePath()),
          gson.toJson(config).getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Configuration createDefaultConfig() {
    ModuleConfig[] c = new ModuleConfig[MODULE_LENGTH];
    // TODO: Change this to enhanced-for loop so we don't have to call .values every single time
    for (int i = 0; i < ModuleName.values().length; i++) {
      // TODO: Change this to use static methods so we aren't unnecessarily creating Module objects
      Module m = ModuleName.values()[i].returnDefaultModule();
      c[i] = m.generateDefaultConfig();
    }
    return new Configuration(Defaults.COLLECTION_CHECKING,
        Defaults.CRAFTING_SORTING_OPTION, Defaults.CRAFTING_LIST_LENGTH,
        Defaults.INSTANT_SELL_PROFIT, Defaults.SELL_OFFER_PROFIT,
        Defaults.PROFIT_PER_MIL, Defaults.SUGGESTION_LIST_LENGTH, Defaults.SEND_CHAT_MESSAGES, "",
        Defaults.USE_BUY_ORDERS, c);
  }

  public void setShowInstantSellProfit(boolean showInstantSellProfit) {
    this.showInstantSellProfit = showInstantSellProfit;
    if(checkIfDisabled()){
      this.showInstantSellProfit = true;
    }
  }

  public boolean isShowSellOfferProfit() {
    return showSellOfferProfit;
  }

  public void setShowSellOfferProfit(boolean showSellOfferProfit) {
    this.showSellOfferProfit = showSellOfferProfit;
    if(checkIfDisabled()){
      this.showSellOfferProfit = true;
    }
  }

  public boolean isShowProfitPerMil() {
    return showProfitPerMil;
  }

  public void setShowProfitPerMil(boolean showProfitPerMil) {
    this.showProfitPerMil = showProfitPerMil;
    if(checkIfDisabled()){
      this.showProfitPerMil = true;
    }
  }
  public boolean isShowInstantSellProfit() {
    return showInstantSellProfit;
  }

  private boolean checkIfDisabled(){
    return !showProfitPerMil && !showSellOfferProfit && !showInstantSellProfit;
  }
}
