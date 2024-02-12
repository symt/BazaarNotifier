package dev.meyi.bn.modules.calc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.module.CraftingModule;
import dev.meyi.bn.utilities.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CraftingCalculator {

  private static List<String> unlockedRecipes = new ArrayList<>();


  public static void getBestEnchantRecipes() {
    ArrayList<String[]> list = new ArrayList<>();
    if (BazaarNotifier.enchantCraftingList == null
        || BazaarNotifier.bazaarDataRaw.products.size() == 0) {
      return;
    }
    for (Map.Entry<String, JsonElement> keys : BazaarNotifier.enchantCraftingList
        .getAsJsonObject("other").entrySet()) {

      String itemName = keys.getKey();
      String collection = BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
          .getAsJsonObject(itemName)
          .get("collection").getAsString();

      if (BazaarNotifier.config.collectionCheck && !(unlockedRecipes.contains(collection)
          || collection.equalsIgnoreCase("NONE"))) {
        continue;
      }

      String[] itemCrafting = getEnchantCraft(itemName);

      if (!itemCrafting[6].equalsIgnoreCase("0")) {
        list.add(itemCrafting);
      }
    }
    CraftingModule.list = list;

    sort();
  }


  public static void toggleCrafting() {
    BazaarNotifier.config.craftingModule.craftingSortingOption =
        (BazaarNotifier.config.craftingModule.craftingSortingOption + 1) % 3;
  }

  public static String[] getEnchantCraft(String itemName) {
    String[] values = new String[7];
    Arrays.fill(values, "0");

    if (BazaarNotifier.enchantCraftingList.getAsJsonObject("other").has(itemName)) {
      if (BazaarNotifier.bazaarDataRaw.products.size() != 0) {
        if (BazaarNotifier.bazaarDataRaw.products.get(itemName).buy_summary.size() == 0
            || BazaarNotifier.bazaarDataRaw.products.get(itemName).sell_summary.size() == 0) {
          return values;
        }

        // buy order / instant sell
        double itemSellPrice = BazaarNotifier.bazaarDataRaw.products.get(itemName).sell_summary
            .get(0).getPriceWithTax();

        // instant buy / sell offer
        double itemBuyPrice = BazaarNotifier.bazaarDataRaw.products.get(itemName).buy_summary
            .get(0).getPriceWithTax();
        double ingredientPrice = 0d;
        int ingredientCount;
        double materialCost = 0d;
        double ingredientPrice2 = 0d;
        int ingredientCount2;
        double materialCost2 = 0d;

        boolean missingMaterialOrder = false;
        boolean missingMaterialInstant = false;

        //Buy orders
        for (int h = 0; h < BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
            .getAsJsonObject(itemName).getAsJsonArray("material").size(); h++) {
          if (h % 2 == 0) {
            if (BazaarNotifier.bazaarDataRaw.products.get(
                BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                    .getAsJsonObject(itemName)
                    .getAsJsonArray("material").get(h).getAsString()).sell_summary.size() == 0) {
              missingMaterialOrder = true;
              break;
            }
            ingredientPrice = BazaarNotifier.bazaarDataRaw.products.get(
                    BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                        .getAsJsonObject(itemName).getAsJsonArray("material").get(h).getAsString())
                .sell_summary.get(0).pricePerUnit;
          } else {
            ingredientCount = BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                .getAsJsonObject(itemName).getAsJsonArray("material").get(h).getAsInt();
            materialCost += (ingredientPrice * ingredientCount);
          }
        }

        //Instant buy
        for (int h = 0; h < BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
            .getAsJsonObject(itemName).getAsJsonArray("material").size(); h++) {
          if (h % 2 == 0) {
            if (BazaarNotifier.bazaarDataRaw.products.get(
                BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                    .getAsJsonObject(itemName)
                    .getAsJsonArray("material").get(h).getAsString()).buy_summary.size() == 0) {
              missingMaterialInstant = true;
              break;
            }
            ingredientPrice2 = BazaarNotifier.bazaarDataRaw.products.get(
                    BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                        .getAsJsonObject(itemName).getAsJsonArray("material").get(h).getAsString())
                .buy_summary.get(0).pricePerUnit;
          } else {
            ingredientCount2 = BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                .getAsJsonObject(itemName).getAsJsonArray("material").get(h).getAsInt();
            materialCost2 += (ingredientPrice2 * ingredientCount2);
          }
        }

        if (!missingMaterialOrder) {
          double profitInstaSell = itemSellPrice - materialCost;
          double profitSellOffer = itemBuyPrice - materialCost;
          double profitPercentage = (itemSellPrice / materialCost - 1) * 100;
          values[0] = String.valueOf(profitInstaSell);
          values[1] = String.valueOf(profitSellOffer);
          values[2] = String.valueOf(profitPercentage);
        }

        if (!missingMaterialInstant) {
          double profitInstaSell2 = itemSellPrice - materialCost2;
          double profitSellOffer2 = itemBuyPrice - materialCost2;
          double profitPercentage2 = (itemSellPrice / materialCost2 - 1) * 100;
          values[3] = String.valueOf(profitInstaSell2);
          values[4] = String.valueOf(profitSellOffer2);
          values[5] = String.valueOf(profitPercentage2);
        }
      }
      values[6] = itemName;
    }

    return values;

  }

  public static Map<String, Integer> getMaterialsMap(String productId) {
    JsonArray materialsArray = BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
        .getAsJsonObject(productId).getAsJsonArray("material");
    Map<String, Integer> materials = new HashMap<>();
    for (int i = 0; i < materialsArray.size(); i += 2) {
      materials.put(materialsArray.get(i).getAsString(), materialsArray.get(i + 1).getAsInt());
    }
    return materials;
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
    int i = BazaarNotifier.config.craftingModule.useBuyOrders ? 0 : 3;

    CraftingModule.list.sort((o1, o2) -> Double.compare(
        Double.parseDouble(o2[i + BazaarNotifier.config.craftingModule.craftingSortingOption]),
        Double.parseDouble(o1[i + BazaarNotifier.config.craftingModule.craftingSortingOption])
    ));
  }
}
