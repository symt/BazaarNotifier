package dev.meyi.bn.modules;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.Utils;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.input.Mouse;

public class ModuleList extends ArrayList<Module> {

  Module movingModule = null;

  public ModuleList() {
    this(Utils.initializeConfig());
  }

  public ModuleList(JSONObject config) {
    BazaarNotifier.apiKey = config.getString("api");
    JSONObject workingConfig;
    if (!config.getString("version").equalsIgnoreCase(BazaarNotifier.VERSION)) {
      workingConfig = Utils.initializeConfig();
    } else {
      workingConfig = config;
    }

    JSONArray modules = workingConfig.getJSONArray("modules");

    for (Object m : modules) {
      JSONObject module = (JSONObject) m;
      switch (ModuleName.valueOf(module.getString("name"))) {
        case SUGGESTION:
          add(new SuggestionModule(module));
          break;
        case BANK:
          add(new BankModule(module));
          break;
        case NOTIFICATION:
          add(new NotificationModule(module));
          break;
        default:
          throw new IllegalStateException(
              "Unexpected value: " + ModuleName.valueOf(module.getString("name")));
      }
    }
  }

  public void drawAllModules() {
    for (Module m : this) {
      m.draw();
    }
  }

  public void drawAllOutlines() {
    for (Module m : this) {
      m.drawBounds();
    }
  }

  public void movementCheck() {
    if (Mouse.isButtonDown(0)) {
      if (movingModule == null) {
        for (Module m : this) {
          if (m.inMovementBox()) {
            m.needsToMove = true;
          }
        }
        for (Module m : this) {
          if (movingModule != null) {
            m.needsToMove = false;
          } else if (m.needsToMove) {
            movingModule = m;
          }
        }
      }
      if (movingModule != null) {
        movingModule.handleMovement();
      }
    } else {
      if (movingModule != null) {
        movingModule.needsToMove = false;
        movingModule.moving = false;
      }
      movingModule = null;
    }
  }

  public void resetAll() {
    for (Module m : this) {
      m.reset();
    }
  }

  public JSONObject generateConfig() {
    JSONObject o = new JSONObject().put("api", BazaarNotifier.apiKey)
        .put("version", BazaarNotifier.VERSION);

    JSONArray modules = new JSONArray();
    for (Module m : this) {
      modules.put(m.generateModuleConfig());
    }
    return o.put("modules", modules);
  }
}
