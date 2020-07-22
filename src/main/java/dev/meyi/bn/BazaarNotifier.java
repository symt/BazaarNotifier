package dev.meyi.bn;

import dev.meyi.bn.commands.BazaarNotifierCommand;
import dev.meyi.bn.handlers.EventHandler;
import dev.meyi.bn.handlers.MouseHandler;
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

  public static DecimalFormat df = new DecimalFormat("#,###.00");

  public static int X_POS = 5;
  public static int Y_POS = 5;
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
          X_POS = Integer.parseInt(splitConfig[1]);
          Y_POS = Integer.parseInt(splitConfig[2]);
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
    ClientCommandHandler.instance.registerCommand(new BazaarNotifierCommand());
    ScheduledEvents.create();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(() -> Utils.saveConfigFile(configFile, apiKey + "," + X_POS + "," + Y_POS)));
  }
}
