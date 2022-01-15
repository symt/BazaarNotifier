package dev.meyi.bn.modules.calc;

import com.google.gson.JsonElement;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.config.Configuration;
import dev.meyi.bn.utilities.Utils;
import java.io.IOException;
import java.util.*;

import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.text.WordUtils;


public class CraftingCalculator {

  private static String unlockedRecipes = "";


  public static ArrayList<ArrayList<String>> getBestEnchantRecipes() {
    ArrayList<ArrayList<String>> list = new ArrayList<>();

    for (Map.Entry<String, JsonElement> keys : BazaarNotifier.enchantCraftingList.getAsJsonObject("normal").entrySet()) {
      String itemName = keys.getKey();
      if (unlockedRecipes.contains(
              BazaarNotifier.enchantCraftingList.getAsJsonObject("normal").getAsJsonObject(itemName)
                      .get("collection").getAsString()) || Objects.equals(
              BazaarNotifier.enchantCraftingList.getAsJsonObject("normal").getAsJsonObject(itemName)
                      .get("collection").getAsString(), "NONE") || Configuration.collectionCheckDisabled) {
        if (BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName).getAsJsonArray("sell_summary")
                .size() > 0 &&
                BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName).getAsJsonArray("buy_summary")
                        .size() > 0) {
          if (BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName).getAsJsonArray("sell_summary")
                  .size() != 0) {// I don´t know why, but it sometimes crashes with org.json.JSONException: JSONArray[0] not found.
            String material = BazaarNotifier.enchantCraftingList.getAsJsonObject("normal")
                    .getAsJsonObject(itemName).get("material").getAsString();
            try {
              double price1 =
                      (BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName).getAsJsonArray("sell_summary")
                              .get(0).getAsJsonObject().get("pricePerUnit").getAsDouble()) -
                              (BazaarNotifier.bazaarDataRaw.getAsJsonObject(material)
                                      .getAsJsonArray("sell_summary").get(0).getAsJsonObject().get("pricePerUnit").getAsDouble())
                                      * 160;
              double price2 =
                      BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName).getAsJsonArray("buy_summary")
                              .get(0).getAsJsonObject().get("pricePerUnit").getAsDouble() -
                              (BazaarNotifier.bazaarDataRaw.getAsJsonObject(material)
                                      .getAsJsonArray("sell_summary").get(0).getAsJsonObject().get("pricePerUnit").getAsDouble())
                                      * 160;
              double profitPerMilC = 1000000 / (
                      BazaarNotifier.bazaarDataRaw.getAsJsonObject(material).getAsJsonArray("sell_summary")
                              .get(0).getAsJsonObject().get("pricePerUnit").getAsDouble() * 160);
              double profitPerMil =
                      profitPerMilC * BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName)
                              .getAsJsonArray("sell_summary").get(0).getAsJsonObject().get("pricePerUnit").getAsDouble() - (
                              BazaarNotifier.bazaarDataRaw.getAsJsonObject(material)
                                      .getAsJsonArray("sell_summary").get(0).getAsJsonObject().get("pricePerUnit").getAsDouble()
                                      * 160);

