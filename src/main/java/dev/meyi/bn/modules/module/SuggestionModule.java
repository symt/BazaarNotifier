package dev.meyi.bn.modules.module;

import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.migration.JsonName;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.utilities.RenderUtils;
import dev.meyi.bn.utilities.Defaults;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class SuggestionModule extends Module{

  public transient static final ModuleName type = ModuleName.SUGGESTION;
  public transient static List<String[]> list = new LinkedList<>();
  transient float longestXString = 1;

  @Switch(name= "Use Profit per Hour", category = "Suggestion Module")
  public boolean useProfitPerHour = false;

  @JsonName("suggestionListLength")
  @Slider(name = "Suggestion List Entries",
          category = "Suggestion Module",
          description = "The amount of entries in the Suggestion Module list",
          min = 1,max = 25,step = 1
  )
  public int suggestionListLength = Defaults.SUGGESTION_LIST_LENGTH;

  @JsonName("suggestionShowEnchantments")
  @Switch(name = "Show Enchantments",
          category = "Suggestion Module",
          description = "If the mod should recommend enchantments"
  )
  public boolean suggestionShowEnchantments = Defaults.SUGGESTION_SHOW_ENCHANTMENTS;

  public SuggestionModule() {
    super();
  }

  @Override
  protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
    draw();
  }

  @Override
  protected float getWidth(float scale, boolean example) {
    if(example){
      return 200 * scale;
    }else{
      return longestXString;
    }


  }

  @Override
  protected float getHeight(float scale, boolean example) {
    if(BazaarNotifier.config == null){
      return 100f;
    }
    return (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT
            * BazaarNotifier.config.suggestionModule.suggestionListLength
            + BazaarNotifier.config.suggestionModule.suggestionListLength * 2)*scale - 2;
  }

  @Override
  public void draw() {
    GL11.glTranslated(0, 0, 1);
    drawBounds();
    if (list.size() != 0) {
      List<LinkedHashMap<String, Color>> items = new ArrayList<>();

      for (int i = shift; i < BazaarNotifier.config.suggestionModule.suggestionListLength + shift; i++) {
        LinkedHashMap <String, Color> message = new LinkedHashMap <>();
        message.put((i + 1) + ". ", BazaarNotifier.config.numberColor.toJavaColor());
        message.put(list.get(i)[0], BazaarNotifier.config.itemColor.toJavaColor());
        message.put(" - ", BazaarNotifier.config.infoColor.toJavaColor());
        message.put("EP: ", Color.RED);
        message.put("" + BazaarNotifier.df.format(Double.parseDouble(list.get(i)[1]) *
                (useProfitPerHour?60:1)), Color.ORANGE);
        items.add(message);
      }

      longestXString = RenderUtils.drawColorfulParagraph(items, (int)position.getX(), (int)position.getY(), scale);

    } else {
      RenderUtils.drawCenteredString("Waiting for bazaar data", (int)position.getX(), (int)position.getY(), 0xAAAAAA, scale);
      //Todo add height and width
    }
    GL11.glTranslated(0, 0, -1);
  }

  @Override
  protected void reset() {
    position.setPosition(Defaults.SUGGESTION_MODULE_X,Defaults.SUGGESTION_MODULE_Y);
    scale = 1;
    enabled = true;
    BazaarNotifier.config.suggestionModule.suggestionListLength = Defaults.SUGGESTION_LIST_LENGTH;
  }

  @Override
  public String name() {
    return ModuleName.SUGGESTION.name();
  }

  @Override
  protected boolean shouldDrawBounds() {
    return true;
  }

  @Override
  protected int getMaxShift() {
    return list.size() - BazaarNotifier.config.suggestionModule.suggestionListLength;
  }

}


