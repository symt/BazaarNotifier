package dev.meyi.bn.config;

import com.google.gson.Gson;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.modules.calc.BankCalculator;
import dev.meyi.bn.utilities.Defaults;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Configuration {

  private static final int MODULE_LENGTH = 4;

  public boolean collectionCheck;
  public int craftingSortingOption;
  public int craftingListLength;
  public int suggestionListLength;
  public boolean showChatMessages;
  public boolean useBuyOrders;
  public boolean suggestionShowEnchantments;
  public boolean bankRawDifference;

  public String api = "";
  public String version;
  public ModuleConfig[] modules;

  public double bazaarProfit = 0;

  private boolean showInstantSellProfit;
  private boolean showSellOfferProfit;
  private boolean showProfitPerMil;

  public Configuration(boolean collectionCheck, int craftingSortingOption,
      int craftingListLength, boolean suggestionShowEnchantments,
      boolean showInstantSellProfit, boolean showSellOfferProfit, boolean showProfitPerMil,
      int suggestionListLength, boolean showChatMessages, String apiKey, boolean useBuyOrders, double bazaarProfit, boolean bankRawDifference,
      ModuleConfig[] modules) {
    this.collectionCheck = collectionCheck;
    this.craftingSortingOption = craftingSortingOption;
    this.craftingListLength = craftingListLength;
    this.suggestionShowEnchantments = suggestionShowEnchantments;
    this.showInstantSellProfit = showInstantSellProfit;
    this.showSellOfferProfit = showSellOfferProfit;
    this.showProfitPerMil = showProfitPerMil;
    this.suggestionListLength = suggestionListLength;
    this.bazaarProfit = bazaarProfit;
    this.bankRawDifference = bankRawDifference;
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
    BazaarNotifier.config.bazaarProfit = BankCalculator.getBazaarProfit();
    try {
      if (!file.isFile()) {
        //noinspection ResultOfMethodCallIgnored
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
    int i = 0;
    for (ModuleName moduleName : ModuleName.values()) {
      c[i++] = ModuleConfig.generateDefaultConfig(moduleName.name());
    }
    return new Configuration(Defaults.COLLECTION_CHECKING,
        Defaults.CRAFTING_SORTING_OPTION, Defaults.CRAFTING_LIST_LENGTH,
        Defaults.SUGGESTION_SHOW_ENCHANTMENTS,
        Defaults.INSTANT_SELL_PROFIT, Defaults.SELL_OFFER_PROFIT,
        Defaults.PROFIT_PER_MIL, Defaults.SUGGESTION_LIST_LENGTH, Defaults.SEND_CHAT_MESSAGES, "",
        Defaults.USE_BUY_ORDERS, 0, Defaults.BANK_RAW_DIFFERENCE, c);
  }

  public boolean isShowSellOfferProfit() {
    return showSellOfferProfit;
  }

  public void setShowSellOfferProfit(boolean showSellOfferProfit) {
    this.showSellOfferProfit = showSellOfferProfit;
    if (checkIfDisabled()) {
      this.showSellOfferProfit = true;
    }
  }

  public boolean isShowProfitPerMil() {
    return showProfitPerMil;
  }

  public void setShowProfitPerMil(boolean showProfitPerMil) {
    this.showProfitPerMil = showProfitPerMil;
    if (checkIfDisabled()) {
      this.showProfitPerMil = true;
    }
  }

  public boolean isShowInstantSellProfit() {
    return showInstantSellProfit;
  }

  public void setShowInstantSellProfit(boolean showInstantSellProfit) {
    this.showInstantSellProfit = showInstantSellProfit;
    if (checkIfDisabled()) {
      this.showInstantSellProfit = true;
    }
  }

  private boolean checkIfDisabled() {
    return !showProfitPerMil && !showSellOfferProfit && !showInstantSellProfit;
  }
}
