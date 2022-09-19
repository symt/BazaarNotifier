package dev.meyi.bn.utilities;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.resp.BazaarResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.lwjgl.opengl.GL11;

public class Utils {

  private static final Pattern uuidMatcher = Pattern
      .compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$",
          Pattern.CASE_INSENSITIVE);
  private static String playerUUID = "";

  public static BazaarResponse getBazaarData() throws IOException {
    Gson gson = new Gson();
    HttpClient client = HttpClientBuilder.create().build();
    String apiBit = "";
    if (!BazaarNotifier.apiKeyDisabled) {
      apiBit = "?key=" + BazaarNotifier.config.api;
    }
    HttpGet request = new HttpGet(
        "https://api.hypixel.net/skyblock/bazaar" + apiBit);
    HttpResponse response = client.execute(request);

    String result = IOUtils.toString(new BufferedReader
        (new InputStreamReader(
            response.getEntity().getContent())));

    if (isJSONValid(result)) {
      return gson.fromJson(result, BazaarResponse.class);
    } else {
      return new BazaarResponse(false, 0, null);
    }
  }


  public static List<String> unlockedRecipes() throws IOException {
    Gson gson = new Gson();
    if (!BazaarNotifier.config.api.isEmpty() && (BazaarNotifier.validApiKey
        || (BazaarNotifier.validApiKey = validateApiKey()))) {

      HttpClient client = HttpClientBuilder.create().build();
      if (playerUUID.equals("")) {
        HttpGet request = new HttpGet(
            "https://api.mojang.com/users/profiles/minecraft/" + Minecraft.getMinecraft()
                .getSession().getUsername()); // Change this to your username if testing
        HttpResponse response = client.execute(request);

        String uuidResponse = IOUtils
            .toString(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));

        try {
          playerUUID = gson.fromJson(uuidResponse, JsonObject.class).get("id").getAsString();
        } catch (JsonSyntaxException e) {
          return null;
        }
      }

      HttpGet request = new HttpGet(
          "https://api.hypixel.net/skyblock/profiles?key=" + BazaarNotifier.config.api + "&uuid="
              + playerUUID);
      HttpResponse response = client.execute(request);

      String _results = IOUtils
          .toString(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
      JsonObject results = gson.fromJson(_results, JsonObject.class);
      long lastSaved = 0;
      int profileIndex = 0;
      if (!results.get("success").getAsBoolean() || !results.has("profiles")) {
        return null;
      }

      for (int i = 0; i < results.get("profiles").getAsJsonArray().size(); i++) {
        JsonObject playerDataFromAPI = results.getAsJsonArray("profiles").get(i).getAsJsonObject()
            .getAsJsonObject("members").getAsJsonObject(playerUUID);
        if (playerDataFromAPI.has("last_save")) {
          if (playerDataFromAPI.getAsJsonObject().get("last_save").getAsLong() > lastSaved) {
            lastSaved = playerDataFromAPI.get("last_save").getAsLong();
            profileIndex = i;
          }
        }
      }
      BazaarNotifier.playerDataFromAPI = results.getAsJsonArray("profiles")
          .get(profileIndex).getAsJsonObject().getAsJsonObject("members")
          .getAsJsonObject(playerUUID);
      if (!BazaarNotifier.playerDataFromAPI.has("unlocked_coll_tiers")
          || !BazaarNotifier.playerDataFromAPI.has("slayer_bosses")) {
        System.out.println("could not load unlocked collection tiers from API");
        return null;
      }
      List<String> unlockedCollections = new ArrayList<>();

      BazaarNotifier.playerDataFromAPI.getAsJsonArray("unlocked_coll_tiers")
          .forEach(cmd -> unlockedCollections.add(cmd.getAsString()));

      JsonObject slayer = BazaarNotifier.playerDataFromAPI.getAsJsonObject("slayer_bosses");
      Set<Map.Entry<String, JsonElement>> set = slayer.entrySet();
      for (Map.Entry<String, JsonElement> entry : set) {
        Set<Map.Entry<String, JsonElement>> set2 = slayer.getAsJsonObject(entry.getKey())
            .getAsJsonObject("claimed_levels").entrySet();
        for (Map.Entry<String, JsonElement> entry2 : set2) {
          unlockedCollections.add(
              entry.getKey().toUpperCase() + entry2.getKey().replace("level", "").toUpperCase());
        }
      }
      return unlockedCollections;
    } else {
      BazaarNotifier.config.collectionCheckDisabled = true;

      return null;
    }
  }

  public static boolean isJSONValid(String jsonInString) {
    Gson gson = new Gson();
    try {
      gson.fromJson(jsonInString, Object.class);
      return true;
    } catch (com.google.gson.JsonSyntaxException ex) {
      return false;
    }
  }

  public static boolean validateApiKey(String key) throws IOException {
    Gson gson = new Gson();
    if (uuidMatcher.matcher(key).find()) {
      try {
        return gson.fromJson(IOUtils.toString(new BufferedReader
            (new InputStreamReader(HttpClientBuilder.create().build().execute(new HttpGet(
                "https://api.hypixel.net/key?key=" + key)).getEntity()
                .getContent()))), JsonObject.class).getAsJsonObject().get("success")
            .getAsBoolean();
      } catch (JsonSyntaxException e) {
        return false;
      }
    }
    return false;
  }

  public static boolean validateApiKey() throws IOException {
    return validateApiKey(BazaarNotifier.config.api);
  }


  /**
   * @param price price per unit of order
   * @param i index of order
   * @param type Buy Order or Sell Offer (sets message color to dark purple vs blue)
   * @param notification yellow notification message
   * @return ChatComponentText completed message
   */
  public static ChatComponentText chatNotification(double price, int i, String type,
      String notification) {
    EnumChatFormatting messageColor =
        (notification.equalsIgnoreCase("REVIVED") ? EnumChatFormatting.GREEN
            : type.equalsIgnoreCase("Buy Order") ? EnumChatFormatting.DARK_PURPLE
                : EnumChatFormatting.BLUE);
    return new ChatComponentText(
        messageColor + type
            + EnumChatFormatting.GRAY + " for "
            + messageColor + BazaarNotifier.dfNoDecimal
            .format(BazaarNotifier.orders.get(i).startAmount)
            + EnumChatFormatting.GRAY + "x " + messageColor
            + BazaarNotifier.orders.get(i).product
            + EnumChatFormatting.YELLOW
            + " " + notification + " " + EnumChatFormatting.GRAY + "("
            + messageColor + BazaarNotifier.df.format(price)
            + EnumChatFormatting.GRAY + ")"
    );
  }


  public static void drawCenteredString(String text, int x, int y, int color, float moduleScale) {
    x = (int) (x / moduleScale + 200 / 4);
    y = (int) (y / moduleScale + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 6));
    GL11.glScalef(moduleScale, moduleScale, 1);
    Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color);
    GL11.glScalef((float) Math.pow(moduleScale, -1), (float) Math.pow(moduleScale, -1), 1);
  }

  public static void updateResources() throws IOException {
    Gson gson = new Gson();
    String result;
    HttpGet request;
    HttpResponse response;
    HttpClient client = HttpClientBuilder.create().build();
    request = new HttpGet(BazaarNotifier.RESOURCE_LOCATION);
    response = client.execute(request);
    result = IOUtils
        .toString(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
    try {
      BazaarNotifier.resources = gson.fromJson(result, JsonObject.class).getAsJsonObject();
      BazaarNotifier.bazaarConv = jsonToBimap(
          BazaarNotifier.resources.getAsJsonObject("bazaarConversions"));
      BazaarNotifier.enchantCraftingList = BazaarNotifier.resources
          .getAsJsonObject("enchantCraftingList");
    } catch (JsonSyntaxException e) {
      e.printStackTrace();
    }
  }

  public static BiMap<String, String> jsonToBimap(JsonObject jsonObject) {
    BiMap<String, String> b = HashBiMap.create();
    Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
    for (Map.Entry<String, JsonElement> entry : entries) {
      try {
        b.put(entry.getKey(), jsonObject.get(entry.getKey()).getAsString());
      } catch (IllegalArgumentException ignored) {
      }
    }
    return b;
  }

  public static void saveResources(File file, JsonObject resources) {
    Gson gson = new Gson();
    try {
      if (!file.isFile()) {
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
      }
      Files.write(Paths.get(file.getAbsolutePath()),
          gson.toJson(resources).getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String[] getItemIdFromName(String userInput) {
    int threshold = userInput.length() / 3;
    String closestConversion = "";
    int minLevenshteinDistance = threshold + 1;

    for (String key : BazaarNotifier.bazaarConv.values()) {
      int levenshteinDistance = StringUtils.getLevenshteinDistance(userInput.toLowerCase(), key.toLowerCase(), threshold);
      if (levenshteinDistance != -1) {
        if (minLevenshteinDistance > levenshteinDistance) {
          minLevenshteinDistance = levenshteinDistance;
          closestConversion = key;
          if (levenshteinDistance == 0) {
            break;
          }
        }
      }
    }

    return new String[]{closestConversion, BazaarNotifier.bazaarConv.inverse().getOrDefault(closestConversion, "")};
  }
}