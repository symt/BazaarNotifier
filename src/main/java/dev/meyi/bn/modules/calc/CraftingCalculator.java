package dev.meyi.bn.modules.calc;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.CraftingModule;
import dev.meyi.bn.utilities.Utils;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.util.*;


public class CraftingCalculator {

  private static String unlockedRecipes = "";


  public static void getBestEnchantRecipes() {
    ArrayList<ArrayList<String>> list = new ArrayList<>();
    if (BazaarNotifier.enchantCraftingList == null
        || BazaarNotifier.bazaarDataRaw.products.size() == 0) {
      return;
    }
    for (Map.Entry<String, JsonElement> keys : BazaarNotifier.enchantCraftingList
        .getAsJsonObject("normal").entrySet()) {
      String itemName = keys.getKey();
      if (!BazaarNotifier.bazaarDataRaw.products.containsKey(itemName)) {
        continue;
      }
      if (unlockedRecipes.contains(
          BazaarNotifier.enchantCraftingList.getAsJsonObject("normal").getAsJsonObject(itemName)
              .get("collection").getAsString()) ||
          BazaarNotifier.enchantCraftingList.getAsJsonObject("normal").getAsJsonObject(itemName)
              .get("collection").getAsString().equals("NONE") || BazaarNotifier.config.collectionCheckDisabled) {
        if (BazaarNotifier.bazaarDataRaw.products.get(itemName).sell_summary.size() > 0 &&
            BazaarNotifier.bazaarDataRaw.products.get(itemName).buy_summary.size() > 0) {
          String material = BazaarNotifier.enchantCraftingList.getAsJsonObject("normal")
              .getAsJsonObject(itemName).get("material").getAsString();
          double price1 =
              (BazaarNotifier.bazaarDataRaw.products.get(itemName).sell_summary.get(0).pricePerUnit) -
                  (BazaarNotifier.config.useBuyOrders?
                          (BazaarNotifier.bazaarDataRaw.products.get(material).sell_summary.get(0).pricePerUnit * 160):
                          (BazaarNotifier.bazaarDataRaw.products.get(material).buy_summary.get(0).pricePerUnit * 160));
          double price2 =
              BazaarNotifier.bazaarDataRaw.products.get(itemName).buy_summary.get(0).pricePerUnit -
                  (BazaarNotifier.config.useBuyOrders?
                      (BazaarNotifier.bazaarDataRaw.products.get(material).sell_summary.get(0).pricePerUnit * 160):
                      (BazaarNotifier.bazaarDataRaw.products.get(material).buy_summary.get(0).pricePerUnit * 160));
          double profitPerMilC = 1000000 /( BazaarNotifier.config.useBuyOrders?
                  (BazaarNotifier.bazaarDataRaw.products.get(material).sell_summary.get(0).pricePerUnit * 160):
                  (BazaarNotifier.bazaarDataRaw.products.get(material).buy_summary.get(0).pricePerUnit * 160));
          double profitPerMil = profitPerMilC * price1;

          list.add(new ArrayList<>(Arrays
              .asList(Double.toString(price1), Double.toString(price2),
                  Double.toString(profitPerMil), itemName)));
        } else {
          list.add(new ArrayList<>(Arrays.asList("0", "0", "0", itemName)));
        }
      }
    }

    for (Map.Entry<String, JsonElement> keys : BazaarNotifier.enchantCraftingList
        .getAsJsonObject("other").entrySet()) {

      String itemName = keys.getKey();
      if (unlockedRecipes.contains(
          BazaarNotifier.enchantCraftingList.getAsJsonObject("other").getAsJsonObject(itemName)
              .get("collection").getAsString()) ||
          BazaarNotifier.enchantCraftingList.getAsJsonObject("other").getAsJsonObject(itemName)
              .get("collection").getAsString().equals("NONE")
          || BazaarNotifier.config.collectionCheckDisabled) {
        if (BazaarNotifier.bazaarDataRaw.products.get(itemName).sell_summary.size() > 0 &&
            BazaarNotifier.bazaarDataRaw.products.get(itemName).buy_summary.size() > 0) {
          try {
            double itemSellPrice = BazaarNotifier.bazaarDataRaw.products.get(itemName).sell_summary
                    .get(0).pricePerUnit;
            double itemBuyPrice = BazaarNotifier.bazaarDataRaw.products.get(itemName).buy_summary
                    .get(0).pricePerUnit;
            double ingredientPrice = 0d;
            int ingredientCount;
            double materialCost = 0d;
            if (BazaarNotifier.config.useBuyOrders){
              for (int h = 0; h < BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                      .getAsJsonObject(itemName).getAsJsonArray("material").size(); h++) {
                if (h % 2 == 0) {
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
          }else{
              for (int h = 0; h < BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                      .getAsJsonObject(itemName).getAsJsonArray("material").size(); h++) {
                if (h % 2 == 0) {
                  ingredientPrice = BazaarNotifier.bazaarDataRaw.products.get(
                                  BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                                          .getAsJsonObject(itemName).getAsJsonArray("material").get(h).getAsString())
                          .buy_summary.get(0).pricePerUnit;
                } else {
                  ingredientCount = BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                          .getAsJsonObject(itemName).getAsJsonArray("material").get(h).getAsInt();
                  materialCost += (ingredientPrice * ingredientCount);
                }
              }
            }
            double profitInstaSell = itemSellPrice - materialCost;
            double profitSellOffer = itemBuyPrice - materialCost;
            double countPerMil = 1000000 / materialCost;
            double pricePerMil = countPerMil * profitInstaSell ;
            list.add(new ArrayList<>(Arrays
                .asList(String.valueOf(profitInstaSell), Double.toString(profitSellOffer),
                    Double.toString(pricePerMil), itemName)));
          } catch (JsonIOException e) {
            e.printStackTrace();
          }
        } else {
          list.add(new ArrayList<>(Arrays.asList("0", "0", "0", itemName)));
        }
      }
    }

    list.sort((o1, o2) -> {
      ArrayList<Double> list1 = new ArrayList<>();
      ArrayList<Double> list2 = new ArrayList<>();
      list1.add(Double.valueOf(o1.get(0)));
      list1.add(Double.valueOf(o1.get(1)));
      list1.add(Double.valueOf(o1.get(2)));
      list2.add(Double.valueOf(o2.get(0)));
      list2.add(Double.valueOf(o2.get(1)));
      list2.add(Double.valueOf(o2.get(2)));

      return list1.get(BazaarNotifier.config.craftingSortingOption).compareTo(list2.get(
          BazaarNotifier.config.craftingSortingOption));
    });
    Collections.reverse(list);
    CraftingModule.list = list;
  }


  public static String toggleCrafting() {
    if (BazaarNotifier.config.craftingSortingOption == 0) {
      BazaarNotifier.config.craftingSortingOption = 1;
      return "Crafting now sorts by sell offer";
    } else if (BazaarNotifier.config.craftingSortingOption == 1) {
      BazaarNotifier.config.craftingSortingOption = 2;
      return "Crafting now sorts by profit per million";
    } else {
      BazaarNotifier.config.craftingSortingOption = 0;
      return "Crafting now sorts by instant sell";
    }
  }

  public static String setCraftingLength(int length) {
    if (length > BazaarNotifier.enchantCraftingList.getAsJsonObject("normal").entrySet().size()
        + BazaarNotifier.enchantCraftingList.getAsJsonObject("other").entrySet().size()) {
      return length + " is too long";
    } else {
      BazaarNotifier.config.craftingListLength = length;
      return "Item list size set to " + length;
    }
  }

  public static String editCraftingModuleGUI(String craftingValue) {
    if (craftingValue.equalsIgnoreCase("instant_sell")) {
      BazaarNotifier.config.showInstantSellProfit = !BazaarNotifier.config.showInstantSellProfit;
      return
          (BazaarNotifier.config.showInstantSellProfit ? EnumChatFormatting.GREEN
              : EnumChatFormatting.RED)
              + "Toggled profit column (Instant Sell)";
    } else if (craftingValue.equalsIgnoreCase("sell_offer")) {
      BazaarNotifier.config.showSellOfferProfit = !BazaarNotifier.config.showSellOfferProfit;
      return (BazaarNotifier.config.showSellOfferProfit ? EnumChatFormatting.GREEN
          : EnumChatFormatting.RED)
          + "Toggled profit column (Sell Offer)";
    } else if (craftingValue.equalsIgnoreCase("ppm")) {
      BazaarNotifier.config.showProfitPerMil = !BazaarNotifier.config.showProfitPerMil;
      return (BazaarNotifier.config.showProfitPerMil ? EnumChatFormatting.GREEN
          : EnumChatFormatting.RED)
          + "Toggled profit column (Profit per 1M)";
    } else {
      return EnumChatFormatting.RED + "This value does not exist";
    }
  }

  public static String[] getEnchantCraft(String item) {
    String itemName = BazaarNotifier.bazaarConv.inverse()
        .get(WordUtils.capitalize(item.toLowerCase()));
    String[] values = new String[6];
    if (BazaarNotifier.enchantCraftingList.getAsJsonObject("normal").has(itemName)) {
      if (BazaarNotifier.bazaarDataRaw.products.size() != 0) {
        String material = BazaarNotifier.enchantCraftingList.getAsJsonObject("normal")
            .getAsJsonObject(itemName).get("material").getAsString();
        double price1 =
            BazaarNotifier.bazaarDataRaw.products.get(itemName).sell_summary.get(0).pricePerUnit -
                BazaarNotifier.bazaarDataRaw.products.get(material).sell_summary.get(1).pricePerUnit
                    * 160;
        double price2 =
            BazaarNotifier.bazaarDataRaw.products.get(itemName).buy_summary.get(0).pricePerUnit -
                BazaarNotifier.bazaarDataRaw.products.get(material).sell_summary.get(1).pricePerUnit
                    * 160;
        double profitPerMilCount = 1000000 / (
            BazaarNotifier.bazaarDataRaw.products.get(material).sell_summary.get(0).pricePerUnit
                * 160);
        double profitPerMil = profitPerMilCount * price1;
        double price12 =
                BazaarNotifier.bazaarDataRaw.products.get(itemName).sell_summary.get(0).pricePerUnit -
                        BazaarNotifier.bazaarDataRaw.products.get(material).buy_summary.get(1).pricePerUnit
                                * 160;
        double price22 =
                BazaarNotifier.bazaarDataRaw.products.get(itemName).buy_summary.get(0).pricePerUnit -
                        BazaarNotifier.bazaarDataRaw.products.get(material).buy_summary.get(1).pricePerUnit
                                * 160;
        double profitPerMilCount2 = 1000000 / (
                BazaarNotifier.bazaarDataRaw.products.get(material).buy_summary.get(0).pricePerUnit
                        * 160);
        double profitPerMil2 = profitPerMilCount2 * price12;

        values[0] = BazaarNotifier.df.format(price1);
        values[1] = BazaarNotifier.df.format(price2);
        values[2] = BazaarNotifier.df.format(profitPerMil);
        values[3] = BazaarNotifier.df.format(price12);
        values[4] = BazaarNotifier.df.format(price22);
        values[5] = BazaarNotifier.df.format(profitPerMil2);
      } else {
        values[0] = BazaarNotifier.df.format(0);
        values[1] = BazaarNotifier.df.format(0);
        values[2] = BazaarNotifier.df.format(0);
        values[3] = BazaarNotifier.df.format(0);
        values[4] = BazaarNotifier.df.format(0);
        values[5] = BazaarNotifier.df.format(0);
      }
    } else if (BazaarNotifier.enchantCraftingList.getAsJsonObject("other").has(itemName)) {
      if (BazaarNotifier.bazaarDataRaw.products.size() != 0) {
        double itemSellPrice = BazaarNotifier.bazaarDataRaw.products.get(itemName).sell_summary
            .get(0).pricePerUnit;
        double itemBuyPrice = BazaarNotifier.bazaarDataRaw.products.get(itemName).buy_summary
            .get(0).pricePerUnit;
        double ingredientPrice = 0d;
        int ingredientCount;
        double materialCost = 0d;
        double ingredientPrice2 = 0d;
        int ingredientCount2;
        double materialCost2 = 0d;


        for (int h = 0; h < BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                .getAsJsonObject(itemName).getAsJsonArray("material").size(); h++) {
          if (h % 2 == 0) {
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

        for (int h = 0; h < BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                  .getAsJsonObject(itemName).getAsJsonArray("material").size(); h++) {
          if (h % 2 == 0) {
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


        double profitInstaSell = itemSellPrice - materialCost;
        double profitSellOffer = itemBuyPrice - materialCost;
        double CountPerMil = 1000000 / materialCost;
        double profitPerMil = CountPerMil * profitInstaSell - 1000000;
        double profitInstaSell2 = itemSellPrice - materialCost2;
        double profitSellOffer2 = itemBuyPrice - materialCost2;
        double CountPerMil2 = 1000000 / materialCost2;
        double profitPerMil2 = CountPerMil2 * profitInstaSell2;
        values[0] = BazaarNotifier.df.format(profitInstaSell);
        values[1] = BazaarNotifier.df.format(profitSellOffer);
        values[2] = BazaarNotifier.df.format(profitPerMil);
        values[3] = BazaarNotifier.df.format(profitInstaSell2);
        values[4] = BazaarNotifier.df.format(profitSellOffer2);
        values[5] = BazaarNotifier.df.format(profitPerMil2);
      } else {
        values[0] = BazaarNotifier.df.format(0);
        values[1] = BazaarNotifier.df.format(0);
        values[2] = BazaarNotifier.df.format(0);
        values[3] = BazaarNotifier.df.format(0);
        values[4] = BazaarNotifier.df.format(0);
        values[5] = BazaarNotifier.df.format(0);
      }
    }

    return values;

  }

  public static void getUnlockedRecipes() {
    try {
      List<String> s = Utils.unlockedRecipes();
      if (s != null) {
        unlockedRecipes = s.toString();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}