              list.add(new ArrayList<>(Arrays
                      .asList(Double.toString(price1), Double.toString(price2),
                              Double.toString(profitPerMil), itemName)));
            } catch (Exception ignored) {
            }
          }
        } else {
          list.add(new ArrayList<>(Arrays.asList("0", "0", "0", itemName)));
        }
      }
    }


    for (Map.Entry<String, JsonElement> keys : BazaarNotifier.enchantCraftingList.getAsJsonObject("other").entrySet()) {

      String itemName = keys.getKey();
      if (unlockedRecipes.contains(
          BazaarNotifier.enchantCraftingList.getAsJsonObject("other").getAsJsonObject(itemName)
              .get("collection").getAsString()) || Objects.equals(
          BazaarNotifier.enchantCraftingList.getAsJsonObject("other").getAsJsonObject(itemName)
              .get("collection").getAsString(), "NONE") || Configuration.collectionCheckDisabled) {
        if (BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName).getAsJsonArray("sell_summary")
            .size() > 0 &&
            BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName).getAsJsonArray("buy_summary")
                .size() > 0) {
          try {
            double itemSellPrice = BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName)
                .getAsJsonArray("sell_summary").get(0).getAsJsonObject().get("pricePerUnit").getAsDouble();
            double itemBuyPrice = BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName)
                .getAsJsonArray("buy_summary").get(0).getAsJsonObject().get("pricePerUnit").getAsDouble();
            double ingredientPrice = 0d;
            int ingredientCount;
            double P = 0d;

            for (int h = 0; h < BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                .getAsJsonObject(itemName).getAsJsonArray("material").size(); h++) {
              if (h % 2 == 0) {
                ingredientPrice = BazaarNotifier.bazaarDataRaw.getAsJsonObject(
                    BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                        .getAsJsonObject(itemName).getAsJsonArray("material").get(h).getAsString())
                    .getAsJsonArray("sell_summary").get(0).getAsJsonObject().get("pricePerUnit").getAsDouble();
              } else {
                ingredientCount = BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                    .getAsJsonObject(itemName).getAsJsonArray("material").get(h).getAsInt();
                P += (ingredientPrice * ingredientCount);
              }
            }

            double profitInstaSell = itemSellPrice - P;
            double profitSellOffer = itemBuyPrice - P;
            double countPerMil = 1000000 / P;
            double pricePerMil = countPerMil * profitInstaSell;
            list.add(new ArrayList<>(Arrays
                .asList(String.valueOf(profitInstaSell), Double.toString(profitSellOffer),
                    Double.toString(pricePerMil), itemName)));
          } catch (Exception ignored) {
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

      return list1.get(Configuration.craftingSortingOption).compareTo(list2.get(
          Configuration.craftingSortingOption));
    });
    Collections.reverse(list);
    return list;
  }


  public static String toggleCrafting() {
    if (Configuration.craftingSortingOption == 0) {
      Configuration.craftingSortingOption = 1;
      return "Crafting now sorts by sell offer";
    } else if (Configuration.craftingSortingOption == 1) {
      Configuration.craftingSortingOption = 2;
      return "Crafting now sorts by profit per million";
    } else {
      Configuration.craftingSortingOption = 0;
      return "Crafting now sorts by instant sell";
    }
  }

  public static String setCraftingLength(int length) {
    if (length > BazaarNotifier.enchantCraftingList.getAsJsonObject("normal").entrySet().size()
        + BazaarNotifier.enchantCraftingList.getAsJsonObject("other").entrySet().size()) {
      return length + " is too long";
    } else {
      Configuration.craftingListLength = length;
      return "Item list size set to " + length;
    }
  }

  public static String editCraftingModuleGUI(String craftingValue) {
    if (craftingValue.equalsIgnoreCase("instant_sell")) {
      Configuration.showInstantSellProfit = !Configuration.showInstantSellProfit;
      return
          (Configuration.showInstantSellProfit ? EnumChatFormatting.GREEN : EnumChatFormatting.RED)
              + "Toggled profit column (Instant Sell)";
    } else if (craftingValue.equalsIgnoreCase("sell_offer")) {
      Configuration.showSellOfferProfit = !Configuration.showSellOfferProfit;
      return (Configuration.showSellOfferProfit ? EnumChatFormatting.GREEN : EnumChatFormatting.RED)
          + "Toggled profit column (Sell Offer)";
    } else if (craftingValue.equalsIgnoreCase("ppm")) {
      Configuration.showProfitPerMil = !Configuration.showProfitPerMil;
      return (Configuration.showProfitPerMil ? EnumChatFormatting.GREEN : EnumChatFormatting.RED)
          + "Toggled profit column (Profit per 1M)";
    } else {
      return EnumChatFormatting.RED + "This value does not exist";
    }
  }

  public static String[] getEnchantCraft(String itemU) {
    String itemName = BazaarNotifier.bazaarConversionsReversed
        .get(WordUtils.capitalize(itemU.toLowerCase())).getAsString();
    String[] values = new String[3];
    if (BazaarNotifier.enchantCraftingList.getAsJsonObject("normal").has(itemName)) {
      if (BazaarNotifier.bazaarDataRaw.entrySet().size() != 0) {
        String material = BazaarNotifier.enchantCraftingList.getAsJsonObject("normal")
            .getAsJsonObject(itemName).get("material").getAsString();
        double price1 =
            (BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName).getAsJsonArray("sell_summary")
                .get(0).getAsJsonObject().get("pricePerUnit").getAsDouble()) -
                (BazaarNotifier.bazaarDataRaw.getAsJsonObject(material).getAsJsonArray("sell_summary")
                    .get(1).getAsJsonObject().get("pricePerUnit").getAsDouble()) * 160;
        double price2 =
            BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName).getAsJsonArray("buy_summary")
                .get(0).getAsJsonObject().get("pricePerUnit").getAsDouble() -
                (BazaarNotifier.bazaarDataRaw.getAsJsonObject(material).getAsJsonArray("sell_summary")
                    .get(1).getAsJsonObject().get("pricePerUnit").getAsDouble()) * 160;
        double profitPerMilCount = 1000000 / (
            BazaarNotifier.bazaarDataRaw.getAsJsonObject(material).getAsJsonArray("sell_summary")
                .get(0).getAsJsonObject().get("pricePerUnit").getAsDouble() * 160);
        double profitPerMil =
            profitPerMilCount * BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName)
                .getAsJsonArray("sell_summary").get(0).getAsJsonObject().get("pricePerUnit").getAsDouble() - (
                BazaarNotifier.bazaarDataRaw.getAsJsonObject(material).getAsJsonArray("sell_summary")
                    .get(1).getAsJsonObject().get("pricePerUnit").getAsDouble() * 160);

        values[0] = BazaarNotifier.df.format(price1);
        values[1] = BazaarNotifier.df.format(price2);
        values[2] = BazaarNotifier.df.format(profitPerMil);
      } else {
        values[0] = BazaarNotifier.df.format(0);
        values[1] = BazaarNotifier.df.format(0);
        values[2] = BazaarNotifier.df.format(0);
      }
    } else if (BazaarNotifier.enchantCraftingList.getAsJsonObject("other").has(itemName)) {
      if (BazaarNotifier.bazaarDataRaw.entrySet().size() != 0) {
        double itemSellPrice = BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName)
            .getAsJsonArray("sell_summary").get(0).getAsJsonObject().get("pricePerUnit").getAsDouble();
        double itemBuyPrice = BazaarNotifier.bazaarDataRaw.getAsJsonObject(itemName)
            .getAsJsonArray("buy_summary").get(0).getAsJsonObject().get("pricePerUnit").getAsDouble();
        double ingredientPrice = 0d;
        int ingredientCount;
        double materialCost = 0d;

        for (int h = 0;
            h < BazaarNotifier.enchantCraftingList.getAsJsonObject("other").getAsJsonObject(itemName)
                .getAsJsonArray("material").size(); h++) {
          if (h % 2 == 0) {
            ingredientPrice = BazaarNotifier.bazaarDataRaw.getAsJsonObject(
                BazaarNotifier.enchantCraftingList.getAsJsonObject("other").getAsJsonObject(itemName)
                    .getAsJsonArray("material").get(h).getAsString()).getAsJsonArray("sell_summary")
                .get(0).getAsJsonObject().get("pricePerUnit").getAsDouble();
          } else {
            ingredientCount = BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                .getAsJsonObject(itemName).getAsJsonArray("material").get(h).getAsInt();
            materialCost += (ingredientPrice * ingredientCount);
          }
        }

        double profitInstaSell = itemSellPrice - materialCost;
        double profitSellOffer = itemBuyPrice - materialCost;
        double CountPerMil = 1000000 / materialCost;
        double profitPerMil = CountPerMil * profitInstaSell;
        values[0] = BazaarNotifier.df.format(profitInstaSell);
        values[1] = BazaarNotifier.df.format(profitSellOffer);
        values[2] = BazaarNotifier.df.format(profitPerMil);
      } else {
        values[0] = BazaarNotifier.df.format(0);
        values[1] = BazaarNotifier.df.format(0);
        values[2] = BazaarNotifier.df.format(0);
      }
    }

    return values;

  }

  public static void getUnlockedRecipes() {
    try {
      unlockedRecipes = Utils.unlockedRecipes().toString();
    } catch (IOException ignored) {
    }
  }

}