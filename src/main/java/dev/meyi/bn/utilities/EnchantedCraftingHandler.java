package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;


public class EnchantedCraftingHandler {


    public static ArrayList<ArrayList<String>> getBestEnchantRecipes(){
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        Iterator<String> normalKeys = BazaarNotifier.enchantCraftingList.getJSONObject("normal").keys();


        for (int i = 0; i < BazaarNotifier.enchantCraftingList.getJSONObject("normal").length(); i++) {
                String key1 =normalKeys.next();
                String Material = BazaarNotifier.enchantCraftingList.getJSONObject("normal").getJSONObject(key1).getString("material");
                double Price1 = (BazaarNotifier.bazaarDataRaw.getJSONObject(key1).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) * 160;
                double Price2 = BazaarNotifier.bazaarDataRaw.getJSONObject(key1).getJSONArray("buy_summary").getJSONObject(1).getDouble("pricePerUnit") - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) * 160;
                double ProfitPerMilC = 1000000 / (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") * 160);
                double ProfitPerMil = ProfitPerMilC * BazaarNotifier.bazaarDataRaw.getJSONObject(key1).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") * 160);

                list.add(new ArrayList<>(Arrays.asList(Double.toString(Price1), Double.toString(Price2), Double.toString(ProfitPerMil), key1)));


        }


        Iterator<String> otherKeys = BazaarNotifier.enchantCraftingList.getJSONObject("other").keys();
        for (int i = 0; i < BazaarNotifier.enchantCraftingList.getJSONObject("other").length(); i++) {


            String itemName = otherKeys.next();
            double itemSellPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(itemName).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit");
            double itemBuyPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(itemName).getJSONArray("buy_summary").getJSONObject(1).getDouble("pricePerUnit");
            double ingredientPrice= 0d;
            int ingredientCount;
            double P= 0d;

            for(int h = 0; h < BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(itemName).getJSONArray("material").length(); h++){
                if(h % 2 == 0){
                    ingredientPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(itemName).getJSONArray("material").getString(h)).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                }else{
                    ingredientCount = BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(itemName).getJSONArray("material").getInt(h);
                    P += (ingredientPrice*ingredientCount);
                }
            }



            Double profitInstaSell = itemSellPrice - P;
            double profitSellOffer = itemBuyPrice - P;
            Double countPerMil = 1000000 / P;
            double pricePerMil = countPerMil * profitInstaSell;
            list.add(new ArrayList<>(Arrays.asList(profitInstaSell.toString(), Double.toString(profitSellOffer), Double.toString(pricePerMil), itemName)));

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


            return list1.get(BazaarNotifier.config.getInt("craftingToggle")).compareTo(list2.get(BazaarNotifier.config.getInt("craftingToggle")));
        });
        Collections.reverse(list);
        return list;
    }


    public static String toggleCrafting(){
        if(BazaarNotifier.config.getInt("craftingToggle")==0){
            BazaarNotifier.config.put("craftingToggle", 1);
            return "Toggled crafting to selloffer";
        }else if(BazaarNotifier.config.getInt("craftingToggle")==1){
            BazaarNotifier.config.put("craftingToggle", 2);
            return "Toggled crafting to profit per million";
        }else {
            BazaarNotifier.config.put("craftingToggle", 0);
            return "Toggled crafting to instasell";
        }
    }

    public static String setCraftingLength(int length){
        if(length > BazaarNotifier.enchantCraftingList.getJSONObject("normal").length()+BazaarNotifier.enchantCraftingList.getJSONObject("other").length()){
            return length + " is too long";
        }else{
            BazaarNotifier.config.put("craftingLength" , length);
            return "Item list size set to " + length;
        }
    }

    public static String[] getEnchantCraft(String itemU){
        String ItemName = BazaarNotifier.bazaarConversionsReversed.getString(WordUtils.capitalize(itemU.toLowerCase()));
        String[] values = new String[3];
            if (BazaarNotifier.enchantCraftingList.getJSONObject("normal").has(ItemName)) {
                String Material = BazaarNotifier.enchantCraftingList.getJSONObject("normal").getJSONObject(ItemName).getString("material");
                double Price1 = (BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) * 160;
                double Price2 = BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("buy_summary").getJSONObject(1).getDouble("pricePerUnit") - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) * 160;
                double ProfitPerMilCount = 1000000 / (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") * 160);
                double ProfitPerMil = ProfitPerMilCount * BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") * 160);

                values[0] = BazaarNotifier.df.format(Price1);
                values[1] = BazaarNotifier.df.format(Price2);
                values[2] = BazaarNotifier.df.format(ProfitPerMil);

            } else if (BazaarNotifier.enchantCraftingList.getJSONObject("other").has(ItemName)) {
                double itemSellPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit");
                double itemBuyPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("buy_summary").getJSONObject(1).getDouble("pricePerUnit");
                double ingredientPrice = 0d;
                int ingredientCount;
                double materialCost = 0d;

                for (int h = 0; h < BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(ItemName).getJSONArray("material").length(); h++) {
                    if (h % 2 == 0) {
                        ingredientPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(ItemName).getJSONArray("material").getString(h)).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    } else {
                        ingredientCount = BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(ItemName).getJSONArray("material").getInt(h);
                        materialCost += (ingredientPrice * ingredientCount);
                    }
                }

                Double profitInstaSell = itemSellPrice - materialCost;
                double profitSellOffer = itemBuyPrice - materialCost;
                Double CountPerMil = 1000000 / materialCost;
                double profitPerMil = CountPerMil * profitInstaSell;
                values[0] = BazaarNotifier.df.format(profitInstaSell);
                values[1] = BazaarNotifier.df.format(profitSellOffer);
                values[2] = BazaarNotifier.df.format(profitPerMil);
            }

            return values;

    }

}