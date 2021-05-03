package dev.meyi.bn;

import dev.meyi.bn.commands.BazaarNotifierCommand;
import dev.meyi.bn.handlers.ChestTickHandler;
import dev.meyi.bn.handlers.EventHandler;
import dev.meyi.bn.handlers.MouseHandler;
import dev.meyi.bn.handlers.UpdateHandler;
import dev.meyi.bn.modules.ModuleList;
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
  public static final String VERSION = "1.4.1";
  public static final String prefix =
      EnumChatFormatting.GOLD + "[" + EnumChatFormatting.YELLOW + "BN" + EnumChatFormatting.GOLD + "] " + EnumChatFormatting.RESET;
  public static String apiKey = "";

  public static DecimalFormat df = new DecimalFormat("#,###.0");
  public static DecimalFormat dfNoDecimal = new DecimalFormat("#,###");

  public static boolean activeBazaar = true;
  public static boolean inBazaar = false;
  public static boolean forceRender = false;
  public static boolean validApiKey = false;
  public static boolean apiKeyDisabled = true; // Change this if an api key is ever required to access the bazaar again.

  public static JSONArray orders = new JSONArray();
  public static JSONObject bazaarDataRaw = new JSONObject();
  public static JSONObject bazaarCache = new JSONObject();
  public static JSONArray bazaarDataFormatted = new JSONArray();

  public static JSONObject bazaarConversions = new JSONObject(
      new JSONTokener(BazaarNotifier.class.getResourceAsStream("/bazaarConversions.json")));
  public static JSONObject bazaarConversionsReversed = new JSONObject(
      new JSONTokener(BazaarNotifier.class.getResourceAsStream("/bazaarConversionsReversed.json")));

  public static File configFile;

  public static ModuleList modules;

  public static void resetMod() {
    modules.resetAll();
    orders = Defaults.DEFAULT_ORDERS_LAYOUT();
  }

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    configFile = event.getSuggestedConfigurationFile();
    String config = null;

    try {
      if (configFile.isFile()) {
        config = new String(Files.readAllBytes(Paths.get(configFile.getPath())));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (config != null && Utils.isValidJSONObject(config)) {
      modules = new ModuleList(
          new JSONObject(config));
    } else {
      modules = new ModuleList();
    }
  }

  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new EventHandler());
    MinecraftForge.EVENT_BUS.register(new ChestTickHandler());
    MinecraftForge.EVENT_BUS.register(new MouseHandler());
    MinecraftForge.EVENT_BUS.register(new UpdateHandler());
    ClientCommandHandler.instance.registerCommand(new BazaarNotifierCommand());
    ScheduledEvents.create();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> Utils.saveConfigFile(configFile, modules.generateConfig().toString())));
  }
}
