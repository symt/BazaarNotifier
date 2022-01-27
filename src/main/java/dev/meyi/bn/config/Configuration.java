package dev.meyi.bn.config;

import com.google.gson.Gson;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.utilities.Defaults;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Configuration {

  public boolean collectionCheckDisabled;
  public int craftingSortingOption;
  public int craftingListLength;
  public boolean showInstantSellProfit;
  public boolean showSellOfferProfit;
  public boolean showProfitPerMil;
  public int suggestionListLength;
  public boolean showChatMessages;
  public String api;
  public String version;
  public ModuleConfig[] modules;



  public Configuration(boolean collectionCheckDisabled, int craftingSortingOption, int craftingListLength,
                       boolean showInstantSellProfit,boolean showSellOfferProfit, boolean showProfitPerMil,
                       int suggestionListLength,boolean showChatMessages, ModuleConfig[] modules){
    this.collectionCheckDisabled = collectionCheckDisabled;
    this.craftingSortingOption = craftingSortingOption;
    this.craftingListLength = craftingListLength;
    this.showInstantSellProfit = showInstantSellProfit;
    this.showSellOfferProfit = showSellOfferProfit;
    this.showProfitPerMil = showProfitPerMil;
    this.suggestionListLength = suggestionListLength;
    this.api = BazaarNotifier.apiKey;
    this.version = BazaarNotifier.VERSION;
    this.modules = modules;
    this.showChatMessages = showChatMessages;
  }




  public void saveConfig(File file, Configuration config) {
    Gson gson = new Gson();
    modules = BazaarNotifier.modules.generateConfig();
    try {
    if (!file.isFile()) {
      file.createNewFile();
    }
      Files.write(Paths.get(file.getAbsolutePath()),
              gson.toJson(config).getBytes(StandardCharsets.UTF_8));
    }catch (Exception ignored){
      System.out.println("Error while saving config file");
    }
  }

  public static Configuration createDefaultConfig() {
    ModuleConfig[] c = new ModuleConfig[4];
    for (int i = 0; i < ModuleName.values().length; i++) {
      Module m = ModuleName.values()[i].returnDefaultModule();
      if (m != null) {
        c[i] = (m.generateModuleConfig());
      }
    }
    return new Configuration(Defaults.COLLECTION_CHECKING,
            Defaults.CRAFTING_SORTING_OPTION ,Defaults.CRAFTING_LIST_LENGTH,Defaults.INSTANT_SELL_PROFIT, Defaults.SELL_OFFER_PROFIT,
            Defaults.PROFIT_PER_MIL,Defaults.SUGGESTION_LIST_LENGTH,Defaults.SEND_CHAT_MESSAGES,c);
  }

}
