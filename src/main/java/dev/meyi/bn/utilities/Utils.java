package dev.meyi.bn.utilities;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.resp.BazaarResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Utils {
  private static final TrustManager[] trustAllCerts = new TrustManager[] {
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
              return null;
            }

          }
  };
  private static String playerUUID = "";

  public static BazaarResponse getBazaarData() throws IOException {
    Gson gson = new Gson();
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(
        "https://api.hypixel.net/v2/skyblock/bazaar");
    HttpResponse response = client.execute(request);

    String result = IOUtils.toString(new BufferedReader
        (new InputStreamReader(
            response.getEntity().getContent())));

    client.close();

    if (isJSONValid(result)) {
      return gson.fromJson(result, BazaarResponse.class);
    } else {
      return new BazaarResponse(false, 0, null);
    }
  }


  /**
   * Waiting until a proper backend is built out before completing this feature.
   *
   * @see <a
   * href="https://github.com/symt/BazaarNotifier/blob/02114fbef16786c69d7b560d76de53f643970f7e/src/main/java/dev/meyi/bn/utilities/Utils.java#L64">the
   * old code</a>
   */
  public static List<String> unlockedRecipes() throws IOException {
    Gson gson = new Gson();
    if (BazaarNotifier.config.collectionCheck) {
      try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
        if (playerUUID.equals("")) {
          HttpGet request = new HttpGet(
              "https://api.mojang.com/users/profiles/minecraft/" + Minecraft.getMinecraft()
                  .getSession().getUsername()); // Change this to your username if testing
          HttpResponse response = client.execute(request);

          String uuidResponse = IOUtils
              .toString(
                  new BufferedReader(new InputStreamReader(response.getEntity().getContent())));

          try {
            playerUUID = gson.fromJson(uuidResponse, JsonObject.class).get("id").getAsString();
          } catch (JsonSyntaxException e) {
            return null;
          }
        }
      }
    }
    return null;
  }

  public static boolean isJSONValid(String jsonInString) {
    Gson gson = new Gson();
    try {
      gson.fromJson(jsonInString, JsonObject.class);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public static void updateResources() throws IOException, KeyManagementException, NoSuchAlgorithmException, ClassCastException {
    Gson gson = new Gson();
    HttpGet request;
    HttpResponse response;
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    CloseableHttpClient client = HttpClientBuilder.create().setSslcontext(sc).build();
    request = new HttpGet(BazaarNotifier.RESOURCE_LOCATION);
    response = client.execute(request);

    JsonReader jsonReader = new JsonReader(
            new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8)));
    jsonReader.setLenient(true);
    try {
      BazaarNotifier.resources = gson.fromJson(jsonReader, JsonObject.class);
      BazaarNotifier.bazaarConv = jsonToBimap(
              BazaarNotifier.resources.getAsJsonObject("bazaarConversions"));
      BazaarNotifier.enchantCraftingList = BazaarNotifier.resources
              .getAsJsonObject("enchantCraftingList");
    } catch (JsonSyntaxException | ClassCastException e) { //ClassCastException is thrown when GitHub is down
      e.printStackTrace();
    } finally {
      client.close();
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
      int levenshteinDistance = StringUtils
          .getLevenshteinDistance(userInput.toLowerCase(), key.toLowerCase(), threshold);
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

    return new String[]{closestConversion,
        BazaarNotifier.bazaarConv.inverse().getOrDefault(closestConversion, "")};
  }

  public static List<String> getLoreFromItemStack(ItemStack item) {
    NBTTagList lorePreFilter = item.getTagCompound().getCompoundTag("display")
        .getTagList("Lore", 8);

    List<String> lore = new ArrayList<>();

    for (int j = 0; j < lorePreFilter.tagCount(); j++) {
      lore.add(net.minecraft.util.StringUtils.stripControlCodes(lorePreFilter.getStringTagAt(j)));
    }

    return lore;
  }

  public static int getOrderAmountLeft(List<String> lore, int totalAmount) {
    int amountLeft;
    if (lore.get(3).startsWith("Filled:")) {
      if (lore.get(3).split(" ")[2].contains("100%")) {
        amountLeft = 0;
      } else {
        String intToParse = lore.get(3).split(" ")[1].split("/")[0];
        int amountFulfilled;

        if (intToParse.contains("k")) {
          amountFulfilled = (int) (Double.parseDouble(intToParse.replace("k", "")) * 1000);
        } else {
          amountFulfilled = Integer.parseInt(intToParse);
        }

        amountLeft = totalAmount - amountFulfilled;
      }
    } else {
      amountLeft = totalAmount;
    }
    return amountLeft;
  }
}
