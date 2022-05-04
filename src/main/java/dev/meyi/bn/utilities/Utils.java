package dev.meyi.bn.utilities;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;
import dev.meyi.bn.BazaarNotifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.lwjgl.opengl.GL11;

public class Utils {


  private static String playerUUID = "";
  private static long recipeCooldown = 0;

  public static JsonObject getBazaarData() throws IOException {
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

    return new JsonParser().parse(result).getAsJsonObject().get("products").getAsJsonObject();
  }


  public static JsonArray unlockedRecipes() throws IOException {
    if(recipeCooldown + 300000 > System.currentTimeMillis()){
      return  new JsonArray();
    }else{
      recipeCooldown = System.currentTimeMillis();
    }
    if(!BazaarNotifier.validApiKey){
      BazaarNotifier.validApiKey = validateApiKey();
    }
    if (!BazaarNotifier.config.api.equals("") && BazaarNotifier.validApiKey) {

      HttpClient client = HttpClientBuilder.create().build();
      if (playerUUID.equals("")) {
        HttpGet request = new HttpGet(
            "https://api.mojang.com/users/profiles/minecraft/" + Minecraft.getMinecraft()
                .getSession().getUsername()); //Chance this to your Username
        HttpResponse response = client.execute(request);

        String uuidResponse = IOUtils
            .toString(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));

        try {
          playerUUID = new JsonParser().parse(uuidResponse).getAsJsonObject().get("id").getAsString();
        }catch (Exception e){
          return new JsonArray();
        }
      }

      HttpGet request = new HttpGet(
          "https://api.hypixel.net/skyblock/profiles?key=" + BazaarNotifier.config.api + "&uuid="
              + playerUUID);
      HttpResponse response = client.execute(request);

      String _results = IOUtils
          .toString(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
      JsonObject results = new JsonParser().parse(_results).getAsJsonObject();
      long lastSaved = 0;
      int profileIndex = 0;
      if(!results.get("success").getAsBoolean() || !results.has("profiles")){
        return new JsonArray();
      }

      for (int i = 0; i < results.get("profiles").getAsJsonArray().size(); i++) {
        if (results.get("profiles").getAsJsonArray().get(i).getAsJsonObject().get("members").getAsJsonObject()
            .get(playerUUID).getAsJsonObject().get("last_save").getAsLong() > lastSaved) {
          lastSaved = results.get("profiles").getAsJsonArray().get(i).getAsJsonObject().get("members").getAsJsonObject()
              .get(playerUUID).getAsJsonObject().get("last_save").getAsLong();
          profileIndex = i;
        }
      }
      BazaarNotifier.playerDataFromAPI = results.get("profiles").getAsJsonArray()
          .get(profileIndex).getAsJsonObject().get("members").getAsJsonObject().get(playerUUID).getAsJsonObject();
      if(! results.getAsJsonArray("profiles").get(profileIndex).getAsJsonObject()
              .get("members").getAsJsonObject().get(playerUUID).getAsJsonObject().has("unlocked_coll_tiers")
              || !results.getAsJsonArray("profiles").get(profileIndex).getAsJsonObject()
              .get("members").getAsJsonObject().get(playerUUID).getAsJsonObject().has("slayer_bosses")){
        System.out.println("could not load unlocked collection tiers from API");
        return new JsonArray();
      }

      JsonArray unlockedCollections = results.getAsJsonArray("profiles").get(profileIndex).getAsJsonObject()
          .get("members").getAsJsonObject().get(playerUUID).getAsJsonObject().get("unlocked_coll_tiers").getAsJsonArray();
      JsonObject slayer = results.getAsJsonArray("profiles").get(profileIndex).getAsJsonObject()
          .get("members").getAsJsonObject().get(playerUUID).getAsJsonObject().get("slayer_bosses").getAsJsonObject();
      if (slayer.getAsJsonObject("zombie").getAsJsonObject("claimed_levels").has("level_4")) {
        unlockedCollections.add(new JsonPrimitive("zombie_4"));
      }
      if (slayer.getAsJsonObject("spider").getAsJsonObject("claimed_levels").has("level_4")) {
        unlockedCollections.add(new JsonPrimitive("spider_4"));
      }
      if (slayer.getAsJsonObject("wolf").getAsJsonObject("claimed_levels").has("level_4")) {
        unlockedCollections.add(new JsonPrimitive("wolf_4"));
      }
      if (slayer.getAsJsonObject("enderman").getAsJsonObject("claimed_levels").has("level_2")) {
        unlockedCollections.add(new JsonPrimitive("enderman_2"));
      }
      return unlockedCollections;
    } else {
      BazaarNotifier.config.collectionCheckDisabled = true;
      return new JsonArray();
    }
  }


