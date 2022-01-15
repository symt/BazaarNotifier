package dev.meyi.bn.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleName;


public class Configuration {

  public static boolean collectionCheckDisabled;
  public static int craftingSortingOption;
  public static int craftingListLength;
  public static boolean showInstantSellProfit;
  public static boolean showSellOfferProfit;
  public static boolean showProfitPerMil;
  public static int suggestionListLength;

  public static JsonObject initializeConfig() {
    JsonObject newConfig = new JsonObject();
    newConfig.addProperty("api", BazaarNotifier.apiKey);
    newConfig.addProperty("version", BazaarNotifier.VERSION);
    newConfig.addProperty("craftingListLength", craftingListLength);
    newConfig.addProperty("suggestionListLength", suggestionListLength);
    newConfig.addProperty("craftingSortingOption", craftingSortingOption);
    newConfig.addProperty("showInstantSellProfit", showInstantSellProfit);
    newConfig.addProperty("showSellOfferProfit", showSellOfferProfit);
    newConfig.addProperty("showProfitPerMil", showProfitPerMil);
    newConfig.addProperty("collectionChecking", collectionCheckDisabled);

    JsonArray modules = new JsonArray();
    JsonObject g = new JsonObject();
    g.addProperty("hi", "hi");
    for (ModuleName value : ModuleName.values()) {
      Module m = value.returnDefaultModule();
      if (m != null) {
        modules.add(m.generateModuleConfig());
      }
    }

    BazaarNotifier.validApiKey = false;
    newConfig.add("modules", modules);

    return newConfig;
  }
}
