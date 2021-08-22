package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import org.apache.commons.lang3.text.WordUtils;

import java.util.*;


public class EnchantedCraftingHandler {


    public static ArrayList<ArrayList<String>> GetBestEnchantRecipes(){
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        Iterator<String> NormalKeys = BazaarNotifier.enchantCraftingList.getJSONObject("Normal").keys();
        String key1 = NormalKeys.next();

        for (int i = 0; i < BazaarNotifier.enchantCraftingList.getJSONObject("Normal").length(); i++) {
            try {

                String Material = BazaarNotifier.enchantCraftingList.getJSONObject("Normal").getJSONObject(key1).getString("Material");
                double Price1 = (BazaarNotifier.bazaarDataRaw.getJSONObject(key1).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) * 160;
                double Price2 = BazaarNotifier.bazaarDataRaw.getJSONObject(key1).getJSONArray("buy_summary").getJSONObject(1).getDouble("pricePerUnit") - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) * 160;
                double ProfitPerMilC = 1000000 / (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") * 160);
                double ProfitPerMil = ProfitPerMilC * BazaarNotifier.bazaarDataRaw.getJSONObject(key1).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") * 160);

                list.add(new ArrayList<>(Arrays.asList(Double.toString(Price1), Double.toString(Price2), Double.toString(ProfitPerMil), key1)));
                key1 = NormalKeys.next();
            }catch (Exception ignored){}
        }


        Iterator<String> OtherKeys = BazaarNotifier.enchantCraftingList.getJSONObject("Other").keys();
        for (int i = 0; i < BazaarNotifier.enchantCraftingList.getJSONObject("Other").length(); i++) {


            String ItemName = OtherKeys.next();
            double ItemSellPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit");
            double ItemBuyPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("buy_summary").getJSONObject(1).getDouble("pricePerUnit");
            double IngredientP= 0d;
            int IngredientC;
            double P= 0d;

            for(int h = 0; h < BazaarNotifier.enchantCraftingList.getJSONObject("Other").getJSONObject(ItemName).getJSONArray("Material").length(); h++){
                if(h % 2 == 0){
                    IngredientP = BazaarNotifier.bazaarDataRaw.getJSONObject(BazaarNotifier.enchantCraftingList.getJSONObject("Other").getJSONObject(ItemName).getJSONArray("Material").getString(h)).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                }else{
                    IngredientC = BazaarNotifier.enchantCraftingList.getJSONObject("Other").getJSONObject(ItemName).getJSONArray("Material").getInt(h);
                    P += (IngredientP*IngredientC);
                }
            }



            Double Price1 = ItemSellPrice - P;
            double Price2 = ItemBuyPrice - P;
            Double CountPerMil = 1000000 / P;
            double PricePerMil = CountPerMil * Price1;
            list.add(new ArrayList<>(Arrays.asList(Price1.toString(), Double.toString(Price2), Double.toString(PricePerMil), ItemName)));

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


            return list1.get(BazaarNotifier.config.getInt("CraftingGUITOGGLE")).compareTo(list2.get(BazaarNotifier.config.getInt("CraftingGUITOGGLE")));
        });
        Collections.reverse(list);
        return list;
    }


    public static String toggleCrafting(){
        if(BazaarNotifier.config.getInt("CraftingGUITOGGLE")==0){
            BazaarNotifier.config.put("CraftingGUITOGGLE", 1);
            return "Toggled crafting to selloffer";
        }else if(BazaarNotifier.config.getInt("CraftingGUITOGGLE")==1){
            BazaarNotifier.config.put("CraftingGUITOGGLE", 2);
            return "Toggled crafting to profit per million";
        }else {
            BazaarNotifier.config.put("CraftingGUITOGGLE", 0);
            return "Toggled crafting to instasell";
        }
    }

    public static String setCraftingLength(int Length){
        if(Length >= BazaarNotifier.enchantCraftingList.getJSONObject("Normal").length()+BazaarNotifier.enchantCraftingList.getJSONObject("Other").length()-1){
            return "Error Number to high";
        }else{
            BazaarNotifier.config.put("CraftingListLength" , Length);
            return "Setting CraftingListLength to " + Length;
        }
    }


    public static String[] getEnchantCraft(String itemU){
        String ItemName;
        try{ItemName = BazaarNotifier.bazaarConversionsReversed.getString(WordUtils.capitalize(itemU.toLowerCase()));}catch (Exception e){ItemName = "";}
        String[] values = new String[3];
        try {
            if (BazaarNotifier.enchantCraftingList.getJSONObject("Normal").has(ItemName)) {
                String Material = BazaarNotifier.enchantCraftingList.getJSONObject("Normal").getJSONObject(ItemName).getString("Material");
                double Price1 = (BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) * 160;
                double Price2 = BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("buy_summary").getJSONObject(1).getDouble("pricePerUnit") - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit")) * 160;
                double ProfitPerMilC = 1000000 / (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") * 160);
                double ProfitPerMil = ProfitPerMilC * BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") - (BazaarNotifier.bazaarDataRaw.getJSONObject(Material).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit") * 160);

                values[0] = String.valueOf(Price1);
                values[1] = String.valueOf(Price2);
                values[2] = String.valueOf(ProfitPerMil);

            } else if (BazaarNotifier.enchantCraftingList.getJSONObject("Other").has(ItemName)) {
                double ItemSellPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("sell_summary").getJSONObject(1).getDouble("pricePerUnit");
                double ItemBuyPrice = BazaarNotifier.bazaarDataRaw.getJSONObject(ItemName).getJSONArray("buy_summary").getJSONObject(1).getDouble("pricePerUnit");
                double IngredientP = 0d;
                int IngredientC;
                double P = 0d;

                for (int h = 0; h < BazaarNotifier.enchantCraftingList.getJSONObject("Other").getJSONObject(ItemName).getJSONArray("Material").length(); h++) {
                    if (h % 2 == 0) {
                        IngredientP = BazaarNotifier.bazaarDataRaw.getJSONObject(BazaarNotifier.enchantCraftingList.getJSONObject("Other").getJSONObject(ItemName).getJSONArray("Material").getString(h)).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    } else {
                        IngredientC = BazaarNotifier.enchantCraftingList.getJSONObject("Other").getJSONObject(ItemName).getJSONArray("Material").getInt(h);
                        P += (IngredientP * IngredientC);
                    }
                }

                Double Price1 = ItemSellPrice - P;
                double Price2 = ItemBuyPrice - P;
                Double CountPerMil = 1000000 / P;
                double ProfitPerMil = CountPerMil * Price1;
                values[0] = String.valueOf(Price1);
                values[1] = String.valueOf(Price2);
                values[2] = String.valueOf(ProfitPerMil);
            }
        }catch (Exception e){
                values[0] = "0";
                values[1]= "0";
                values[2]= "0";
        }
            return values;

    }

}