  public static boolean isInteger(String s) {
    return isInteger(s, 10);
  }

  public static boolean isInteger(String s, int radix) {
    if (s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (i == 0 && s.charAt(i) == '-') {
        if (s.length() == 1) {
          return false;
        } else {
          continue;
        }
      }
      if (Character.digit(s.charAt(i), radix) < 0) {
        return false;
      }
    }
    return true;
  }


  public static JsonArray sortJSONArray(JsonArray jsonArr, String key) {
    List<JsonObject> jsonValues = new ArrayList<>();
    for (int i = 0; i < jsonArr.size(); i++) {
      jsonValues.add(jsonArr.get(i).getAsJsonObject());
    }
    jsonValues.sort(new JSONComparator(key));
    JsonArray sortedJsonArray = new JsonArray();
    for (int i = 0; i < jsonArr.size(); i++) {
      sortedJsonArray.add(jsonValues.get(i));
    }
    return sortedJsonArray;
  }

  public static boolean isJSONValid(String jsonInString) {
    Gson gson = new Gson();
    try {
      gson.fromJson(jsonInString, Object.class);
      return true;
    } catch(com.google.gson.JsonSyntaxException ex) {
      return false;
    }
  }

  public static boolean validateApiKey(String key) throws IOException {
    Gson gson = new Gson();
    return gson.fromJson(IOUtils.toString(new BufferedReader
        (new InputStreamReader(
            HttpClientBuilder.create().build().execute(new HttpGet(
                "https://api.hypixel.net/key?key=" + key)).getEntity()
                .getContent()))), JsonObject.class).getAsJsonObject().get("success").getAsBoolean();
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
    GL11.glScalef(moduleScale, moduleScale, 1);
    Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color);
    GL11.glScalef((float) Math.pow(moduleScale, -1), (float) Math.pow(moduleScale, -1), 1);
  }
  public static void updateResources() throws IOException{
    String result;
    HttpGet request;
    HttpResponse response;
    HttpClient client = HttpClientBuilder.create().build();
    request = new HttpGet(
            "https://raw.githubusercontent.com/symth/BazaarNotifier/1.5.0/src/main/resources/resources.json");
    response = client.execute(request);
    result = IOUtils.toString(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
    BazaarNotifier.config.resources =  new JsonParser().parse(result).getAsJsonObject();
    BazaarNotifier.bazaarConv = jsonToBimap(BazaarNotifier.config.resources.getAsJsonObject("bazaarConversions"));
    BazaarNotifier.enchantCraftingList =  BazaarNotifier.config.resources.getAsJsonObject("enchantCraftingList");
  }
  public static BiMap<String, String> jsonToBimap(JsonObject jsonObject){
    BiMap<String, String> b = HashBiMap.create();
    Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
    for (Map.Entry<String, JsonElement> entry: entries) {
      try {
        b.put(entry.getKey(), jsonObject.get(entry.getKey()).getAsString());
      }catch (IllegalArgumentException ignored){}
    }
    System.out.println(b);
    return b;
  }

}