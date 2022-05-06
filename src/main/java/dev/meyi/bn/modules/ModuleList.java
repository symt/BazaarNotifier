package dev.meyi.bn.modules;


import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.config.Configuration;
import dev.meyi.bn.config.ModuleConfig;
import dev.meyi.bn.handlers.MouseHandler;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ModuleList extends ArrayList<Module> {
  public ModuleList() {
   this(BazaarNotifier.config);
  }

  Module movingModule = null;

  public ModuleList(Configuration config) {

    ModuleConfig[] modules = config.modules;

    for (ModuleConfig module : modules) {
      switch (ModuleName.valueOf(module.name)) {
        case SUGGESTION:
          add(new SuggestionModule(module));
          break;
        case BANK:
          add(new BankModule(module));
          break;
        case NOTIFICATION:
          add(new NotificationModule(module));
          break;
        case CRAFTING:
          add(new CraftingModule(module));
          break;
        default:
          throw new IllegalStateException(
              "Unexpected value: " + ModuleName.valueOf(module.name));
      }
    }
  }

  public boolean toggleModule(ModuleName type) {
    if (type == ModuleName.SUGGESTION) {
      BazaarNotifier.config.showChatMessages ^= true;
    }

    for (Module m : this) {
      try {
        if (m.getClass().getField("type").get(null) == type) {
          m.active ^= true;

          return m.active;
        }
      } catch (NoSuchFieldException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }

    return false;
  }

  public void drawAllModules() {
    for (Module m : this) {
      if (m.active) {
        m.draw();
      }
    }
  }

  public void drawAllOutlines() {
    for (Module m : this) {
      if (m.active) {
        m.drawBounds();
      }
    }
  }

  public void rescaleCheck() {
    for (Module m : this) {
      if (m.inMovementBox() && Keyboard.isKeyDown(29) && !Keyboard.isKeyDown(42)) {
        float newScale = m.scale + (float) MouseHandler.mouseWheelMovement / 20;
        m.scale = Math.max(newScale, 0.1f);
      }
    }
  }

  public void shiftSettingCheck(){
    for (Module m: this) {
      if (MouseHandler.mouseWheelMovement != 0 && Keyboard.isKeyDown(42) && !Keyboard.isKeyDown(29)) {
        m.mouseWheelShift -= MouseHandler.mouseWheelMovement;
        if (m.mouseWheelShift < 1) {
          m.mouseWheelShift = 1;
        }
      }
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

  public void pageFlipCheck() {
    if (!Keyboard.isKeyDown(29) && !Keyboard.isKeyDown(42)) {
      if (MouseHandler.mouseWheelMovement != 0) {
        for (Module m : this) {
          if (m.inMovementBox() && m.getMaxShift() > 0) {
            m.shift += MouseHandler.mouseWheelMovement;
            if (m.shift > m.getMaxShift()) {
              m.shift = m.getMaxShift();
            } else if (m.shift < 0) {
              m.shift = 0;
            }

            break;
          }
        }
      }
    }
  }


  public void resetAll() {
    for (Module m : this) {
      m.reset();
    }
  }

  public void resetScale() {
    for (Module m : this) {
      m.scale = 1;
    }
  }
  public ModuleConfig[] generateConfig(){
    ModuleConfig[] config = new ModuleConfig[BazaarNotifier.modules.size()];
    for(int i = 0; i < BazaarNotifier.modules.size(); i++) {
      config[i] = this.get(i).generateModuleConfig();
    }
    return config;
  }

}
