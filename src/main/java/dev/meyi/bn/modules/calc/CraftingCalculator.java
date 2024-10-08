package dev.meyi.bn.modules.calc;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.calculation.CraftingPriceCalculation;
import dev.meyi.bn.json.crafting.CraftingRecipe;
import dev.meyi.bn.json.resp.BazaarItem;
import dev.meyi.bn.modules.module.CraftingModule;
import dev.meyi.bn.utilities.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CraftingCalculator {

  private static List<String> unlockedRecipes = new ArrayList<>();


  public static void getBestEnchantRecipes() {
    ArrayList<CraftingPriceCalculation> craftingPrices = new ArrayList<>();
    if (BazaarNotifier.craftingRecipeMap == null
        || BazaarNotifier.bazaarDataRaw.products.size() == 0) {
      return;
    }
    for (Map.Entry<String, CraftingRecipe> craftingRecipeEntry : BazaarNotifier.craftingRecipeMap.entrySet()) {

      String itemName = craftingRecipeEntry.getKey();
      String collection = craftingRecipeEntry.getValue().collection;

      if (BazaarNotifier.config.collectionCheck && !(unlockedRecipes.contains(collection)
          || collection.equalsIgnoreCase("NONE"))) {
        continue;
      }

      CraftingPriceCalculation cpc = getEnchantCraft(itemName);
      if (cpc != null) {
        craftingPrices.add(cpc);
      }

      CraftingModule.craftingPrices = craftingPrices;
    }

    sort();
  }

  public static CraftingPriceCalculation getEnchantCraft(String itemName) {
    if (BazaarNotifier.craftingRecipeMap.containsKey(itemName)) {
      if (BazaarNotifier.bazaarDataRaw.products.size() != 0) {
        if (BazaarNotifier.bazaarDataRaw.products.get(itemName).buy_summary.size() == 0
            || BazaarNotifier.bazaarDataRaw.products.get(itemName).sell_summary.size() == 0) {
          return null;
        }

        // buy order / instant sell
        double itemSellPrice = BazaarNotifier.bazaarDataRaw.products.get(itemName).sell_summary
            .get(0).getPriceWithTax();

        // instant buy / sell offer
        double itemBuyPrice = BazaarNotifier.bazaarDataRaw.products.get(itemName).buy_summary
            .get(0).getPriceWithTax();

        double orderMaterialCost = 0d;
        double instantMaterialCost = 0d;

        boolean missingMaterialOrder = false;
        boolean missingMaterialInstant = false;

        for (Map.Entry<String, Integer> recipe : BazaarNotifier.craftingRecipeMap.get(
            itemName).material.entrySet()) {

          BazaarItem item = BazaarNotifier.bazaarDataRaw.products.get(recipe.getKey());

          if (item.sell_summary.size() == 0) {
            missingMaterialOrder = true;
          }

          if (item.buy_summary.size() == 0) {
            missingMaterialInstant = true;
          }

          if (!missingMaterialOrder) {
            orderMaterialCost += (item.sell_summary.get(0).pricePerUnit * recipe.getValue());
          }

          if (!missingMaterialInstant) {
            instantMaterialCost += (item.buy_summary.get(0).pricePerUnit * recipe.getValue());
          }

        }

        CraftingPriceCalculation priceCalculation = new CraftingPriceCalculation();
        priceCalculation.itemName = itemName;

        if (!missingMaterialOrder) {
          priceCalculation.buyOrderInstantSell = itemSellPrice - orderMaterialCost;
          priceCalculation.buyOrderSellOffer = itemBuyPrice - orderMaterialCost;
          priceCalculation.buyOrderSellPercentage = (itemSellPrice / orderMaterialCost - 1) * 100;
        }

        if (!missingMaterialInstant) {
          priceCalculation.instantBuyInstantSell = itemSellPrice - instantMaterialCost;
          priceCalculation.instantBuySellOffer = itemBuyPrice - instantMaterialCost;
          priceCalculation.instantBuySellPercentage =
              (itemSellPrice / instantMaterialCost - 1) * 100;
        }

        return priceCalculation;
      }
    }

    return null;
  }

  public static void getUnlockedRecipes() {
    try {
      List<String> s = Utils.unlockedRecipes();
      if (s != null) {
        unlockedRecipes = s;

        // Honestly, if this is empty, we should just assume something went wrong and disable the collection check.
        if (unlockedRecipes.size() == 0) {
          BazaarNotifier.config.collectionCheck = false;
        }
      } else {
        BazaarNotifier.config.collectionCheck = false;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void sort() {
    int offset = (BazaarNotifier.config.craftingModule.useBuyOrders ? 0 : 3)
        + BazaarNotifier.config.craftingModule.craftingSortingOption;

    CraftingModule.craftingPrices.sort((o1, o2) -> {
      switch (offset) {
        case 0:
          return Double.compare(o2.buyOrderInstantSell, o1.buyOrderInstantSell);
        case 1:
          return Double.compare(o2.buyOrderSellOffer, o1.buyOrderSellOffer);
        case 2:
          return Double.compare(o2.buyOrderSellPercentage, o1.buyOrderSellPercentage);
        case 3:
          return Double.compare(o2.instantBuyInstantSell, o1.instantBuyInstantSell);
        case 4:
          return Double.compare(o2.instantBuySellOffer, o1.instantBuySellOffer);
        case 5:
          return Double.compare(o2.instantBuySellPercentage, o1.instantBuySellPercentage);
      }
      return 0;
    });
  }
}
