package dev.meyi.bn.config;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleName;
import org.json.JSONArray;
import org.json.JSONObject;

public class Configuration {

  public static boolean collectionCheckDisabled;
  public static int craftingSortingOption;
  public static int craftingListLength;
  public static boolean showInstantSellProfit;
  public static boolean showSellOfferProfit;
  public static boolean showProfitPerMil;
  public static int suggestionListLength;

  public static JSONObject initializeConfig() {
    JSONObject newConfig = new JSONObject().put("api", BazaarNotifier.apiKey)
        .put("version", BazaarNotifier.VERSION)
        .put("craftingListLength", craftingListLength)
        .put("suggestionListLength", suggestionListLength)
        .put("craftingSortingOption", craftingSortingOption)
        .put("showInstantSellProfit", showInstantSellProfit)
        .put("showSellOfferProfit", showSellOfferProfit)
        .put("showProfitPerMil", showProfitPerMil)
        .put("collectionChecking", collectionCheckDisabled);

    JSONArray modules = new JSONArray();

    for (ModuleName value : ModuleName.values()) {
      Module m = value.returnDefaultModule();
      if (m != null) {
        modules.put(m.generateModuleConfig());
      }
    }

    BazaarNotifier.validApiKey = false;

    return newConfig.put("modules", modules);
  }
}
