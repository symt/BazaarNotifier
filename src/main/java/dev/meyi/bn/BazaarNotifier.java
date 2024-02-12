package dev.meyi.bn;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import dev.meyi.bn.commands.BazaarNotifierCommand;
import dev.meyi.bn.config.Configuration;
import dev.meyi.bn.handlers.ChestTickHandler;
import dev.meyi.bn.handlers.EventHandler;
import dev.meyi.bn.handlers.MouseHandler;
import dev.meyi.bn.handlers.UpdateHandler;
import dev.meyi.bn.json.Order;
import dev.meyi.bn.json.resp.BazaarResponse;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleList;
import dev.meyi.bn.utilities.ReflectionHelper;
import dev.meyi.bn.utilities.ScheduledEvents;
import dev.meyi.bn.utilities.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;


@Mod(modid = BazaarNotifier.MODID, version = BazaarNotifier.VERSION)
public class BazaarNotifier {

  public static final String MODID = "BazaarNotifier";
  public static final String VERSION = "1.8.0-beta1";
  public static final String prefix =
      EnumChatFormatting.GOLD + "[" + EnumChatFormatting.YELLOW + "BN" + EnumChatFormatting.GOLD
          + "] " + EnumChatFormatting.RESET;
  public static final String header =
      EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "" + EnumChatFormatting.STRIKETHROUGH
          + "-------" + EnumChatFormatting.RESET + "" + EnumChatFormatting.GOLD + ""
          + EnumChatFormatting.BOLD + " [ " + EnumChatFormatting.YELLOW + "BazaarNotifier"
          + EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + " ] "
          + EnumChatFormatting.STRIKETHROUGH + "-------";
  public static final String RESOURCE_LOCATION = "https://raw.githubusercontent.com/symt/BazaarNotifier/resources/resources.json";
  public static DecimalFormat df = new DecimalFormat("#,##0.0");
  public static DecimalFormat dfNoDecimal = new DecimalFormat("#,###");

  public static boolean activeBazaar = true;
  public static boolean inBazaar = false;
  public static boolean forceRender = false;


  public static ArrayList<Order> orders = new ArrayList<>();
  public static BazaarResponse bazaarDataRaw;
  public static ModuleList modules;
  public static Configuration config;
  public static JsonObject resources;

  public static String guiToOpen = "";

  public static JsonObject enchantCraftingList;
  public static BiMap<String, String> bazaarConv = HashBiMap.create();

  public static File resourcesFile;

  public static void resetMod() {
    modules.resetAll();
    orders = new ArrayList<>();
  }

  public static void resetScale() {
    modules.resetScale();
  }

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    ReflectionHelper.setup();
    File bnDir = new File(event.getModConfigurationDirectory(), "BazaarNotifier");
    //noinspection ResultOfMethodCallIgnored
    bnDir.mkdirs();
    resourcesFile = new File(bnDir, "resources.json");
    Gson gson = new Gson();

    try {
      if (resourcesFile.isFile()) {
        try {
          byte[] bytes = Files.readAllBytes(Paths.get(resourcesFile.getPath()));
          if (bytes.length == 0) throw new JsonSyntaxException("Invalid JSON in Resources File");
          JsonReader resourcesReader = new JsonReader(new StringReader(new String(
              bytes)));
          resourcesReader.setLenient(true);
          resources = gson.fromJson(resourcesReader, JsonObject.class);
        } catch (JsonSyntaxException | ClassCastException e) {
          e.printStackTrace();
          JsonReader reader = new JsonReader(new InputStreamReader(Objects.requireNonNull(
              BazaarNotifier.class.getResourceAsStream("/resources.json")), StandardCharsets.UTF_8));
          reader.setLenient(true);
          resources = gson.fromJson(reader, JsonObject.class);
        }
      } else {
        JsonReader reader = new JsonReader(new InputStreamReader(Objects.requireNonNull(
            BazaarNotifier.class.getResourceAsStream("/resources.json")), StandardCharsets.UTF_8));
        reader.setLenient(true);
        resources = gson.fromJson(reader, JsonObject.class);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      Utils.updateResources();
    } catch (IOException e) {
      System.err.println("Error while getting resources from GitHub");
      e.printStackTrace();
      JsonObject bazaarConversions = resources.getAsJsonObject("bazaarConversions");
      enchantCraftingList = resources.getAsJsonObject("enchantCraftingList");
      bazaarConv = Utils.jsonToBimap(bazaarConversions);
    }
  }


  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new EventHandler());
    MinecraftForge.EVENT_BUS.register(new ChestTickHandler());
    MinecraftForge.EVENT_BUS.register(new MouseHandler());
    MinecraftForge.EVENT_BUS.register(new UpdateHandler());
    ClientCommandHandler.instance.registerCommand(new BazaarNotifierCommand());



    config = new Configuration();


    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  Utils.saveResources(resourcesFile, resources);
                  //config.save();
                }));

  }
  @Mod.EventHandler
  public void done(FMLLoadCompleteEvent e){
    ScheduledEvents.create();
    modules = new ModuleList();
    if (BazaarNotifier.config.firstLoad){
      for(Module m : modules){
        m.setActive(true);
        m.position.setPosition(20, 20);
        m.showInChat = false;
        m.showInGuis = false;
      }
      BazaarNotifier.config.firstLoad = false;
    }
  }

}
