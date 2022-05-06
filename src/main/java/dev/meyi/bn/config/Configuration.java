package dev.meyi.bn.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
  public boolean showInstantSellProfit;
  public boolean showSellOfferProfit;
  public boolean showProfitPerMil;
  public int suggestionListLength;
  public boolean showChatMessages;
  public String api;
  public String version;
  public ModuleConfig[] modules;
  public JsonObject resources;



  public Configuration(boolean collectionCheckDisabled, int craftingSortingOption, int craftingListLength,
                       boolean showInstantSellProfit,boolean showSellOfferProfit, boolean showProfitPerMil,
                       int suggestionListLength,boolean showChatMessages,String APIKey, ModuleConfig[] modules){
    this.collectionCheckDisabled = collectionCheckDisabled;
    this.craftingSortingOption = craftingSortingOption;
    this.craftingListLength = craftingListLength;
    this.showInstantSellProfit = showInstantSellProfit;
    this.showSellOfferProfit = showSellOfferProfit;
    this.showProfitPerMil = showProfitPerMil;
    this.suggestionListLength = suggestionListLength;
    this.api = APIKey;
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
    }catch (IOException e){
      e.printStackTrace();
    }
  }

  public static Configuration createDefaultConfig() {
    ModuleConfig[] c = new ModuleConfig[MODULE_LENGTH];
    for (int i = 0; i < ModuleName.values().length; i++) {
      Module m = ModuleName.values()[i].returnDefaultModule();
      c[i] = m.generateDefaultConfig();
    }
    return new Configuration(Defaults.COLLECTION_CHECKING,
            Defaults.CRAFTING_SORTING_OPTION ,Defaults.CRAFTING_LIST_LENGTH,Defaults.INSTANT_SELL_PROFIT, Defaults.SELL_OFFER_PROFIT,
            Defaults.PROFIT_PER_MIL,Defaults.SUGGESTION_LIST_LENGTH,Defaults.SEND_CHAT_MESSAGES,"",c);
  }

}
