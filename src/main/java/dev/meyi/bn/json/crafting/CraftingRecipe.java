package dev.meyi.bn.json.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.util.HashMap;
import java.util.Map;

public class CraftingRecipe {

  public Map<String, Integer> material;
  public String collection;

  public CraftingRecipe(Map<String, Integer> material, String collection) {
    this.material = material;
    this.collection = collection;
  }

  public static JsonDeserializer<CraftingRecipe> getDeserializer() {
    return (json, typeOfT, context) -> {
      Map<String, Integer> materials = new HashMap<>();
      JsonObject recipe = json.getAsJsonObject();

      String collection = recipe.get("collection").getAsString();
      JsonArray ja = recipe.getAsJsonArray("material");

      for (int i = 0; i < ja.size(); i += 2) {
        materials.put(ja.get(i).getAsString(), ja.get(i + 1).getAsInt());
      }

      return new CraftingRecipe(materials, collection);
    };
  }

  public static JsonSerializer<CraftingRecipe> getSerializer() {

    return (src, typeOfSrc, context) -> {
      JsonObject craftingRecipe = new JsonObject();

      JsonArray materialsArray = new JsonArray();

      src.material.forEach((k, v) -> {
        materialsArray.add(new JsonPrimitive(k));
        materialsArray.add(new JsonPrimitive(v));
      });

      craftingRecipe.add("material", materialsArray);
      craftingRecipe.addProperty("collection", src.collection);

      return craftingRecipe;
    };
  }
}
