package dev.meyi.bn.modules;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.EnchantedCraftingHandler;
import dev.meyi.bn.utilities.ColorUtils;
import dev.meyi.bn.utilities.Defaults;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.Minecraft;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.input.Mouse;


import java.awt.*;
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
            list = EnchantedCraftingHandler.GetBestEnchantRecipes();

            for(int i = 0; i<BazaarNotifier.config.getInt("CraftingListLength"); i++) {
                LinkedHashMap<String, Color> message = new LinkedHashMap<>();
                try{

                    Double __Price = Double.valueOf(list.get(i).get(0));
                    Double __Price2 =Double.valueOf(list.get(i).get(1));
                    Double PricePerMil =Double.valueOf(list.get(i).get(2));
                    String ItemName = list.get(i).get(3);


                    String ItemNameC = BazaarNotifier.bazaarConversions.getString(ItemName);
                    message.put(String.valueOf(i + 1), Color.MAGENTA);
                    message.put(". ", Color.MAGENTA);
                    message.put(ItemNameC, Color.CYAN);
                    message.put(" - ", Color.GRAY);
                    message.put(BazaarNotifier.df.format(__Price), getColor(__Price.intValue()));
                    message.put("  /   ", Color.GRAY);
                    message.put(BazaarNotifier.df.format(__Price2), getColor(__Price2.intValue()));
                    message.put(" ", Color.BLACK);
                    message.put(BazaarNotifier.df.format(PricePerMil), Color.PINK);




                }catch(IndexOutOfBoundsException e) {
                    message.put("ERROR, JUST WAIT...",Color.RED);
                }
                items.add(message);
            }
            int longestXString = ColorUtils.drawColorfulParagraph(items, x, y);
            boundsX = x + longestXString;
            this.LongestXString = longestXString;
            RenderMaterials(checkHoveredText(Mouse.getX()),list);
        }
       boundsY = y + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * BazaarNotifier.config.getInt("CraftingListLength") + 18;

    }

    protected Color getColor(int Price){
        if (Price <= 0) {
            return Color.RED;
        }else if (Price <= 5000){
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
    //9 pixel on top
    //12 between top and bottom of next(6+6)
    //21 pixel numer

    protected int checkHoveredText(int MouseX){
        int y2 = y+((BazaarNotifier.config.getInt("CraftingListLength"))*11);
        int MouseYFormatted = getMouseCoordinateY();
        int relativeYMouse = (MouseYFormatted- (y))/11 ;
        if(MouseX/3 >= x && MouseX/3 <= x + (LongestXString/2) && MouseYFormatted >= y && MouseYFormatted <= y2){
            return relativeYMouse;
        }else{
            return -1;
        }

    }
    protected void RenderMaterials(int HoveredText, ArrayList<ArrayList<String>> list){
        List<LinkedHashMap<String, Color>> Material = new ArrayList<>();
        LinkedHashMap<String, Color> Text = new LinkedHashMap<>();

        if (HoveredText > -1){
           if(BazaarNotifier.enchantCraftingList.getJSONObject("Normal").has(list.get(HoveredText).get(3))){
               try{
               Text.put("160x ", Color.LIGHT_GRAY);
               Text.put(BazaarNotifier.bazaarConversions.getString(BazaarNotifier.enchantCraftingList.getJSONObject("Normal").getJSONObject(list.get(HoveredText).get(3)).getString("Material")), Color.LIGHT_GRAY);}catch (JSONException e){
                   Text.put("Error", Color.RED);
               }
           }else{
               int MaterialCount;
               StringBuilder NMaterial = new StringBuilder();
               try{MaterialCount = BazaarNotifier.enchantCraftingList.getJSONObject("Other").getJSONObject(list.get(HoveredText).get(3)).getJSONArray("Material").length();}catch (Exception e){MaterialCount = -1;}
               for(int b = 0; b < MaterialCount/2; b++){
                   if(b == 0) {
                       try{NMaterial.append(BazaarNotifier.enchantCraftingList.getJSONObject("Other").getJSONObject(list.get(HoveredText).get(3)).getJSONArray("Material").getInt(1)).append("x ").append(BazaarNotifier.bazaarConversions.getString(BazaarNotifier.enchantCraftingList.getJSONObject("Other").getJSONObject(list.get(HoveredText).get(3)).getJSONArray("Material").getString(0)));}catch (Exception ignored){}
                   }else{
                       try{NMaterial.append(" | ").append(BazaarNotifier.enchantCraftingList.getJSONObject("Other").getJSONObject(list.get(HoveredText).get(3)).getJSONArray("Material").getInt(b * 2 + 1)).append("x ").append(BazaarNotifier.bazaarConversions.getString(BazaarNotifier.enchantCraftingList.getJSONObject("Other").getJSONObject(list.get(HoveredText).get(3)).getJSONArray("Material").getString(b * 2)));}catch (Exception ignored){}
                   }
               }
               Text.put(NMaterial.toString(), Color.LIGHT_GRAY);
           }
           Material.add(Text);
            int longestXString = ColorUtils.drawColorfulParagraph(Material, getMouseCoordinateX(), getMouseCoordinateY()-8-padding);
            Gui.drawRect(getMouseCoordinateX() - padding, getMouseCoordinateY()-8 - padding, getMouseCoordinateX()+longestXString + padding, getMouseCoordinateY()+8 + padding-8, 0xFF404040);
            ColorUtils.drawColorfulParagraph(Material, getMouseCoordinateX(), getMouseCoordinateY()-8);
        }
    }
}
