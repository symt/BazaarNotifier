package dev.meyi.bn.utilities;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import java.util.Comparator;

public class JSONComparator implements Comparator<JsonObject> {

  String key;

  public JSONComparator(String key) {
    this.key = key;
  }

  @Override
  public int compare(JsonObject a, JsonObject b) {
    Double valA = 0.0d;
    double valB = 0.0d;

    try {
      valA = a.get(key).getAsDouble();
      valB = b.get(key).getAsDouble();
    } catch (JsonIOException e) {
      System.err.println("The provided value in " + key + " is not a double.");
      e.printStackTrace();
    }

    return -valA.compareTo(valB);
  }
}
