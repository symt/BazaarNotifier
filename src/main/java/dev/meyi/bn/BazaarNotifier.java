package dev.meyi.bn;

import dev.meyi.bn.commands.BazaarNotifierCommand;
import dev.meyi.bn.handlers.EventHandler;
import dev.meyi.bn.handlers.MouseHandler;
import dev.meyi.bn.handlers.UpdateHandler;
import dev.meyi.bn.utilities.Defaults;
import dev.meyi.bn.utilities.ScheduledEvents;
import dev.meyi.bn.utilities.Utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

@Mod(modid = BazaarNotifier.MODID, version = BazaarNotifier.VERSION)
public class BazaarNotifier {

  public static final String MODID = "BazaarNotifier";
  public static final String VERSION = "1.2.2";
  public static final String prefix =
      EnumChatFormatting.GOLD + "[BazaarNotifier] " + EnumChatFormatting.RESET;
  public static String apiKey = "";

  public static DecimalFormat df = new DecimalFormat("#,###.0");

  public static int suggestionModuleX = Defaults.DEFAULT_SUGGESTION_MODULE_X;
  public static int suggestionModuleY = Defaults.DEFAULT_SUGGESTION_MODULE_Y;
  public static int currentBoundsX;
  public static int currentBoundsY;

  public static boolean activeBazaar = true;
  public static boolean inBazaar = false;
  public static boolean render = true;


  public static JSONObject orders = new JSONObject();
  public static JSONObject bazaarDataRaw = new JSONObject();
  public static JSONObject bazaarCache = new JSONObject();
  public static JSONArray bazaarDataFormatted = new JSONArray();

  public static JSONObject bazaarConversions = new JSONObject(
      new JSONTokener(BazaarNotifier.class.getResourceAsStream("/bazaarConversions.json")));
  public static JSONObject bazaarConversionsReversed = new JSONObject(
      new JSONTokener(BazaarNotifier.class.getResourceAsStream("/bazaarConversionsReversed.json")));

  public static File configFile;

  /**
   * Resets the location of all of the modules and clears all stored user preferences or orders
   */
  public static void resetMod() {
    suggestionModuleX = Defaults.DEFAULT_SUGGESTION_MODULE_X;
    suggestionModuleY = Defaults.DEFAULT_SUGGESTION_MODULE_Y;
    orders = Defaults.DEFAULT_ORDERS_LAYOUT;
  }

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    configFile = event.getSuggestedConfigurationFile();

    if (configFile.isFile()) {
      try {
        String config = new String(Files.readAllBytes(Paths.get(configFile.getPath())));
        String[] splitConfig = config.split(",");
        if (splitConfig.length == 1) {
          apiKey = splitConfig[0];
        } else if (splitConfig.length == 3) {
          apiKey = splitConfig[0];
          suggestionModuleX = Integer.parseInt(splitConfig[1]);
          suggestionModuleY = Integer.parseInt(splitConfig[2]);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new EventHandler());
    MinecraftForge.EVENT_BUS.register(new MouseHandler());
    MinecraftForge.EVENT_BUS.register(new UpdateHandler());
    ClientCommandHandler.instance.registerCommand(new BazaarNotifierCommand());
    ScheduledEvents.create();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(() -> Utils.saveConfigFile(configFile, apiKey + "," + suggestionModuleX + "," + suggestionModuleY)));
  }
}
