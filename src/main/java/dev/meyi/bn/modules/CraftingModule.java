package dev.meyi.bn.modules;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.EnchantedCraftingHandler;
import dev.meyi.bn.utilities.ColorUtils;
import dev.meyi.bn.utilities.Defaults;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.Minecraft;
import org.json.JSONException;
import org.json.JSONObject;


import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CraftingModule extends Module{

    public CraftingModule() {
        super();
    }

    public CraftingModule(JSONObject module) {
        super(module);
    }


    Integer LongestXString;
    @Override
    protected void draw() {

        if (BazaarNotifier.bazaarDataRaw != null) {
            List<LinkedHashMap<String, Color>> items = new ArrayList<>();

            ArrayList<ArrayList<String>> list;
            list = EnchantedCraftingHandler.getBestEnchantRecipes();

            for(int i = 0; i<BazaarNotifier.config.getInt("craftingLength"); i++) {
                LinkedHashMap<String, Color> message = new LinkedHashMap<>();
                if(!list.get(i).isEmpty()) {

                    Double profitInstaSell = Double.valueOf(list.get(i).get(0));
                    Double profitSellOffer = Double.valueOf(list.get(i).get(1));
                    Double pricePerMil = Double.valueOf(list.get(i).get(2));
                    String itemName = list.get(i).get(3);


                    String itemNameConverted = BazaarNotifier.bazaarConversions.getString(itemName);
                    message.put(String.valueOf(i + 1), Color.MAGENTA);
                    message.put(". ", Color.MAGENTA);
                    message.put(itemNameConverted, Color.CYAN);
                    message.put(" - ", Color.GRAY);
                    if(BazaarNotifier.config.getBoolean("instasellProfit")) {
                        message.put(BazaarNotifier.df.format(profitInstaSell), getColor(profitInstaSell.intValue()));
                    }
                    if(BazaarNotifier.config.getBoolean("instasellProfit") && BazaarNotifier.config.getBoolean("sellofferProfit")) {
                        message.put(" / ", Color.GRAY);
                    }
                    if(BazaarNotifier.config.getBoolean("sellofferProfit")) {
                        message.put(BazaarNotifier.df.format(profitSellOffer), getColor(profitSellOffer.intValue()));
                    }
                    if(BazaarNotifier.config.getBoolean("sellofferProfit") && BazaarNotifier.config.getBoolean("profitPerMil")){
                        message.put(" /  ", Color.GRAY);
                    }
                    if(BazaarNotifier.config.getBoolean("profitPerMil")) {
                        message.put(BazaarNotifier.df.format(pricePerMil), getColorForMil(pricePerMil.intValue()));
                    }
                }else {
                    message.put("Error, just wait" ,Color.RED);
                }

                items.add(message);
            }
            int longestXString = ColorUtils.drawColorfulParagraph(items, x, y);
            boundsX = x + longestXString;
            this.LongestXString = longestXString;
            renderMaterials(checkHoveredText(),list);
        }
       boundsY = y + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * BazaarNotifier.config.getInt("craftingLength") + 18;

    }

    protected Color getColor(int price){
        if (price <= 0) {
            return Color.RED;
        }else if (price <= 5000){
            return Color.YELLOW;
        }else return Color.GREEN;
    }
    protected Color getColorForMil(int price){
        if (price <= 0) {
            return Color.RED;
        }else if (price <= 30000){
            return Color.YELLOW;
        }else return Color.GREEN;
    }

    @Override
    protected void reset() {
        x = Defaults.CRAFTING_MODULE_X;
        y = Defaults.CRAFTING_MODULE_Y;
    }

    @Override
    protected String name() {
        return ModuleName.CRAFTING.name();
    }

    @Override
    protected boolean shouldDrawBounds() {
        return true;
    }

    @Override
    protected int getMaxShift() {
        return 50;
    }

    protected int checkHoveredText(){
        int y2 = y+((BazaarNotifier.config.getInt("craftingLength"))*11);
        int mouseYFormatted = getMouseCoordinateY();
        int mouseXFormatted = getMouseCoordinateX();
        int relativeYMouse = (mouseYFormatted- (y))/11 ;
        if(mouseXFormatted >= x && mouseXFormatted <= x + (LongestXString/2) && mouseYFormatted >= y && mouseYFormatted <= y2){
            return relativeYMouse;
        }else{
            return -1;
        }

    }
    protected void renderMaterials(int hoveredText, ArrayList<ArrayList<String>> list){
        List<LinkedHashMap<String, Color>> material = new ArrayList<>();
        LinkedHashMap<String, Color> text = new LinkedHashMap<>();

        if (hoveredText > -1){
           if(BazaarNotifier.enchantCraftingList.getJSONObject("normal").has(list.get(hoveredText).get(3))){
               try{
               text.put("160x ", Color.LIGHT_GRAY);
               text.put(BazaarNotifier.bazaarConversions.getString(BazaarNotifier.enchantCraftingList.getJSONObject("normal").getJSONObject(list.get(hoveredText).get(3)).getString("material")), Color.LIGHT_GRAY);}catch (JSONException e){
                   text.put("Error", Color.RED);
               }
           }else{
               int materialCount;
               StringBuilder _material = new StringBuilder();
               materialCount = BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(list.get(hoveredText).get(3)).getJSONArray("material").length();
               for(int b = 0; b < materialCount/2; b++){
                   if(b == 0) {
                       _material.append(BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(list.get(hoveredText).get(3)).getJSONArray("material").getInt(1)).append("x ").append(BazaarNotifier.bazaarConversions.getString(BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(list.get(hoveredText).get(3)).getJSONArray("material").getString(0)));
                   }else{
                       _material.append(" | ").append(BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(list.get(hoveredText).get(3)).getJSONArray("material").getInt(b * 2 + 1)).append("x ").append(BazaarNotifier.bazaarConversions.getString(BazaarNotifier.enchantCraftingList.getJSONObject("other").getJSONObject(list.get(hoveredText).get(3)).getJSONArray("material").getString(b * 2)));
                   }
               }
               text.put(_material.toString(), Color.LIGHT_GRAY);
           }
           material.add(text);
            int longestXString = ColorUtils.drawColorfulParagraph(material, getMouseCoordinateX(), getMouseCoordinateY()-8-padding);
            Gui.drawRect(getMouseCoordinateX() - padding, getMouseCoordinateY()-8 - padding, getMouseCoordinateX()+longestXString + padding, getMouseCoordinateY()+8 + padding-8, 0xFF404040);
            ColorUtils.drawColorfulParagraph(material, getMouseCoordinateX(), getMouseCoordinateY()-8);
        }
    }
}
